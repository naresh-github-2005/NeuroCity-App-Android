package com.example.neurocity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Called when a new FCM registration token is generated
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        saveTokenToFirestore(token);
    }

    // Save FCM token to Firestore under the current user's document
    private void saveTokenToFirestore(String token) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> {})
                    .addOnFailureListener(e -> {});
        }
    }

    // Handles all incoming FCM messages (data-only and mixed)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Notification";
        String body = "";
        String channelId = "default_channel";
        String clickAction = "DEFAULT";
        String type = "";
        String issueId = "";

        // ✅ Always prefer data payload (our messages are data-only now)
        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();

            if (data.containsKey("title")) title = data.get("title");
            if (data.containsKey("body")) body = data.get("body");
            if (data.containsKey("click_action")) clickAction = data.get("click_action");
            if (data.containsKey("type")) type = data.get("type");
            if (data.containsKey("issueId")) issueId = data.get("issueId");
        }

        // ✅ Choose the correct channel based on type
        switch (type) {
            case "status_update":
                channelId = "issue_updates";
                break;
            case "worker_assignment":
                channelId = "worker_assignments";
                break;
            default:
                channelId = "default_channel";
        }

        // ✅ Build intent dynamically based on click_action
        Intent intent;
        switch (clickAction) {
            case "OPEN_TRACKING":
                intent = new Intent(this, IssueTrackingActivity.class);
                intent.putExtra("issueId", issueId);
                break;
            case "OPEN_ISSUE_DETAILS":
                intent = new Intent(this, WorkerDashboardActivity.class);
                intent.putExtra("issueId", issueId);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
        }

        // Ensure it opens as a new task from notification
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                new Random().nextInt(), // Unique ID per notification
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        sendNotification(title, body, channelId, pendingIntent);
    }

    // Helper method to show the notification
    private void sendNotification(String title, String messageBody, String channelId, PendingIntent pendingIntent) {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // ✅ Create proper notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName;
            int importance = NotificationManager.IMPORTANCE_HIGH;

            switch (channelId) {
                case "issue_updates":
                    channelName = "Issue Updates";
                    break;
                case "worker_assignments":
                    channelName = "Worker Assignments";
                    break;
                default:
                    channelName = "General Notifications";
            }

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription("Notifications for " + channelName);
            channel.setSound(sound, null);
            notificationManager.createNotificationChannel(channel);
        }

        // ✅ Build the actual notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)  // Replace with your icon
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));

        // ✅ Show notification with unique ID
        int notificationId = new Random().nextInt(9999);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
