package com.muziko.fragments.Register;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.muziko.R;
import com.muziko.controls.IntlPhoneInput.IntlPhoneInput;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dev on 2/11/2016.
 */

public class RegisterStepTwo extends Fragment implements ISlidePolicy, View.OnClickListener, IntlPhoneInput.IntlPhoneInputListener {

    private String TAG = RegisterStepTwo.class.getName();
    private Button authButton;
    private TextView introText;
    private boolean canProceed = true;
    private IntlPhoneInput intlPhoneInput;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private MaterialDialog progressdialog;
    private MaterialDialog otpDialog;

    @Override
    public boolean isPolicyRespected() {
        return canProceed;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register_step_two, container, false);

        intlPhoneInput = rootView.findViewById(R.id.intlPhoneInput);
        introText = rootView.findViewById(R.id.introText);
        authButton = rootView.findViewById(R.id.auth_button);
        authButton.setOnClickListener(this);
        authButton.setEnabled(false);

        intlPhoneInput.setOnValidityChange(this);
        intlPhoneInput.setOnKeyboardDone(this);

        updateUI();

        return rootView;
    }

    public void updateUI() {
        if (FirebaseManager.Instance().getFirebaseMe() != null && FirebaseManager.Instance().getFirebaseMe().getPhone() != null && !FirebaseManager.Instance().getFirebaseMe().getPhone().isEmpty()) {
            String phone = FirebaseManager.Instance().getFirebaseMe().getPhone();
            intlPhoneInput.setNumber(phone);
            authButton.setEnabled(true);
            authButton.setText("CHANGE MOBILE PHONE");
        } else {
            intlPhoneInput.setDefault();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.auth_button:
                if (intlPhoneInput.getNumber() != null) {
                    progressdialog = new MaterialDialog.Builder(getActivity())
                            .title("Verifying phone number")
                            .content("Please wait...")
                            .progress(true, 0)
                            .build();

                    progressdialog.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(intlPhoneInput.getNumber(), 60, TimeUnit.SECONDS, getActivity(),
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                    progressdialog.dismiss();
                                    if (otpDialog != null) {
                                        otpDialog.dismiss();
                                    }
                                    if (!getActivity().isFinishing()) {
                                        new MaterialDialog.Builder(getActivity())
                                                .title("Phone Verification Success")
                                                .content("Verification successful for " + intlPhoneInput.getNumber())
                                                .negativeText(R.string.ok)
                                                .show();
                                    }

                                    intlPhoneInput.setEnabled(false);
                                    introText.setVisibility(View.INVISIBLE);
                                    authButton.setVisibility(View.INVISIBLE);

                                    Map<String, Object> updateValues = new HashMap<>();
                                    updateValues.put("phone", intlPhoneInput.getNumber());

                                    FirebaseManager.Instance().getPeopleRef().child(FirebaseManager.Instance().getCurrentUserId()).updateChildren(
                                            updateValues,
                                            (firebaseError, databaseReference) -> {
                                                if (firebaseError != null) {
                                                    AppController.toast(getActivity(), "Couldn't save user data: " + firebaseError.getMessage());
                                                }
                                            });
                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {
                                    progressdialog.dismiss();

                                    if (!getActivity().isFinishing()) {
                                        new MaterialDialog.Builder(getActivity())
                                                .title("Phone Verification Success")
                                                .content("Verification failed for " + intlPhoneInput.getNumber())
                                                .negativeText(R.string.ok)
                                                .show();
                                    }
                                }

                                @Override
                                public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(verificationId, forceResendingToken);
                                    progressdialog.dismiss();

                                    mVerificationId = verificationId;
                                    mResendToken = forceResendingToken;

                                    otpDialog = new MaterialDialog.Builder(getActivity())
                                            .title("OTP Verification")
                                            .content("Please enter the OTP code")
                                            .autoDismiss(false)
                                            .inputType(InputType.TYPE_CLASS_NUMBER)
                                            .input("Enter Code", "", (dialog, input) -> {

                                                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, input.toString());
                                                FirebaseAuth.getInstance().signInWithCredential(credential)
                                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressdialog.dismiss();
                                                                    if (otpDialog != null) {
                                                                        otpDialog.dismiss();
                                                                    }
                                                                    if (!getActivity().isFinishing()) {
                                                                        new MaterialDialog.Builder(getActivity())
                                                                                .title("Phone Verification Success")
                                                                                .content("Verification successful for " + intlPhoneInput.getNumber())
                                                                                .negativeText(R.string.ok)
                                                                                .show();
                                                                    }

                                                                    intlPhoneInput.setEnabled(false);
                                                                    introText.setVisibility(View.INVISIBLE);
                                                                    authButton.setVisibility(View.INVISIBLE);

                                                                    Map<String, Object> updateValues = new HashMap<>();
                                                                    updateValues.put("phone", intlPhoneInput.getNumber());

                                                                    FirebaseManager.Instance().getPeopleRef().child(FirebaseManager.Instance().getCurrentUserId()).updateChildren(
                                                                            updateValues,
                                                                            (firebaseError, databaseReference) -> {
                                                                                if (firebaseError != null) {
                                                                                    AppController.toast(getActivity(), "Couldn't save user data: " + firebaseError.getMessage());
                                                                                }
                                                                            });
                                                                } else {
                                                                    Toast.makeText(getActivity(), "Verification failed", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                            /*    if (input.toString().equals(mVerificationId)) {
                                                    dialog.dismiss();
                                                    if (!getActivity().isFinishing()) {
                                                        new MaterialDialog.Builder(getActivity())
                                                                .title("Phone Verification Success")
                                                                .content("Verification successful for " + intlPhoneInput.getNumber())
                                                                .negativeText(R.string.ok)
                                                                .show();
                                                    }

                                                    intlPhoneInput.setEnabled(false);
                                                    introText.setVisibility(View.INVISIBLE);
                                                    authButton.setVisibility(View.INVISIBLE);

                                                    Map<String, Object> updateValues = new HashMap<>();
                                                    updateValues.put("phone", intlPhoneInput.getNumber());

                                                    FirebaseManager.Instance().getPeopleRef().child(FirebaseManager.Instance().getCurrentUserId()).updateChildren(
                                                            updateValues,
                                                            (firebaseError, databaseReference) -> {
                                                                if (firebaseError != null) {
                                                                    AppController.toast(getActivity(), "Couldn't save user data: " + firebaseError.getMessage());
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(getActivity(), "Verification failed", Toast.LENGTH_LONG).show();
                                                }*/
                                            }).build();
                                    otpDialog.show();
                                }
                            });
                }
                break;
        }
    }

    @Override
    public void done(View view, boolean isValid) {

        if (isValid) {
            authButton.setEnabled(true);
        }
    }


}
