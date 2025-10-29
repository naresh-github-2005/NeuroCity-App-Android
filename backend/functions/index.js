const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendIssueStatusNotification = onDocumentUpdated(
  { document: "civic_issues/{issueId}", region: "asia-south1" },
  async (event) => {
    const newData = event.data.after.data();
    const prevData = event.data.before.data();

    console.log("🔔 Status change detected");
    console.log("Previous status:", prevData?.status);
    console.log("New status:", newData?.status);

    if (!newData || newData.status === prevData?.status) {
      console.log("❌ No status change detected");
      return;
    }

    // Fix: Use user_id instead of userId to match Android code
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

    console.log("✅ FCM Token found, sending notification");

    const message = {
      token: token,
      notification: {
        title: "Issue Status Updated",
        body: `Your issue status is now: ${newData.status}`,
      },
      // Add data payload for better handling
      data: {
        issueId: event.params.issueId,
        status: newData.status,
        type: "status_update"
      }
    };

    try {
      const response = await admin.messaging().send(message);
      console.log("✅ Notification sent successfully:", response);
    } catch (error) {
      console.error("❌ Error sending notification:", error);
    }
  }
);