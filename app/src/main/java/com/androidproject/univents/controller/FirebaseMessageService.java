package com.androidproject.univents.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import android.text.Html;

import com.androidproject.univents.ui.MainActivity;
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

            sendNotification(messageData.get(getString(R.string.KEY_FCM_EVENT_UPDATE_TITLE))
                    , messageData.get(getString(R.string.KEY_FCM_EVENT_UPDATE_MESSAGE))
                    , messageData.get(getString(R.string.KEY_FCM_EVENT_UPDATE_EVENT_ID))
                    , messageData.get(getString(R.string.KEY_FCM_EVENT_UPDATE_ID))
                    , messageData.get(getString(R.string.KEY_FCM_EVENT_UPDATE_EVENT_TITLE)));

        }
    }

    /**
     * produces the intent and pendingintent for notification-onclick and action-onclick.
     * creates a channelid and a default sound for the notification
     */
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
        likeIntent.putExtra(getString(R.string.KEY_FCM_EVENT_UPDATE_EVENT_ID), eventId);
        likeIntent.putExtra(getString(R.string.KEY_FCM_EVENT_UPDATE_ID), updateId);
        PendingIntent likePendingIntent = PendingIntent.getBroadcast(this
                , PENDING_ACTION_REQUEST_CODE, likeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = getString(R.string.updates_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        buildAndNotify(channelId, title, messageBody, likePendingIntent
                , defaultSoundUri, pendingIntent, eventTitle);
    }

    /**
     * Builds and shows the update-notification
     */
    private void buildAndNotify(String channelId, String title, String messageBody
            , PendingIntent likePendingIntent, Uri defaultSoundUri, PendingIntent pendingIntent
            , String eventTitle) {
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_event_24dp)
                        .setContentTitle(getString(R.string.update_suffix) + eventTitle)
                        .setContentText(Html.fromHtml("<b>"+title+"</b>"))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(Html.fromHtml("<b>"+title+"</b><br>" + messageBody)))
                        .setColor(getResources().getColor(R.color.colorAccent))
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_favorite_24dp, getString(R.string.like_it), likePendingIntent)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        if (checkCouldSendNotification()) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }

    }

    private boolean checkCouldSendNotification() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_NOTIFICATIONS), true);
    }
}
