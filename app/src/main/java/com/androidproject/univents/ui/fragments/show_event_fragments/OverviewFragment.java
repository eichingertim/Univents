package com.androidproject.univents.ui.fragments.show_event_fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.models.User;
import com.androidproject.univents.ui.ProfilePageActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment shows all details from a event in the showEventActivity
 */
public class OverviewFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private String eventId;
    private EventItem item;
    private User user;

    private TextView tvMonth, tvDay, tvTitle, tvDateTime, tvExactLocation
            , tvCity, tvLocationDetails, tvDescription, tvOrganizer;

    private ImageView imgTitlePicture, imgOrganizerProfilePic;

    private ImageView btnParticipate;
    private TextView tvParticipate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        eventId = getArguments().get(getString(R.string.KEY_FIREBASE_EVENT_ID)).toString();
        return inflater.inflate(R.layout.fragment_overview, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFireBase();
        getData();
        initViews(view);

    }

    /**
     * initializes necessary firebase-tools
     */
    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    /**
     * initializes all views from the layout
     * @param view layout belonging to the fragment
     */
    private void initViews(View view) {
        tvMonth = view.findViewById(R.id.tv_month);
        tvDay = view.findViewById(R.id.tv_day);
        tvTitle = view.findViewById(R.id.tv_event_title);
        tvDateTime = view.findViewById(R.id.tv_date_time);
        tvExactLocation = view.findViewById(R.id.tv_exact_location);
        tvCity = view.findViewById(R.id.tv_city);
        tvLocationDetails = view.findViewById(R.id.tv_location_detail);
        tvDescription = view.findViewById(R.id.tv_event_description);
        tvOrganizer = view.findViewById(R.id.tvOrganizer);

        imgTitlePicture = view.findViewById(R.id.img_event_picture);
        imgOrganizerProfilePic = view.findViewById(R.id.img_organizer_profile_pic);

        btnParticipate = view.findViewById(R.id.btn_participate);
        tvParticipate = view.findViewById(R.id.tv_participate);

    }

    /**
     * retrieves the data from firebase and saves it an eventItem object.
     * Afterwards the views are fill with data, participation state is set and
     * the onclickListener are set.
     */
    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                item = documentSnapshot.toObject(EventItem.class);
                fillViewsWithData();
                setParticipationState();
                setOnClickListener();
            }
        });
    }

    /**
     * checks whether the current user is the organizer of the event or participates
     * and sets correspondingly the visibility of participate-button and textView;
     */
    private void setParticipationState() {
        if (firebaseUser.getUid().equals(item.getEventOrganizer())) {
            tvParticipate.setVisibility(View.GONE);
            btnParticipate.setVisibility(View.GONE);
        } else if (item.getEventParticipants() == null) {
            tvParticipate.setText(getString(R.string.participate));
            btnParticipate.setImageResource(R.drawable.ic_star_border_24dp);
        } else if (item.getEventParticipants().contains(firebaseUser.getUid())) {
            tvParticipate.setText(getString(R.string.you_participate));
            btnParticipate.setImageResource(R.drawable.ic_star_orange_24dp);
        } else {
            tvParticipate.setText(getString(R.string.participate));
            btnParticipate.setImageResource(R.drawable.ic_star_border_24dp);
        }
    }

    /**
     * sets the onClickListener for the imageView and textView
     * to get to the profile and for the button to participate
     */
    private void setOnClickListener() {
        imgOrganizerProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });
        tvOrganizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        btnParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getEventParticipants() == null) {
                    participate();
                }
                if (item.getEventParticipants().contains(firebaseUser.getUid())) {
                    deleteParticipation();
                } else {
                    participate();
                }
            }
        });
    }

    /**
     * adds the user to the participants list and updates the list
     * of the event in the firebase-cloud
     */
    private void participate() {
        List<String> participants = item.getEventParticipants();
        participants.add(firebaseUser.getUid());
        Map<String, Object> update = new HashMap<>();
        update.put(getString(R.string.KEY_FIREBASE_EVENT_PARTICIPANTS), participants);

        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(item.getEventId())
                .update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                subscribeToNotifications();
            }
        });
    }

    /**
     * subscribe the user to the topic(eventId) to receive notifications for this event
     */
    private void subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic(item.getEventId())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Teilnahme erfolgreich", Toast.LENGTH_LONG).show();
                            getActivity().finish();
                            getActivity().startActivity(getActivity().getIntent());
                        }
                    }
                });
    }

    /**
     * deletes the user from the participants list and updates the list
     * of the event in the firebase-cloud
     */
    private void deleteParticipation() {
        List<String> participants = item.getEventParticipants();
        participants.remove(firebaseUser.getUid());
        Map<String, Object> update = new HashMap<>();
        update.put(getString(R.string.KEY_FIREBASE_EVENT_PARTICIPANTS), participants);

        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(item.getEventId())
                .update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                unSubscribeToNotifications();
            }
        });
    }

    /**
     * unsubscribes the user from the topic(eventId)
     * to receive notifications for this event
     */
    private void unSubscribeToNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(item.getEventId())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity()
                                    , getString(R.string.receive_no_more_notifications_for_the_event)
                                    , Toast.LENGTH_LONG).show();
                            getActivity().finish();
                            getActivity().startActivity(getActivity().getIntent());
                        }
                    }
                });
    }

    /**
     * starts the ProfilePageActivity to show the profile of the organizer
     */
    private void goToProfile() {
        Intent intent = new Intent(getActivity(), ProfilePageActivity.class);
        intent.putExtra(getString(R.string.KEY_FIREBASE_USER_ID), item.getEventOrganizer());
        startActivity(intent);
    }

    /**
     * fills the data of the event to the belonging views
     */
    private void fillViewsWithData() {
        Date begin = item.getEventBegin().toDate();
        Date end = item.getEventEnd().toDate();

        String month = new SimpleDateFormat("MMMM").format(begin).substring(0,3);
        String day = new SimpleDateFormat("dd").format(begin);
        tvMonth.setText(month);
        tvDay.setText(day);

        tvTitle.setText(item.getEventTitle());

        String strBegin = new SimpleDateFormat("dd.MM.yyyy HH.mm").format(begin);
        String strEnd = new SimpleDateFormat("dd.MM.yyyy HH.mm").format(end);
        tvDateTime.setText(strBegin + " - " + strEnd);

        String latLng = item.getEventExactLocation().getLatitude() + ", "
                + item.getEventExactLocation().getLongitude();
        tvExactLocation.setText(latLng);
        tvCity.setText(item.getEventCity());
        tvLocationDetails.setText(item.getEventDetailLocation());

        tvDescription.setText(item.getEventDescription());

        fillPictures();

    }


    private void fillPictures() {
        fillTitlePicture();
        fillOrganizerPicture();

    }

    /**
     * fills the data of title-picture to the belonging image-view
     */
    private void fillTitlePicture() {
        //With this ViewTreeObserver, we can check the height and width of the
        //given imageView and can set the picture to this size with PICASSO.
        ViewTreeObserver viewTreeObserver = imgTitlePicture.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imgTitlePicture.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = imgTitlePicture.getMeasuredHeight();
                int finalWidth = imgTitlePicture.getMeasuredWidth();
                Picasso.get().load(item.getEventPictureUrl())
                        .resize(finalWidth, finalHeight).centerCrop()
                        .into(imgTitlePicture);
                return true;
            }
        });
    }

    /**
     * fills the organizer's user-picture to the belonging imageView
     */
    private void fillOrganizerPicture() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS)).document(item.getEventOrganizer())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);

                if (!user.getPictureURL().equals("")) {
                    Picasso.get().load(user.getPictureURL()).noFade()
                            .into(imgOrganizerProfilePic);
                }

                if (user.getOrga()) {
                    tvOrganizer.setText(user.getOrgaName());
                } else {
                    String name = user.getFirstName() + " " + user.getLastName();
                    tvOrganizer.setText(name);
                }
            }
        });
    }

}
