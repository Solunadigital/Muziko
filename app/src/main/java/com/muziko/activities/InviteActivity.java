package com.muziko.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;
import com.jaredrummler.android.device.DeviceName;
import com.muziko.R;
import com.muziko.billing.Premium;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.firebase.PromoCode;
import com.muziko.common.models.firebase.Registration;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.PrefsManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.muziko.manager.MuzikoConstants.googlePlayURL;

public class InviteActivity extends BaseActivity implements View.OnClickListener, Premium.onUpdatedListener {
    private final WeakHandler handler = new WeakHandler();
    private Toolbar toolbar;
    private ImageView qrcodeimage;
    private TextView invited;
    private TextView inviteExplain;
    private TextView progresstest;
    private ProgressBar progressBar;
    private Button shareButton;
    private Button claimButton;
    private Bitmap qrbitmap;
    private String sharePath;
    private ImageButton infoButton;
    private String androidid;
    private DatabaseReference claimRef;
    private DatabaseReference regoRef;
    private ValueEventListener claimListener;
    private ValueEventListener regoListener;
    private Premium premium;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStartActivity(false);
        setContentView(R.layout.activity_invite);
        findViewsById();

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Invite & Earn");

        shareButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        claimButton.setOnClickListener(this);

        progressBar.setMax(50);

        androidid = AppController.Instance().getAndroidID();

        if (androidid.length() > 30) {
            androidid = androidid.substring(0, 30);
        }
        sharePath = googlePlayURL + androidid;

        premium = new Premium(InviteActivity.this, this);

        invited.setText(String.format("You invited %d friend%s", PrefsManager.Instance().getInviteCount(), PrefsManager.Instance().getInviteCount() != 1 ? "s" : ""));
        progresstest.setText(PrefsManager.Instance().getInviteCount() * 10 + "XP");
        progressBar.setProgress(PrefsManager.Instance().getInviteCount());

        populateInviteCount();

        Resources resources = getResources();

// Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(sharePath, null, QRGContents.Type.TEXT, Utils.toPixels(resources, 160));
        try {
            // Getting QR-Code as Bitmap
            qrbitmap = qrgEncoder.encodeAsBitmap();

        } catch (WriterException e) {
            Crashlytics.logException(e);
        }

        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, Utils.toPixels(resources, 30), Utils.toPixels(resources, 30), false);

        Bitmap qrcode = overlay(qrbitmap, scaledLogo);

        qrcodeimage.setImageBitmap(qrcode);

//		populatePromoCodes();

        premium.initBillProcessor();

        DatabaseReference regoRef = FirebaseManager.Instance().getRegistrationsRef().child(androidid);

        Registration registration = new Registration(androidid, androidid, DeviceName.getDeviceName(), ServerValue.TIMESTAMP);

        regoRef.child(androidid).setValue(registration, (error, firebase) -> {
            if (error != null) {
//					Utils.toast(InviteActivity.this, "Network connection failed");
            }
        });
    }

    @Override
    public void onDestroy() {
        if (premium != null)
            premium.destroy();

        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (claimRef != null && claimListener != null) {
            claimRef.removeEventListener(claimListener);
        }
        if (regoRef != null && regoListener != null) {
            regoRef.removeEventListener(regoListener);
        }

        super.onDestroy();
    }

    @Override
    public void onPremiumChanged() {

        populateInviteCount();
        EventBus.getDefault().post(new RefreshEvent(1000));
        sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));
    }

    @Override
    public void onPurchased() {

        populateInviteCount();
        EventBus.getDefault().post(new RefreshEvent(1000));
        sendBroadcast(new Intent(AppController.INTENT_PREMIUM_CHANGED));
    }

//	private void populatePromoCodes() {
//
//		InputStreamReader is = null;
//		final DatabaseReference promoRef = FirebaseUtil.getPromosRef();
//
//		try {
//			is = new InputStreamReader(getAssets().open("promo.csv"));
//			BufferedReader reader = new BufferedReader(is);
//			reader.readLine();
//			String line;
//			while ((line = reader.readLine()) != null) {
//				PromoCode promoCode = new PromoCode(line, "", 0);
//				promoRef.child(line).setValue(promoCode, new DatabaseReference.CompletionListener() {
//					@Override
//					public void onComplete(DatabaseError error, DatabaseReference firebase) {
//						if (error != null) {
//
//						}
//					}
//				});
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

    private void findViewsById() {

        toolbar = findViewById(R.id.toolbar);
        qrcodeimage = findViewById(R.id.qrcodeimage);
        invited = findViewById(R.id.invited);
        inviteExplain = findViewById(R.id.inviteExplain);
        progresstest = findViewById(R.id.progresstest);
        progressBar = findViewById(R.id.progressBar);
        shareButton = findViewById(R.id.shareButton);
        claimButton = findViewById(R.id.claimButton);
        infoButton = findViewById(R.id.infoButton);
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Resources resources = getResources();

        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, Utils.toPixels(resources, 65), Utils.toPixels(resources, 65), null);
        return bmOverlay;
    }

    private void claimPromoCode() {

        new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Claim").content("Your one time promotion code will be copied to the clipboard. Paste it into the Google Play purchase window that will be launched when you click OK. If you don't purchase now you can always come back and use your code another time.").positiveText("Ok").onPositive((dialog, which) -> {


            claimRef = FirebaseManager.Instance().getPromosRef();

            claimListener = new ValueEventListener() {

                String myPromoCode;
                ArrayList<PromoCode> promocodeList = new ArrayList<>();

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        PromoCode promoCode = postSnapshot.getValue(PromoCode.class);

                        // find existing promo code for this device
                        if (!Utils.isEmptyString(promoCode.getAndroidID())) {
                            if (promoCode.getAndroidID().equals(androidid)) {
                                myPromoCode = promoCode.getPromoCode();
                            }
                        } else {

                            // build list of unused promo codes
                            promocodeList.add(promoCode);
                        }
                    }

                    // check if we had an existing promo code for this device - if not assign one from list
                    if (myPromoCode == null) {
                        for (int i = 0; i < promocodeList.size(); i++) {
                            myPromoCode = promocodeList.get(i).getPromoCode();
                            if (myPromoCode != null) {
                                break;
                            }
                        }

                        if (myPromoCode != null) {
                            //copy to clipboard

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Promo Code", myPromoCode);
                            clipboard.setPrimaryClip(clip);

                            // mark as used
                            final DatabaseReference myPromoRef = FirebaseManager.Instance().getPromosRef().child(myPromoCode);
                            PromoCode savedPromoCode = new PromoCode(myPromoCode, androidid, ServerValue.TIMESTAMP);
                            Map<String, PromoCode> promotion = new HashMap<>();
                            promotion.put(myPromoCode, savedPromoCode);
                            myPromoRef.setValue(promotion, (error, firebase) -> {
                                if (error != null) {

                                } else {
                                    premium.buyPremium();
                                }
                            });
                        }
                    } else {
                        //copy to clipboard

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Promo Code", myPromoCode);
                        clipboard.setPrimaryClip(clip);
                        AppController.toast(InviteActivity.this, "Promo code copied");
                        premium.buyPremium();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    AppController.toast(InviteActivity.this, "Problem connecting to database");
                }
            };

            claimRef.addValueEventListener(claimListener);

        }).negativeText("Cancel").show();


    }

    private void populateInviteCount() {


        regoRef = FirebaseManager.Instance().getRegistrationsRef().child(androidid);

        regoListener = new ValueEventListener() {

            int regoCount = 0;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                regoCount = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Registration registration = postSnapshot.getValue(Registration.class);
//					Utils.toast(InviteActivity.this, "User with Android ID " + registration.getAndroidId() + " registered");
                    if (registration != null) {
                        if (!registration.getAndroidId().equals(androidid)) {
                            regoCount++;
                        }
                    }
                }

                PrefsManager.Instance().setInviteCount(regoCount);

                progresstest.setText(PrefsManager.Instance().getInviteCount() * 10 + "XP");
                progressBar.setProgress(PrefsManager.Instance().getInviteCount());

                if (PrefsManager.Instance().getInviteCount() >= MuzikoConstants.invitesRequired && !PrefsManager.Instance().getPremium()) {
                    invited.setVisibility(View.GONE);
                    infoButton.setVisibility(View.GONE);
                    claimButton.setVisibility(View.VISIBLE);
                    PrefsManager.Instance().setEarnedPremium(true);
                    EventBus.getDefault().post(new RefreshEvent(1000));
                } else {
                    invited.setVisibility(View.VISIBLE);
                    invited.setText(String.format("You invited %d friend%s", PrefsManager.Instance().getInviteCount(), PrefsManager.Instance().getInviteCount() != 1 ? "s" : ""));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                AppController.toast(InviteActivity.this, "Problem connecting to database");

                progresstest.setText(PrefsManager.Instance().getInviteCount() * 10 + "XP");
                progressBar.setProgress(PrefsManager.Instance().getInviteCount());

                if (PrefsManager.Instance().getInviteCount() >= MuzikoConstants.invitesRequired && !PrefsManager.Instance().getPremium()) {
                    invited.setVisibility(View.GONE);
                    infoButton.setVisibility(View.GONE);
                    claimButton.setVisibility(View.VISIBLE);
                } else {
                    invited.setVisibility(View.VISIBLE);
                    invited.setText(String.format("You invited %d friend%s", PrefsManager.Instance().getInviteCount(), PrefsManager.Instance().getInviteCount() != 1 ? "s" : ""));
                }
            }
        };

        regoRef.addValueEventListener(regoListener);

    }

    @Override
    public void onClick(View v) {

        if (v == shareButton) {

            String share = "Download Muziko Music Player at " + sharePath;

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, share);
            Intent openin = Intent.createChooser(intent, "Share To...");
            startActivity(openin);
        } else if (v == infoButton) {

            new MaterialDialog.Builder(this).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Remove Ads").content("Remove ads by inviting 50 new friends").negativeText("Close").show();
        } else if (v == claimButton) {

            claimPromoCode();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.invite_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.invite_save:

                qrcodeimage.setDrawingCacheEnabled(true);
                Bitmap qrcode = qrcodeimage.getDrawingCache();
                MediaStore.Images.Media.insertImage(getContentResolver(), qrcode, "Muziko QR Code", "Muziko QR Code to share with friends");
                AppController.toast(this, "QR code saved to gallery");
                return true;

//			case R.id.player_mediascan:
//				MyApplication.scanMedia(this, coordinatorlayout);
//				return true;


            case R.id.player_share:
                AppController.Instance().shareApp();
                return true;

            case R.id.player_play_songs:
                AppController.Instance().playAll();
                return true;

            case R.id.player_exit:
                AppController.Instance().exit();

                return true;
            default:
                return false;   //super.onOptionsItemSelected(item);
        }
    }
}
