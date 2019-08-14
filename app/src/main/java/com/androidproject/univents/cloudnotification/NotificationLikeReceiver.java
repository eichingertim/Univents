package com.androidproject.univents.cloudnotification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class NotificationLikeReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 315;
    private FirebaseFirestore db;

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventID = intent.getStringExtra("event_id");
        String updateID = intent.getStringExtra("update_id");
        initFireBase();
        executeLike(context, eventID, updateID);
    }

    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * increments the like-counter in firebase database
     * @param context includes the active context
     * @param eventID id of the current event
     * @param updateID id of the current update
     */
    private void executeLike(final Context context, String eventID, String updateID) {
        db.collection(context.getString(R.string.KEY_FB_EVENTS)).document(eventID)
                .collection(context.getString(R.string.KEY_FB_UPDATES)).document(updateID)
                .update("update_likes", FieldValue.increment(1)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Dir gefällt dieses Update", Toast.LENGTH_LONG).show();
                dismissNotification(context);
            }
        });


    }

    private void dismissNotification(Context context) {
        NotificationManager notifyMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.cancel(NOTIFICATION_ID);

    }
}
