const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

// ========================================
// 1️⃣ NOTIFY USER: Status Updates
// ========================================
exports.sendIssueStatusNotification = onDocumentUpdated(
  { document: "civic_issues/{issueId}", region: "asia-south1" },
  async (event) => {
    const newData = event.data.after.data();
    const prevData = event.data.before.data();

    if (!newData || newData.status === prevData?.status) return;

    const userId = newData.user_id;
    if (!userId) return;

    const userSnap = await admin.firestore().collection("users").doc(userId).get();
    if (!userSnap.exists) return;

    const token = userSnap.data().fcmToken;
    if (!token) return;

    // Custom message
    let body = "";
    switch (newData.status) {
      case "Assigned":
        body = `Your ${newData.issue_type} issue has been assigned to ${newData.assigned_worker_name || "a worker"}.`;
        break;
      case "In Progress":
        body = `Work has started on your ${newData.issue_type} issue.`;
        break;
      case "Resolved":
        body = `Great news! Your ${newData.issue_type} issue has been resolved.`;
        break;
      default:
        body = `Your issue status is now: ${newData.status}`;
    }

    const message = {
      token,
      notification: {
        title: "📢 Issue Status Updated",
        body,
      },
      data: {
        title: "📢 Issue Status Updated",
        body,
        issueId: event.params.issueId,
        status: newData.status,
        type: "status_update",
        click_action: "OPEN_TRACKING",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "issue_updates",
          sound: "default",
          color: "#4CAF50",
        },
      },
    };

    try {
      const res = await admin.messaging().send(message);
      console.log("✅ USER notification sent:", res);
    } catch (err) {
      console.error("❌ Error sending USER notification:", err);
    }
  }
);

// ========================================
// 2️⃣ NOTIFY WORKER: Assignment
// ========================================
exports.notifyWorkerOnAssignment = onDocumentUpdated(
  { document: "civic_issues/{issueId}", region: "asia-south1" },
  async (event) => {
    const newData = event.data.after.data();
    const prevData = event.data.before.data();

    if (
      !newData?.assigned_worker_id ||
      newData.assigned_worker_id === prevData?.assigned_worker_id
    ) {
      return;
    }

    const workerId = newData.assigned_worker_id;
    const workerSnap = await admin.firestore().collection("users").doc(workerId).get();
    if (!workerSnap.exists) return;

    const token = workerSnap.data().fcmToken;
    if (!token) return;

    const title = "🔔 New Issue Assigned";
    const body = `You have been assigned a ${newData.issue_type} issue in ${newData.department || "your department"}.`;

    const message = {
      token,
      notification: {
        title,
        body,
      },
      data: {
        title,
        body,
        issueId: event.params.issueId,
        issueType: newData.issue_type || "",
        department: newData.department || "",
        latitude: String(newData.latitude || ""),
        longitude: String(newData.longitude || ""),
        type: "worker_assignment",
        click_action: "OPEN_ISSUE_DETAILS",
      },
      android: {
        priority: "high",
        notification: {
          channelId: "worker_assignments",
          sound: "default",
          color: "#FF9800",
          tag: event.params.issueId,
        },
      },
    };

    try {
      const res = await admin.messaging().send(message);
      console.log("✅ WORKER notification sent:", res);
    } catch (err) {
      console.error("❌ Error sending WORKER notification:", err);
    }
  }
);
