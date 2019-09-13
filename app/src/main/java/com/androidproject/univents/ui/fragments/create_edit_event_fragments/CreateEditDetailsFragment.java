package com.androidproject.univents.ui.fragments.create_edit_event_fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class CreateEditDetailsFragment extends Fragment implements View.OnClickListener {

    private static final int IMAGE_REQUEST_CODE = 111;
    private static final int PERMISSIONS_REQUEST_CODE = 201;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;

    private EditText txtTitle, txtStartDate, txtStartTime,
            txtEndDate, txtEndTime, txtDescription;
    private ImageView ivTitlePicure;
    private TextView btnSelectPicture;
    private FabClickListener fabClickListener;
    private Spinner spCategory;

    private FloatingActionButton fabContinue;
    private ArrayAdapter<CharSequence> adapter;

    private DatePickerDialog datePicker;
    private TimePickerDialog timePicker;

    private String currentPhotoPath;

    private String eventId = "";
    private EventItem event;

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
        initFireBase();
        initViews(view);
        setClickListener();
        if (getArguments() != null) {
            getData();
        }

    }

    /**
     * initializes necessary Firebase-Tools;
     */
    private void initFireBase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
    }

    /**
     * initializes all views from the layout
     * @param view layout belonging to this fragment
     */
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

        spCategory = view.findViewById(R.id.sp_create_edit_event_category);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.eventCategorysCreate, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    /**
     * sets the onClickListener to all necessary views
     */
    private void setClickListener() {
        fabContinue.setOnClickListener(this);
        txtStartDate.setOnClickListener(this);
        txtStartTime.setOnClickListener(this);
        txtEndDate.setOnClickListener(this);
        txtEndTime.setOnClickListener(this);
        btnSelectPicture.setOnClickListener(this);
    }

    /**
     * retrieves the event data from FireBase and saves the data in an EventItem-Object
     */
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

    /**
     * fills the data into the initialized views
     */
    private void fillData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm");
        txtTitle.setText(event.getEventTitle());
        txtStartDate.setText(dateFormat.format(event.getEventBegin().toDate()));
        txtStartTime.setText(timeFormat.format(event.getEventBegin().toDate()));
        txtEndDate.setText(dateFormat.format(event.getEventEnd().toDate()));
        txtEndTime.setText(timeFormat.format(event.getEventEnd().toDate()));
        txtDescription.setText(event.getEventDescription());
        String[] array = getResources().getStringArray(R.array.eventCategorysCreate);
        for (int i = 0; i< array.length; i++) {
            if (array[i].equals(event.getEventCategory())) {
                spCategory.setSelection(i);
                break;
            }
        }
        fillImageData();

    }

    /**
     * checks if a picture was selected from the gallery or not
     * and hands a boolean over to the loadImageMethod
     */
    private void fillImageData() {
        if (currentPhotoPath == null) {
            loadImage(false);
        } else {
            loadImage(true);
        }

    }

    /**
     * loads the image either from a file or from an URL depending on the boolean
     * @param isFile includes if it is a file from a path or not
     */
    private void loadImage(final boolean isFile) {

        //With this ViewTreeObserver, we can check the height and width of the
        //given imageView and can set the picture to this size with PICASSO.
        ViewTreeObserver viewTreeObserver = ivTitlePicure.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ivTitlePicure.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = ivTitlePicure.getMeasuredHeight();
                int finalWidth = ivTitlePicure.getMeasuredWidth();
                if (isFile) {
                    File file = new File(currentPhotoPath);
                    Picasso.get().load(file)
                            .resize(finalWidth, finalHeight).centerCrop()
                            .into(ivTitlePicure);
                } else {
                    Picasso.get().load(event.getEventPictureUrl())
                            .resize(finalWidth, finalHeight).centerCrop()
                            .into(ivTitlePicure);
                }
                return true;
            }
        });
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

    /**
     * Creates a key-value-map with all data that the user has entered/selected.
     * The keys of the map matches with the firebase keys
     * @return returns a map with the data
     */
    private Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(getString(R.string.KEY_FIREBASE_EVENT_TITLE), txtTitle.getText().toString());

        Date begin = getDateOutOfString(txtStartDate.getText().toString(),
                txtStartTime.getText().toString());
        Date end =  getDateOutOfString(txtEndDate.getText().toString(),
                txtEndTime.getText().toString());

        map.put(getString(R.string.KEY_FIREBASE_EVENT_BEGIN), new Timestamp(begin));
        map.put(getString(R.string.KEY_FIREBASE_EVENT_END), new Timestamp(end));

        map.put(getString(R.string.KEY_FIREBASE_EVENT_CATEGORY)
                , adapter.getItem(spCategory.getSelectedItemPosition()));

        map.put(getString(R.string.KEY_FIREBASE_EVENT_DESCRIPTION), txtDescription.getText().toString());
        if (currentPhotoPath == null) {
            map.put(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_URL), event.getEventPictureUrl());
        } else {
            map.put(getString(R.string.KEY_FIREBASE_EVENT_PICTURE_PATH), currentPhotoPath);
        }
        return map;
    }

    /**
     * checks if all inputs are valid and if not toasts with error-messages are displayed
     * @return if all is valid or not
     */
    private boolean checkValidInput() {

        if (txtTitle.getText().toString().equals("") || txtStartDate.getText().toString().equals("") ||
                txtEndDate.getText().toString().equals("") || txtStartTime.getText().toString().equals("") ||
                txtEndTime.getText().toString().equals("") || txtDescription.getText().toString().equals("")) {
            Toast.makeText(getActivity(), getString(R.string.fill_all_fields)
                    , Toast.LENGTH_LONG).show();
            return false;
        }

        Date start = getDateOutOfString(txtStartDate.getText().toString(),
                txtStartTime.getText().toString());
        Date end =  getDateOutOfString(txtEndDate.getText().toString(),
                txtEndTime.getText().toString());

        if (start == null || end == null || start.after(end)) {
            Toast.makeText(getActivity(), getString(R.string.startdate_after_enddate)
                    , Toast.LENGTH_LONG).show();
            return false;
        } else if (currentPhotoPath == null && eventId.equals("")) {
            Toast.makeText(getActivity(), getString(R.string.select_title_picture)
                    , Toast.LENGTH_LONG).show();
            return false;
        } else if (spCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(getActivity(), getString(R.string.select_category)
                    , Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * this method transforms dateString and a timeString into a date object
     * @param dateStr contains the date as a string
     * @param timeStr contains the time as a string
     * @return a date object
     */
    private Date getDateOutOfString(String dateStr, String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date_pattern_for_saving));
        Date date = null;
        try {
            date = format.parse(dateStr+timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * starts an intent to select a picture from the gallery
     */
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
            loadImage(true);

        }
    }

    /**
     * Creates and shows the date-picker-dialog
     * @param txtDate editText where the selected date should be displayed
     */
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

                        fillDateToView(dayOfMonth, monthOfYear, year, txtDate);

                    }
                }, year, month, day);
        datePicker.show();
    }

    /**
     * fills the selected time to the ediText
     * @param dayOfMonth selected day
     * @param monthOfYear selected month
     * @param year selected year
     * @param txtDate editText where the selected date should be displayed
     */
    private void fillDateToView(int dayOfMonth, int monthOfYear, int year
            , EditText txtDate) {
        String dayStr = String.valueOf(dayOfMonth);
        String monthStr = String.valueOf(monthOfYear+1);
        if (dayOfMonth < 10) {
            dayStr = "0"+dayOfMonth;
        }
        if ((monthOfYear+1) < 10) {
            monthStr = "0"+(monthOfYear+1);
        }

        String date = dayStr + "." + monthStr + "." + year;
        txtDate.setText(date);
    }

    /**
     * Creates and shows the time-picker-dialog
     * @param txtTime editText where the selected time should be displayed
     */
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

                fillTimeToView(hourOfDay, minute, txtTime);

            }
        }, hour, minute, true);
        timePicker.show();
    }

    /**
     * fills the selected time to the ediText
     * @param hourOfDay selected hour
     * @param minute selected minute
     * @param txtTime editText where the selected time should be displayed
     */
    private void fillTimeToView(int hourOfDay, int minute, EditText txtTime) {
        String hourStr = String.valueOf(hourOfDay);
        String minuteStr = String.valueOf(minute);

        if (hourOfDay < 10) {
            hourStr = "0"+hourOfDay;
        }
        if (minute < 10) {
            minuteStr = "0"+minute;
        }

        String time = hourStr + "." + minuteStr;
        txtTime.setText(time);
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
