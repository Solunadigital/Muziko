package com.muziko.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.ImagePickerSheetView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.muziko.R;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.models.firebase.Person;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.tasks.ProfilePhotoUploader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;
import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;
import static com.muziko.manager.MuzikoConstants.RC_SIGN_IN;
import static com.muziko.manager.MuzikoConstants.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private final int REQUEST_STORAGE = 0;
    private final int REQUEST_IMAGE_CAPTURE = REQUEST_STORAGE + 1;
    private final int REQUEST_LOAD_IMAGE = REQUEST_IMAGE_CAPTURE + 1;
    private BottomSheetLayout bottomSheetLayout;
    private String TAG = ProfileActivity.class.getName();
    private MenuItem menuItemView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private Button signInButton;
    private Button signoutButton;
    private CircleImageView profileImage;
    private EditText editNickname;
    private EditText editPhone;
    private com.github.clans.fab.FloatingActionButton fab;
    private Uri cameraImageUri = null;
    private Uri newProfileUri = null;
    private ProfilePhotoUploader profilePhotoUploader = null;
    private DatabaseReference peopleRef;
    private ValueEventListener peopleListener;
    private boolean listenInput = true;
    private boolean hasChanged = false;
    private MaterialDialog progress;
    private String lastImageUrl = "";
    private boolean ignoreUpdate = false;
    private Target profileUploadTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            // loading of the bitmap was a success
            if (profilePhotoUploader != null) {
                profilePhotoUploader.cancel(true);
                profilePhotoUploader = null;
            }

            profilePhotoUploader = new ProfilePhotoUploader(ProfileActivity.this, bitmap, newProfileUri);
            profilePhotoUploader.execute();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // loading of the bitmap failed
            // TODO do some action/warning/error message
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewsById();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        bottomSheetLayout.setPeekOnDismiss(true);
        fab.setOnClickListener(this);
        fab.hide(false);
        profileImage.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        signoutButton.setOnClickListener(this);

        editNickname.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {
                if (listenInput) {
                    hasChanged = true;
                    fab.show(true);
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                updateUI(FirebaseAuth.getInstance().getCurrentUser());
                Log.d(TAG, "onAuthStateChanged:signed_in:" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
            // ...
        };

        check();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        } else if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = null;
            if (requestCode == REQUEST_LOAD_IMAGE && data != null) {
                selectedImage = data.getData();
                if (selectedImage == null) {
                    genericError();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Do something with imagePath
                selectedImage = cameraImageUri;
            }

            if (selectedImage != null) {
                showSelectedImage(selectedImage);
            } else {
                genericError();
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (hasChanged) {
            new MaterialDialog.Builder(ProfileActivity.this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Unsaved Changes").content("There are unsaved changes. If you leave they will be lost.").positiveText("OK").onPositive((dialog, which) -> finish()).negativeText("Cancel").show();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_STORAGE: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSheetView();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Sheet is useless without access to external storage :/", Toast.LENGTH_SHORT).show();
                }
            }
            break;

            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    mAuth.addAuthStateListener(mAuthListener);
                } else {
                    // Permission Denied
                    new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).positiveColorRes(R.color.normal_blue).title("Permission not provided").content("Read contacts permission is required to share tracks with friends.").positiveText("OK").onPositive((dialog, which) -> {
                        finish();
                    }).cancelable(false).show();
                }
            }
            break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void signOut() {

        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                status -> updateUI(null));

        FirebaseManager.Instance().setFirebaseMe(null);
        EventBus.getDefault().post(new FirebaseRefreshEvent(1000));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onBoxConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void check() {
        // Here, thisActivity is the current activity
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("Read Contacts");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                Utils.alertNoDismiss(this, getString(R.string.app_name), message, () -> {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(ProfileActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                });

                return;
            }
            ActivityCompat.requestPermissions(ProfileActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;   //super.onOptionsItemSelected(item);

        }
    }

    private void updateUI(final FirebaseUser user) {

        if (user != null) {

            if (user.isAnonymous()) {
                signInButton.setVisibility(View.VISIBLE);
                signoutButton.setVisibility(View.GONE);
            } else {
                signInButton.setVisibility(View.GONE);
                signoutButton.setVisibility(View.VISIBLE);


                peopleRef = FirebaseManager.Instance().getPeopleRef().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                peopleListener = new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Person person = dataSnapshot.getValue(Person.class);

                        if (!ignoreUpdate) {
                            if (person != null) {
                                listenInput = false;
                                editNickname.setText(person.getDisplayName());
                                listenInput = true;
                                if (person.getPhotoUrl() != null) {
                                    if (lastImageUrl != null) {
                                        if (!lastImageUrl.equals(person.getPhotoUrl())) {
                                            Picasso.with(ProfileActivity.this)
                                                    .load(person.getPhotoUrl())
                                                    .error(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
                                                    .placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
                                                    .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                                                    .centerCrop()
                                                    .into(profileImage);
                                            lastImageUrl = person.getPhotoUrl();
                                        }
                                    }
                                }

                            } else {
                                if (user.getDisplayName() != null) {
                                    listenInput = false;
                                    editNickname.setText(user.getDisplayName());
                                    listenInput = true;
                                }
                                if (newProfileUri == null) {
                                    if (user.getPhotoUrl() != null) {
                                        if (!lastImageUrl.equals(user.getPhotoUrl().toString())) {
                                            Picasso.with(ProfileActivity.this)
                                                    .load(user.getPhotoUrl().toString())
                                                    .error(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
                                                    .placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
                                                    .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                                                    .centerCrop()
                                                    .into(profileImage);
                                            lastImageUrl = user.getPhotoUrl().toString();
                                        }
                                    }

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
//						Utils.toast(ProfileActivity.this, "Problem connecting to database");

                    }
                };

                peopleRef.addValueEventListener(peopleListener);
            }
        } else {
            signInButton.setVisibility(View.VISIBLE);
            signoutButton.setVisibility(View.GONE);
            listenInput = false;
            editNickname.setText("");
            listenInput = true;
            profileImage.setImageResource(R.drawable.profile_placeholder);
            fab.hide(true);
        }
    }
    // [END on_start_add_listener]

    private void findViewsById() {
        signInButton = findViewById(R.id.sign_in_button);
        signoutButton = findViewById(R.id.signoutButton);
        profileImage = findViewById(R.id.profileImage);
        editNickname = findViewById(R.id.editNickname);
//		editPhone = (EditText) findViewById(R.id.editPhone);
        fab = findViewById(R.id.fab);
        bottomSheetLayout = findViewById(R.id.bottomsheet);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        progress = new MaterialDialog.Builder(this)
                .title("Logging In")
                .content("Please wait")
                .progress(true, 0)
                .show();


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    FirebaseManager.Instance().startFirebase();

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(ProfileActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    progress.dismiss();
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                status -> updateUI(null));
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.signoutButton:

                new MaterialDialog.Builder(this)
                        .theme(Theme.LIGHT)
                        .titleColorRes(R.color.normal_blue)
                        .negativeColorRes(R.color.dialog_negetive_button)
                        .positiveColorRes(R.color.normal_blue)
                        .title("Sign out?")
                        .content("Are you sure you want to log out?")
                        .positiveText("OK")
                        .onPositive((dialog, which) -> {
                            FirebaseManager.Instance().stopFirebase();
                            signOut();
                        })
                        .negativeText("Cancel")
                        .show();


                break;
            case R.id.fab:
                updateUserProfile(FirebaseAuth.getInstance().getCurrentUser());
                break;
            case R.id.profileImage:
                if (checkNeedsPermission()) {
                    requestStoragePermission();
                } else {
                    showSheetView();
                }
                break;
        }

    }

    private void updateUserProfile(FirebaseUser firebaseUser) {
        fab.hide(true);
        hasChanged = false;
        ignoreUpdate = true;

        Picasso.with(this)
                .load(newProfileUri)
                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                .into(profileUploadTarget);

        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("displayName", editNickname.getText().toString() != null ? editNickname.getText().toString() : firebaseUser.getDisplayName());

        FirebaseManager.Instance().getPeopleRef().child(firebaseUser.getUid()).updateChildren(
                updateValues,
                (firebaseError, databaseReference) -> {
                    if (firebaseError != null) {
                        AppController.toast(ProfileActivity.this, "Couldn't save user data: " + firebaseError.getMessage());
                    } else {
                        AppController.toast(ProfileActivity.this, "Profile saved");
                    }
                });
    }

    private boolean checkNeedsPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        } else {
            // Eh, prompt anyway
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
        }
    }

    /**
     * Show an {@link ImagePickerSheetView}
     */
    private void showSheetView() {
        ImagePickerSheetView sheetView = new ImagePickerSheetView.Builder(this)
                .setMaxItems(30)
                .setShowCameraOption(createCameraIntent() != null)
                .setShowPickerOption(createPickIntent() != null)
                .setImageProvider((imageView, imageUri, size) -> Picasso.with(ProfileActivity.this)
                        .load(imageUri)
                        .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView))
                .setOnTileSelectedListener(selectedTile -> {
                    bottomSheetLayout.dismissSheet();
                    if (selectedTile.isCameraTile()) {
                        dispatchTakePictureIntent();
                    } else if (selectedTile.isPickerTile()) {
                        startActivityForResult(createPickIntent(), REQUEST_LOAD_IMAGE);
                    } else if (selectedTile.isImageTile()) {
                        showSelectedImage(selectedTile.getImageUri());
                    } else {
                        genericError();
                    }
                })
                .setTitle("Choose an image...")
                .create();

        bottomSheetLayout.showWithSheetView(sheetView);
    }

    /**
     * For images captured from the camera, we need to createActivityListener a File first to tell the camera
     * where to store the image.
     *
     * @return the File created for the image to be store under.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraImageUri = Uri.fromFile(imageFile);
        return imageFile;
    }

    /**
     * This checks to see if there is a suitable activity to handle the `ACTION_PICK` intent
     * and returns it if found. {@link Intent#ACTION_PICK} is for picking an image from an external app.
     *
     * @return A prepared intent if found.
     */
    @Nullable
    private Intent createPickIntent() {
        Intent picImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picImageIntent.resolveActivity(getPackageManager()) != null) {
            return picImageIntent;
        } else {
            return null;
        }
    }

    /**
     * This checks to see if there is a suitable activity to handle the {@link MediaStore#ACTION_IMAGE_CAPTURE}
     * intent and returns it if found. {@link MediaStore#ACTION_IMAGE_CAPTURE} is for letting another app take
     * a picture from the camera and store it in a file that we specify.
     *
     * @return A prepared intent if found.
     */
    @Nullable
    private Intent createCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            return takePictureIntent;
        } else {
            return null;
        }
    }

    /**
     * This utility function combines the camera intent creation and image file creation, and
     * ultimately fires the intent.
     *
     * @see {@link #createCameraIntent()}
     * @see {@link #createImageFile()}
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = createCameraIntent();
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent != null) {
            // Create the File where the photo should go
            try {
                File imageFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                // Error occurred while creating the File
                genericError("Could not createActivityListener imageFile for camera");
            }
        }
    }

    private void showSelectedImage(Uri selectedImageUri) {
        hasChanged = true;
        fab.show(true);
        newProfileUri = selectedImageUri;
        profileImage.setImageDrawable(null);

        lastImageUrl = selectedImageUri.toString();

        Picasso.with(ProfileActivity.this)
                .load(selectedImageUri)
                .error(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
                .placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
                .resize(IMAGE_MEDIUM_SIZE, IMAGE_MEDIUM_SIZE)
                .centerCrop()
                .into(profileImage);
    }

    private void genericError() {
        genericError(null);
    }

    private void genericError(String message) {
        Toast.makeText(this, message == null ? "Something went wrong." : message, Toast.LENGTH_SHORT).show();
    }

}
