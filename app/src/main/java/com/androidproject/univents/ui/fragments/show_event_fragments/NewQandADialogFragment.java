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

/**
 * DialogFragment where the user can either ask a new question
 * or the organizer can answer a asked question
 */
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
        if (getArguments().getBoolean(getString(R.string.KEY_INTENT_IS_ANSWER))) {
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

    /**
     * initializes necessary firebase-tools
     */
    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    /**
     * initializes all views from the layout
     * @param view layout belonging to this dialog-fragment
     */
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

    /**
     * sets the onClickListener to the 2 buttons
     */
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

    /**
     * handles whether the publish-action is a answer- or a question-action and
     * processes the data correspondingly;
     */
    private void publish() {
        if (canBePublished()) {
            Map<String, Object> qanda = new HashMap<>();

            if (isAnswer) {
                qanda.put(getString(R.string.KEY_FIREBASE_EVENT_QANDA_ANSWER), txtAnswer.getText().toString());
                publishAnswer(qanda);
            } else {
                qanda.put("eventQandAId", firebaseUser.getUid());
                qanda.put(getString(R.string.KEY_FIREBASE_EVENT_QANDA_QUESTION), txtQuestion.getText().toString());
                publishQuestion(qanda);
            }
        }
    }

    /**
     * publishes a question to the correspondingly place in the firebase cloud
     * @param qanda map with data (here the question and the id of the user who asked)
     */
    private void publishQuestion(Map<String, Object> qanda) {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).add(qanda)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getActivity(), getString(R.string.question_posted), Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                });
    }

    /**
     * publishes the answer to the correspondingly place in the firebase cloud
     * @param qanda map with data (here the answer)
     */
    private void publishAnswer(Map<String, Object> qanda) {
        String document = getArguments().getString("eventQandAId");
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).document(document)
                .update(qanda).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), getString(R.string.answer_posted), Toast.LENGTH_LONG).show();
                dismiss();
            }
        });
    }

    /**
     * checks whether a answer or question can be published
     * @return true or false
     */
    private boolean canBePublished() {

        if (isAnswer && txtAnswer.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.fill_all_fields), Toast.LENGTH_LONG).show();
            return false;
        } else if (!isAnswer && txtQuestion.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.fill_all_fields), Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /**
     * fills the textView belonging to question with data retrieved from firebase
     */
    private void fillQuestion() {
        final String document = getArguments().getString("eventQandAId");
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventID)
                .collection(getString(R.string.KEY_FIREBASE_COLLECTION_QANDAS)).document(document)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tvQuestion.setText(documentSnapshot
                        .getString(getString(R.string.KEY_FIREBASE_EVENT_QANDA_QUESTION)));
                if (documentSnapshot.getString(getString(R.string.KEY_FIREBASE_EVENT_QANDA_ANSWER))
                        != null) {
                    txtAnswer.setText(documentSnapshot
                            .getString(getString(R.string.KEY_FIREBASE_EVENT_QANDA_ANSWER)));
                }
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
