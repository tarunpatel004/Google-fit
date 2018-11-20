package com.fitnessapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.fitnessapp.Application;
import com.fitnessapp.BaseActivity;
import com.fitnessapp.util.Constants;
import com.fitnessapp.util.GoogleApiHelper;
import com.fitnessapp.R;
import com.fitnessapp.util.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.Task;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.img)
    ImageView img;
    private static final int GOOGLE_PROFILE_REQ = 107;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Load gif for rotate image
        Ion.with(img)
                .error(R.drawable.logo_fit)
                .animateGif(AnimateGifMode.ANIMATE)
                .load(getResources().getString(R.string.logo_path));
    }

    @OnClick(R.id.btn_login)
    public void onClick() {

        if (Application.getGoogleApiHelper().isConnected()) {
            signIntoPlus();
        } else {
            GoogleApiHelper helper = new GoogleApiHelper(LoginActivity.this);
            helper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Log.e("failed",connectionResult.toString());

                }

                @Override
                public void onConnectionSuspended(int i) {

                }

                @Override
                public void onConnected(Bundle bundle) {

//                    Log.e("Login", "API client is now connected for use");
                    signIntoPlus();
                }
            });

        }

    }

   /* @Override
    protected void onPause() {
        super.onPause();
        Application.getGoogleApiHelper().disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Application.getGoogleApiHelper().connect();
    }*/

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



        if (requestCode == GOOGLE_PROFILE_REQ && resultCode == Activity.RESULT_OK) {
            Application.setPreferencesBoolean(Constants.isLoggedIn, true);

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


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
