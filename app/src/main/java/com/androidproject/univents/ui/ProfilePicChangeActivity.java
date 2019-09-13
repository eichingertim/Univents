package com.androidproject.univents.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidproject.univents.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity handles the process of changing the profile picture
 */
public class ProfilePicChangeActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private ImageButton btnFromCamera, btnFromGallery;
    private ImageView imgPreview;
    private TextView tvGrantPermission;

    private Toolbar toolbar;

    private ProgressDialog progressDialog;

    private static final int PERMISSIONS_REQUEST_CODE = 201;

    private static final int RESULT_LOAD_IMAGE = 21;
    private static final int REQUEST_TAKE_PHOTO = 20;
    private String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic_change);

        initToolbar();
        initFireBase();
        initProgressDialog();
        initUI();
        showCurrentProfilePic();
        checkStoragePermission();

    }

    /**
     * initializes toolbar as an actionBar and sets the back icon available
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * initializes necessary firebase-tools
     */
    private void initFireBase() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * initializes progress-dialog
     */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading));
        progressDialog.setCancelable(false);
    }

    /**
     * initializes all views from the layout
     */
    private void initUI() {
        btnFromCamera = findViewById(R.id.btn_select_from_camera);
        btnFromCamera.setEnabled(false);
        btnFromCamera.setOnClickListener(this);

        btnFromGallery = findViewById(R.id.btn_select_from_gallery);
        btnFromGallery.setEnabled(false);
        btnFromGallery.setOnClickListener(this);

        imgPreview = findViewById(R.id.current_profile_pic);

        tvGrantPermission = findViewById(R.id.tv_grant_permission);
        tvGrantPermission.setOnClickListener(this);
    }

    /**
     * retrieves the url of the users profile-pic and shows it with PICASSO
     */
    private void showCurrentProfilePic() {
        Uri photoUrl = firebaseUser.getPhotoUrl();
        if (photoUrl != null) {
            Picasso.get().load(photoUrl).noFade().into(imgPreview);
        } else {
            imgPreview.setImageResource(R.color.colorAccent);
        }
    }

    /**
     * checks permission to access storage of device
     */
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        } else {
            disableTvEnableButtons();
        }
    }

    /**
     * sends an intent to camera, to take a picture and saves the picture in a new file
     */
    private void selectImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ignored) {
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getString(R.string.fileprovider_path),
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    private void selectImageFromGallery() {
        loadFromGallery();
    }

    /**
     * creates a new imageFile
     * @return a new file that contains the captured image
     * @throws IOException exception if file not found
     */
    private File createImageFile () throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                timeStamp,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * saves an image to gallery
     * @param bitmap image selected
     */
    private void saveImageToGallery(Bitmap bitmap) {
        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "",
                ""
        );

        Uri savedImageURI = Uri.parse(savedImageURL);
        imgPreview.setImageURI(savedImageURI);

    }


    /**
     * sends an intent to select an image from the gallery
     */
    private void loadFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            saveImageToGallery(bitmap);
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            currentPhotoPath = picturePath;
            imgPreview.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.permssion_granted), Toast.LENGTH_SHORT).show();
                disableTvEnableButtons();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (currentPhotoPath != null) {
                    checkUserWantToSave();
                } else {
                    onBackPressed();
                }
                break;
            case R.id.profile_edit_save:
                if (currentPhotoPath != null) {
                    saveNewProfilePic();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * creates and shows dialog if the user wants to save his profile picture
     */
    private void checkUserWantToSave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfilePicChangeActivity.this);
        builder.setTitle(getString(R.string.save_changes));
        builder.setMessage(getString(R.string.want_to_save_changes));
        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (currentPhotoPath != null) {
                    saveNewProfilePic();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    /**
     * create a storage reference to the firebase storage and put a file to it
     */
    private void saveNewProfilePic() {
        Uri file = Uri.fromFile(new File(currentPhotoPath));
        StorageReference ref = storage.getReference().child("user_profile_pictures/"
                +file.getLastPathSegment());
        UploadTask uploadTask = ref.putFile(file);
        upload(uploadTask, ref);
    }

    /**
     * uploads the selected image to firebase storage.
     * @param uploadTask started upload task for uploading the image
     * @param ref storage reference
     */
    private void upload(UploadTask uploadTask, final StorageReference ref) {
        progressDialog.show();
        Task<Uri> urlTask = (uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        })).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    updateUserProfileUri(downloadUri);
                } else {
                    progressDialog.dismiss();
                }
            }
        });
    }

    /**
     * updates the reference to the new profile picture(firebaseUser and cloud-document)
     * @param downloadUri download URL
     */
    private void updateUserProfileUri(Uri downloadUri) {
        final Uri oldDownloadUri = firebaseUser.getPhotoUrl();
        final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build();

        Map<String, Object> update = new HashMap<>();
        update.put(getString(R.string.KEY_FIREBASE_USER_PICTURE_URL), downloadUri.toString());

        db.collection(getString(R.string.KEY_FIREBASE_COLLECTION_USERS))
                .document(firebaseUser.getUid()).update(update)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    deleteOldProfilePic(oldDownloadUri);
                                } else {
                                    progressDialog.dismiss();
                                }
                            }
                        });
            }
        });
    }

    /**
     * deletes the old user-profile-picture when the user selected a new one
     * @param oldDownloadUri download URL of old picture
     */
    private void deleteOldProfilePic(Uri oldDownloadUri) {
        try {
            StorageReference refOld = storage.getReferenceFromUrl(oldDownloadUri.toString());
            refOld.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), getString(R.string.profile_picture_was_changed)
                            , Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }

    /**
     * checks if dark theme is on
     */
    private void checkTheme() {
        if (isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkTheme);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme);
        }
    }

    private boolean isDarkTheme() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences
                .getBoolean(getString(R.string.PREF_KEY_THEME), false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_from_camera:
                selectImageFromCamera();
                break;
            case R.id.btn_select_from_gallery:
                selectImageFromGallery();
                break;
            case R.id.tv_grant_permission:
                checkStoragePermission();
                break;
        }
    }

    private void disableTvEnableButtons() {
        btnFromGallery.setEnabled(true);
        btnFromCamera.setEnabled(true);
        tvGrantPermission.setVisibility(View.INVISIBLE);
    }
}
