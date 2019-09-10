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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewQandADialogFragment extends DialogFragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private String eventID;
    private boolean isAnswer = false;

    private ImageButton btnCloseDialog;
    private TextView btnPublish;

    private EditText txtQuestion;

    private TextView tvQuestion;
    private EditText txtAnswer;

    public static NewQandADialogFragment newInstance() {
        return new NewQandADialogFragment();
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
        if (getArguments().getBoolean(getString(R.string.IS_ANSWER))) {
            isAnswer = true;
            return inflater.inflate(R.layout.layout_dialog_qanda_create_answer, container, false);
        } else {
            isAnswer = false;
            return inflater.inflate(R.layout.layout_dialog_qanda_create_question, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFirebase();
        eventID = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
        initViews(view);
        setOnClickListeners();

    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    private void setOnClickListeners() {
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
            }
        });
    }

    private void publish() {
        if (canBePublished()) {
            Map<String, Object> qanda = new HashMap<>();

            if (isAnswer) {
                qanda.put("answer", txtAnswer.getText().toString());
                publishAnswer(qanda);
            } else {
                qanda.put("eventQandAId", firebaseUser.getUid());
                qanda.put("question", txtQuestion.getText().toString());
                publishQuestion(qanda);
            }
        }
    }

    private void publishQuestion(Map<String, Object> qanda) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).add(qanda)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getActivity(), "Frage wurde gepostet", Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                });
    }

    private void publishAnswer(Map<String, Object> qanda) {
        String document = getArguments().getString("eventQandAId");
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).document(document)
                .update(qanda).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "Antwort wurde gepostet", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    private boolean canBePublished() {

        if (isAnswer && txtAnswer.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Du musst alle Felder befüllen", Toast.LENGTH_LONG).show();
            return false;
        } else if (!isAnswer && txtQuestion.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Du musst alle Felder befüllen", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private void initViews(View view) {
        btnCloseDialog = view.findViewById(R.id.btn_close_dialog);
        btnPublish = view.findViewById(R.id.btn_publish);

        if (isAnswer) {
            txtAnswer = view.findViewById(R.id.txt_answer);
            tvQuestion = view.findViewById(R.id.tv_show_question);
            fillQuestion();
        } else {
            txtQuestion = view.findViewById(R.id.txt_question);
        }

    }

    private void fillQuestion() {
        String document = getArguments().getString("eventQandAId");
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).document(document)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tvQuestion.setText(documentSnapshot.getString("question"));
            }
        });
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }
}
