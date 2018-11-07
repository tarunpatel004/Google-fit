package com.fitness.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.fitness.Application;
import com.fitness.BaseActivity;
import com.fitness.util.Constants;
import com.fitness.util.GoogleApiHelper;
import com.fitness.R;
import com.fitness.util.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fitness.BaseActivity.GOOGLE_PROFILE_REQ;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.img)
    ImageView img;
    private String TAG = LoginActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        Ion.with(img)
                .error(R.drawable.logo_fit)
                .animateGif(AnimateGifMode.ANIMATE)
                .load("file:///android_asset/logo_fit.gif");


    }

    @OnClick(R.id.btn_login)
    public void onClick() {

        if (Application.getGoogleApiHelper().isConnected()) {
            Log.e("Login", "API Client is already connected");
            signIntoPlus();
        } else {
            Log.e("Login", "API Client is not connected ....");
            GoogleApiHelper helper = new GoogleApiHelper(LoginActivity.this);
            helper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                }

                @Override
                public void onConnectionSuspended(int i) {

                }

                @Override
                public void onConnected(Bundle bundle) {

                    Log.e("Login", "API client is now connected for use");
                    signIntoPlus();
                }
            });

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        Application.getGoogleApiHelper().disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Application.getGoogleApiHelper().connect();
    }

    private void signIntoPlus() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_PROFILE_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult: ===");


        if (requestCode == GOOGLE_PROFILE_REQ && resultCode == Activity.RESULT_OK) {
            Application.setPreferencesBoolean("isLoggedIn", true);

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            Log.e("onActivityResult", task.toString());

            if (task != null) {
                Application.setPreferences(Constants.name, task.getResult().getDisplayName());
                if (task.getResult().getPhotoUrl() != null)
                    Application.setPreferences(Constants.img, task.getResult().getPhotoUrl().toString());
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();

            } else {
                Utils.showAlertToast(this, getResources().getString(R.string.error_fetch_profile));
            }
        }
    }


}
