package com.fitness.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.fitness.Application;
import com.fitness.BaseActivity;
import com.fitness.util.Constants;
import com.fitness.util.GoogleApiHelper;
import com.fitness.R;
import com.fitness.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.main_frame_layout)
    FrameLayout mainFrameLayout;
    private boolean showSharing;
    private GoogleApiHelper apiHelper;
    private GoogleApiClient mGoogleApiClientH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        apiHelper = new GoogleApiHelper(MainActivity.this);
        apiHelper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }

            @Override
            public void onConnectionSuspended(int i) {

            }

            @Override
            public void onConnected(Bundle bundle) {

                Log.e("Connected","API connected from here...");
                if (Application.getPrefranceData(Constants.max_calories).isEmpty() || Application.getPrefranceData(Constants.max_steps).isEmpty()) {
                    replaceFragment(new ProfileFragment(), R.id.main_frame_layout);
                    return;
                }

                replaceFragment(new DailyStepsFragment(), R.id.main_frame_layout);

            }
        });



        setUpNavigationDrawer();
        setUpHeaderView();

    }



    private void setUpHeaderView() {
        CircleImageView userImage = (CircleImageView) navView.getHeaderView(0).findViewById(R.id.img_user);
        TextView userName = (TextView) navView.getHeaderView(0).findViewById(R.id.txt_user_name);

        userName.setText(Application.getPrefranceData(Constants.name));
        Picasso.get().load(Application.getPrefranceData(Constants.img)).into(userImage);

        navView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new DailyStepsFragment(), R.id.main_frame_layout);
                drawerLayout.closeDrawer(GravityCompat.START);

            }
        });
    }

    private class GetGoal extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
//            getUserGoal();
            return null;
        }
    }


    private void setUpNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_share);
        item.setVisible(showSharing);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_share) {

            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultAction() {

                        @Override
                        public void onGranted() {

                            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                            DailyStepsFragment frag = (DailyStepsFragment) fm.findFragmentById(R.id.main_frame_layout);


                            Intent i = new Intent(Intent.ACTION_SEND);

                            i.setType("image/*");
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
    /*compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] bytes = stream.toByteArray();*/


                            i.putExtra(Intent.EXTRA_STREAM, getImageUri(MainActivity.this, Utils.createBitmapFromView(frag.llMain)));
                            i.putExtra(Intent.EXTRA_TEXT, "Checkout my today's steps and calories");
                            try {
                                startActivity(Intent.createChooser(i, "Share today's activity ..."));
                            } catch (android.content.ActivityNotFoundException ex) {

                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void onDenied(String permission) {
                            Toast.makeText(MainActivity.this,
                                    "Sorry, we need the Storage Permission to do that",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });




        }
        return super.onOptionsItemSelected(item);
    }

    public void showMenu(boolean b) {
        showSharing = b;
        invalidateOptionsMenu();

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "tmp", null);
        return Uri.parse(path);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_summary) {
            replaceFragment(new SummaryFragment(), R.id.main_frame_layout);
        } else if (id == R.id.nav_setting) {
            replaceFragment(new ProfileFragment(), R.id.main_frame_layout);
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Logout from account
     */
    private void logout() {
         mGoogleApiClientH = apiHelper.getGoogleApiClient();

       apiHelper.setConnectionListener(new GoogleApiHelper.ConnectionListener() {
           @Override
           public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

           }

           @Override
           public void onConnectionSuspended(int i) {

           }

           @Override
           public void onConnected(Bundle bundle) {
               mGoogleApiClientH.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {

                   @Override
                   public void onResult(Status status) {
                       mGoogleApiClientH.disconnect();
                       mGoogleApiClientH = null;

                       Application.clearSharedPreferences();
                       startActivity(new Intent(MainActivity.this, LoginActivity.class));
                       finish();
                   }
               });
           }
       });

    }
}
