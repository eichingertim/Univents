package com.androidproject.univents.cloudnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.androidproject.univents.MainActivity;
import com.androidproject.univents.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessageService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 315;
    private static final int PENDING_REQUEST_CODE = 201;
    private static final int PENDING_ACTION_REQUEST_CODE = 202;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {

            Map<String, String> messageData = remoteMessage.getData();

            sendNotification(messageData.get("title"), messageData.get("message")
                    , messageData.get("eventID"), messageData.get("updateID")
                    , messageData.get("eventTitle"));

        }
    }

    private void sendNotification(String title, String messageBody, String eventId
            , String updateId, String eventTitle) {

        //OnNormalClick
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PENDING_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        //OnActionClick
        Intent likeIntent = new Intent(this, NotificationLikeReceiver.class);
        likeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        likeIntent.putExtra("event_id", eventId);
        likeIntent.putExtra("update_id", updateId);
        PendingIntent likePendingIntent = PendingIntent.getBroadcast(this
                , PENDING_ACTION_REQUEST_CODE, likeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.updates_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        buildAndNotify(channelId, title, messageBody, likePendingIntent
                , defaultSoundUri, pendingIntent, eventTitle);
    }

    private void buildAndNotify(String channelId, String title, String messageBody
            , PendingIntent likePendingIntent, Uri defaultSoundUri, PendingIntent pendingIntent
            , String eventTitle) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_event_24dp)
                        .setContentTitle("Update: " + eventTitle)
                        .setContentText(Html.fromHtml("<b>"+title+"</b>"))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(Html.fromHtml("<b>"+title+"</b><br>" + messageBody)))
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_favorite_24dp, "Gefällt mir", likePendingIntent)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel for Event Updates",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}