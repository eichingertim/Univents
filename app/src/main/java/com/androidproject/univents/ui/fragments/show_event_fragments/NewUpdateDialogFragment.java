package com.androidproject.univents.ui.fragments.show_event_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewUpdateDialogFragment extends DialogFragment {

    private FirebaseFirestore db;

    private String eventID;

    private ImageButton btnCloseDialog;
    private TextView btnPublishUpdate;

    private EditText txtUpdateTitle;
    private EditText txtUpdateDescription;

    private EventItem event;

    public static NewUpdateDialogFragment newInstance() {
        return new NewUpdateDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isDarkTheme()) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentDarkTheme);
        } else {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_update_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        eventID = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
        getEventData(view);

    }

    private void getEventData(final View view) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                event = documentSnapshot.toObject(EventItem.class);
                initViews(view);
                setOnClickListeners();
            }
        });
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setOnClickListeners() {
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnPublishUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishUpdate();
            }
        });
    }

    private void publishUpdate() {
        if (canBePublished()) {
            String updateId = generateEventId();
            Map<String, Object> newUpdate = new HashMap<>();
            newUpdate.put(getString(R.string.KEY_FIRBASE_EVENT_UPDATE_TITLE)
                    , txtUpdateTitle.getText().toString());
            newUpdate.put(getString(R.string.KEY_FIRBASE_EVENT_UPDATE_DESCRIPTION)
                    , txtUpdateDescription.getText().toString());
            newUpdate.put(getString(R.string.KEY_FIREBASE_EVENT_UPDATE_ID), updateId);
            newUpdate.put(getString(R.string.KEY_FCM_EVENT_UPDATE_EVENT_ID), eventID);
            newUpdate.put(getString(R.string.KEY_FCM_EVENT_UPDATE_EVENT_TITLE), event.getEventTitle());
            db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                    .collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENT_UPDATES)).document(updateId).set(newUpdate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), "Betrag wurde gepostet", Toast.LENGTH_LONG).show();
                            dismiss();
                        }
                    });
        }
    }

    private boolean canBePublished() {
        if (txtUpdateTitle.getText().toString().equals("")
                || txtUpdateDescription.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Du musst alle Felder befüllen", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void initViews(View view) {
        btnCloseDialog = view.findViewById(R.id.btn_close_update_dialog);
        btnPublishUpdate = view.findViewById(R.id.btn_publish_update);

        txtUpdateTitle = view.findViewById(R.id.txt_update_title);
        txtUpdateDescription = view.findViewById(R.id.txt_update_description);
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }

    private String generateEventId() {
        String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(20);

        for (int i = 0; i < 20; i++) {
            int index = (int)(availableChars.length()
                    * Math.random());
            sb.append(availableChars
                    .charAt(index));
        }
        return sb.toString();
    }
}
