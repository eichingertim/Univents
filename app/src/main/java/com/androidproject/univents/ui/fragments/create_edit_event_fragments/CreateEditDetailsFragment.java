package com.androidproject.univents.ui.fragments.create_edit_event_fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.androidproject.univents.R;
import com.androidproject.univents.models.EventItem;
import com.androidproject.univents.models.FabClickListener;
import com.androidproject.univents.ui.CreateEditEventActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class CreateEditDetailsFragment extends Fragment implements View.OnClickListener {

    private FabClickListener fabClickListener;
    private FloatingActionButton fabContinue;

    private EditText txtTitle, txtStartDate, txtStartTime,
            txtEndDate, txtEndTime, txtDescription;

    private ImageView ivTitlePicure;
    private TextView btnSelectPicture;

    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;

    private String currentPhotoPath = "";

    private static final int IMAGE_REQUEST_CODE = 111;
    private static final int PERMISSIONS_REQUEST_CODE = 201;

    private String eventId = "";
    private EventItem event;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    public void setFabClickListener(FabClickListener fabClickListener) {
        this.fabClickListener = fabClickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_edit_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(getString(R.string.KEY_FIREBASE_EVENT_ID));
        }
        initFirebase();
        initViews(view);
        setClickListener();
        if (getArguments() != null) {
            getData();
        }

    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    private void getData() {
        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_EVENTS)).document(eventId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                event = documentSnapshot.toObject(EventItem.class);
                fillData();
            }
        });
    }

    private void fillData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm");
        txtTitle.setText(event.getEventTitle());
        txtStartDate.setText(dateFormat.format(event.getEventBegin().toDate()));
        txtStartTime.setText(timeFormat.format(event.getEventBegin().toDate()));
        txtEndDate.setText(dateFormat.format(event.getEventEnd().toDate()));
        txtEndTime.setText(timeFormat.format(event.getEventEnd().toDate()));
        txtDescription.setText(event.getEventDescription());
        Picasso.get().load(event.getEventPictureUrl()).into(ivTitlePicure);
    }

    private void setClickListener() {
        fabContinue.setOnClickListener(this);
        txtStartDate.setOnClickListener(this);
        txtStartTime.setOnClickListener(this);
        txtEndDate.setOnClickListener(this);
        txtEndTime.setOnClickListener(this);
        btnSelectPicture.setOnClickListener(this);
    }

    private void initViews(View view) {
        fabContinue = view.findViewById(R.id.fab_controller_create_edit_event);

        txtTitle = view.findViewById(R.id.txt_create_edit_event_title);
        txtStartDate = view.findViewById(R.id.txt_create_edit_event_start_date);
        txtStartTime = view.findViewById(R.id.txt_create_edit_event_start_time);
        txtEndDate = view.findViewById(R.id.txt_create_edit_event_end_date);
        txtEndTime = view.findViewById(R.id.txt_create_edit_event_end_time);
        txtDescription = view.findViewById(R.id.txt_create_edit_event_description);

        ivTitlePicure = view.findViewById(R.id.img_create_edit_event_picture);
        btnSelectPicture = view.findViewById(R.id.tv_create_edit_select_picture);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_controller_create_edit_event:
                if (checkValidInput()) {
                    fabClickListener.onFabClick(getDataMap(), 0);
                }
                break;
            case R.id.txt_create_edit_event_start_date:
                showDatePickerDialog(txtStartDate);
                break;
            case R.id.txt_create_edit_event_start_time:
                showTimePickerDialog(txtStartTime);
                break;
            case R.id.txt_create_edit_event_end_date:
                showDatePickerDialog(txtEndDate);
                break;
            case R.id.txt_create_edit_event_end_time:
                showTimePickerDialog(txtEndTime);
                break;
            case R.id.tv_create_edit_select_picture:
                checkStoragePermission();
                break;

        }
    }

    private Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(getString(R.string.KEY_FIREBASE_EVENT_TITLE), txtTitle.getText().toString());

        Date begin = getDateOutOfString(txtStartDate.getText().toString(),
                txtStartTime.getText().toString());
        Date end =  getDateOutOfString(txtEndDate.getText().toString(),
                txtEndTime.getText().toString());

        map.put(getString(R.string.KEY_FIREBASE_EVENT_BEGIN), new Timestamp(begin));
        map.put(getString(R.string.KEY_FIREBASE_EVENT_END), new Timestamp(end));

        map.put(getString(R.string.KEY_FIREBASE_EVENT_DESCRIPTION), txtDescription.getText().toString());
        if (currentPhotoPath.equals("")) {
            map.put(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_URL), event.getEventPictureUrl());
        } else {
            map.put(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_PATH), currentPhotoPath);
        }
        return map;
    }

    private boolean checkValidInput() {

        if (txtTitle.getText().toString().equals("") || txtStartDate.getText().toString().equals("") ||
                txtEndDate.getText().toString().equals("") || txtStartTime.getText().toString().equals("") ||
                txtEndTime.getText().toString().equals("") || txtDescription.getText().toString().equals("")) {
            Toast.makeText(getActivity(), "Du musst alle Felder ausfüllen"
                    , Toast.LENGTH_LONG).show();
            return false;
        }

        Date start = getDateOutOfString(txtStartDate.getText().toString(),
                txtStartTime.getText().toString());
        Date end =  getDateOutOfString(txtEndDate.getText().toString(),
                txtEndTime.getText().toString());

        if (start == null || end == null || start.after(end)) {
            Toast.makeText(getActivity(), "Das Startdatum liegt hinter dem Endatum"
                    , Toast.LENGTH_LONG).show();
            return false;
        } else if (currentPhotoPath.equals("") && eventId.equals("")) {
            Toast.makeText(getActivity(), "Du musst ein Titelbild auswählen"
                    , Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private Date getDateOutOfString(String dateStr, String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyyHH.mm");
        Date date = null;
        try {
            date = format.parse(dateStr+timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void selectTitlePicture() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            currentPhotoPath = picturePath;
            ivTitlePicure.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    private void showDatePickerDialog(final EditText txtDate){
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        getActivity().setTheme(R.style.AppTheme);

        datePicker = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        ((CreateEditEventActivity)getActivity()).checkTheme();
                        String date = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
                        txtDate.setText(date);
                    }
                }, year, month, day);
        datePicker.show();
    }

    private void showTimePickerDialog(final EditText txtTime){
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        getActivity().setTheme(R.style.AppTheme);

        timePicker = new TimePickerDialog(getActivity()
                ,new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                ((CreateEditEventActivity)getActivity()).checkTheme();
                String time = hourOfDay + "." + minute;
                txtTime.setText(time);
            }
        }, hour, minute, true);
        timePicker.show();
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
            , PERMISSIONS_REQUEST_CODE);
        } else {
            selectTitlePicture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), getString(R.string.permssion_granted), Toast.LENGTH_SHORT).show();
                selectTitlePicture();
            }
        }
    }

}
