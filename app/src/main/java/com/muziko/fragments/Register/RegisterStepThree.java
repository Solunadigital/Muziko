package com.muziko.fragments.Register;

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
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.ImagePickerSheetView;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.muziko.R;
import com.muziko.common.models.firebase.Person;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.tasks.ProfilePhotoUploader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;
import static com.muziko.MyApplication.IMAGE_MEDIUM_SIZE;

/**
 * Created by dev on 2/11/2016.
 */

public class RegisterStepThree extends Fragment implements View.OnClickListener, ISlidePolicy {

	private final int REQUEST_STORAGE = 0;
	private final int REQUEST_IMAGE_CAPTURE = REQUEST_STORAGE + 1;
	private final int REQUEST_LOAD_IMAGE = REQUEST_IMAGE_CAPTURE + 1;
	private BottomSheetLayout bottomSheetLayout;
	private String TAG = RegisterStepThree.class.getName();
	private com.github.clans.fab.FloatingActionButton fab;
	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	private Uri newProfileUri = null;
	private ProfilePhotoUploader profilePhotoUploader = null;
	private CircleImageView profileImage;
	private DatabaseReference peopleRef;
	private ValueEventListener peopleListener;
	private EditText editUsername;
	private Uri cameraImageUri = null;
	private GoogleSignInAccount account;
	private boolean canProceed = true;
	private boolean listenInput = true;
	private boolean hasChanged = false;
	private boolean ignoreUpdate = false;
	private Target profileUploadTarget = new Target() {
		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			// loading of the bitmap was a success
			if (profilePhotoUploader != null) {
				profilePhotoUploader.cancel(true);
				profilePhotoUploader = null;
			}

			profilePhotoUploader = new ProfilePhotoUploader(getActivity(), bitmap, newProfileUri);
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
    public boolean isPolicyRespected() {
        return canProceed;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSheetView();
            } else {
                // Permission denied
                Toast.makeText(getActivity(), "Sheet is useless without access to external storage :/", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_register_step_three, container, false);

        profileImage = rootView.findViewById(R.id.profileImage);
        editUsername = rootView.findViewById(R.id.editNickname);
        bottomSheetLayout = rootView.findViewById(R.id.bottomsheet);
        fab = rootView.findViewById(R.id.fab);
        fab.hide(true);

		bottomSheetLayout.setPeekOnDismiss(true);
		profileImage.setOnClickListener(this);
		fab.setOnClickListener(this);

		editUsername.addTextChangedListener(new TextWatcher() {

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
		mAuth = FirebaseAuth.getInstance();
		mAuthListener = firebaseAuth -> {
			if (FirebaseAuth.getInstance().getCurrentUser() != null) {
				updateUI(FirebaseAuth.getInstance().getCurrentUser());
				Log.d(TAG, "onAuthStateChanged:signed_in:" + FirebaseAuth.getInstance().getCurrentUser().getUid());
			} else {
				// User is signed out
				Log.d(TAG, "onAuthStateChanged:signed_out");
			}
		};

		return rootView;
	}
    // [END on_start_add_listener]

	@Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
	}

	private void updateUI(final FirebaseUser user) {
		if (user != null) {

            peopleRef = FirebaseManager.Instance().getPeopleRef().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            peopleListener = new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {

					Person person = dataSnapshot.getValue(Person.class);
					if (!ignoreUpdate) {
						if (person != null) {
							listenInput = false;
							editUsername.setText(person.getDisplayName());
							listenInput = true;

							Picasso.with(getActivity())
									.load(person.getPhotoUrl())
									.error(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
									.placeholder(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
									.resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
									.centerCrop()
									.into(profileImage);

						}
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					// Getting Post failed, log a message
                    AppController.toast(getActivity(), "Problem connecting to database");

				}
			};

			peopleRef.addValueEventListener(peopleListener);
		}
	}

	private void updateUserProfile(FirebaseUser firebaseUser) {
		hasChanged = false;
		ignoreUpdate = true;
		fab.hide(true);

		Picasso.with(getActivity())
				.load(newProfileUri)
				.resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
				.centerCrop()
				.into(profileUploadTarget);

		Map<String, Object> updateValues = new HashMap<>();
		updateValues.put("displayName", editUsername.getText().toString() != null ? editUsername.getText().toString() : firebaseUser.getDisplayName());

        FirebaseManager.Instance().getPeopleRef().child(firebaseUser.getUid()).updateChildren(
                updateValues,
				(firebaseError, databaseReference) -> {
					if (firebaseError != null) {
                        AppController.toast(getActivity(), "Couldn't save user data: " + firebaseError.getMessage());
                    } else {
                        AppController.toast(getActivity(), "Profile saved");
                    }
				});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.profileImage:
				if (checkNeedsPermission()) {
					requestStoragePermission();
				} else {
					showSheetView();
				}
				break;
			case R.id.fab:
				updateUserProfile(FirebaseAuth.getInstance().getCurrentUser());
				break;
		}
	}

	private boolean checkNeedsPermission() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void requestStoragePermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
		} else {
			// Eh, prompt anyway
			ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
		}
	}

	/**
	 * Show an {@link ImagePickerSheetView}
	 */
	private void showSheetView() {
		ImagePickerSheetView sheetView = new ImagePickerSheetView.Builder(getActivity())
				.setMaxItems(30)
				.setShowCameraOption(createCameraIntent() != null)
				.setShowPickerOption(createPickIntent() != null)
				.setImageProvider((imageView, imageUri, size) -> Picasso.with(getActivity())
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
		if (picImageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
		if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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

		Picasso.with(getActivity())
				.load(selectedImageUri)
				.error(R.drawable.profile_placeholder).error(R.drawable.profile_placeholder)
				.resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
				.centerInside()
				.into(profileImage);

	}

	private void genericError() {
		genericError(null);
	}

	private void genericError(String message) {
		Toast.makeText(getActivity(), message == null ? "Something went wrong." : message, Toast.LENGTH_SHORT).show();
	}
}
