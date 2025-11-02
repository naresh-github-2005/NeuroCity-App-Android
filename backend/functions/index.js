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

    console.log("🔔 Status change detected");
    console.log("Previous status:", prevData?.status);
    console.log("New status:", newData?.status);

    // Check if status actually changed
    if (!newData || newData.status === prevData?.status) {
      console.log("❌ No status change detected");
      return;
    }

    const userId = newData.user_id;
    
    if (!userId) {
      console.log("❌ No user_id found in document");
      return;
    }

    console.log("📱 Looking up user:", userId);

    const userSnap = await admin.firestore().collection("users").doc(userId).get();
    
    if (!userSnap.exists) {
      console.log("❌ User document not found");
      return;
    }

    const token = userSnap.data().fcmToken;
    
    if (!token) {
      console.log("❌ No FCM token found for user");
      return;
    }

    console.log("✅ FCM Token found, sending notification to USER");

    // Custom messages based on status
    let notificationBody = "";
    switch (newData.status) {
      case "Assigned":
        notificationBody = `Your ${newData.issue_type} issue has been assigned to ${newData.assigned_worker_name || "a worker"}.`;
        break;
      case "In Progress":
        notificationBody = `Work has started on your ${newData.issue_type} issue.`;
        break;
      case "Resolved":
        notificationBody = `Great news! Your ${newData.issue_type} issue has been resolved.`;
        break;
      default:
        notificationBody = `Your issue status is now: ${newData.status}`;
    }

    const message = {
      token,
      data: {
        title: "📢 Issue Status Updated",
        body: notificationBody,
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
          color: "#4CAF50"
        }
      }
    };

    try {
      const response = await admin.messaging().send(message);
      console.log("✅ Notification sent successfully to USER:", response);
    } catch (error) {
      console.error("❌ Error sending notification:", error);
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

    console.log("👷 Checking worker assignment");
    console.log("Previous worker:", prevData?.assigned_worker_id);
    console.log("New worker:", newData?.assigned_worker_id);

    // Check if worker was just assigned (didn't have one before, or changed)
    if (!newData?.assigned_worker_id || 
        newData.assigned_worker_id === prevData?.assigned_worker_id) {
      console.log("❌ No new worker assignment detected");
      return;
    }

    const workerId = newData.assigned_worker_id;
    
    console.log("📱 Looking up worker:", workerId);

    // Get worker's FCM token from users or workers collection
    // Adjust collection name based on your structure
    const workerSnap = await admin.firestore().collection("users").doc(workerId).get();
    
    if (!workerSnap.exists) {
      console.log("❌ Worker document not found");
      return;
    }

    const token = workerSnap.data().fcmToken;
    
    if (!token) {
      console.log("❌ No FCM token found for worker");
      return;
    }

    console.log("✅ FCM Token found, sending notification to WORKER");

    const message = {
      token,
      data: {
        title: "🔔 New Issue Assigned",
        body: `You have been assigned a ${newData.issue_type} issue in ${newData.department || "your department"}.`,
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
          tag: event.params.issueId
        }
      }
    };


    try {
      const response = await admin.messaging().send(message);
      console.log("✅ Notification sent successfully to WORKER:", response);
    } catch (error) {
      console.error("❌ Error sending notification:", error);
    }
  }
);
