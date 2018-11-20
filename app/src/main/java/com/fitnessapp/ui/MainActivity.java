package com.fitnessapp.ui;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.fitnessapp.Application;
import com.fitnessapp.BaseActivity;
import com.fitnessapp.database.DatabaseHelper;
import com.fitnessapp.util.Constants;
import com.fitnessapp.util.GoogleApiHelper;
import com.fitnessapp.R;
import com.fitnessapp.util.Utils;
import com.fitnessapp.widget.JobSchedulerService;
import com.fitnessapp.widget.MyWidgetProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

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

    public DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        databaseHelper = DatabaseHelper.getInstance(this);
        apiHelper = new GoogleApiHelper(MainActivity.this);
        checkGoalsAndReplaceFragment();

        setUpNavigationDrawer();
        setUpHeaderView();

        startJobscheduler();
    }

    /**
     * This function start job scheduler for periodic update the widget
     * Currently it will refresh widget after every 15 minutes
     */
    private void startJobscheduler() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler mJobScheduler = (JobScheduler)
                    getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder;

            builder = new JobInfo.Builder(1,
                    new ComponentName(getPackageName(),
                            JobSchedulerService.class.getName()));

            builder.setPeriodic(Constants.WidgetRefreshTime);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

            if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
//                Log.e("JobScheduler", "onCreate: Some error while scheduling the job");
            }
        }
    }

    /**
     * This method check that goal is set to profile or not if not than it will force the user to add it
     */
    private void checkGoalsAndReplaceFragment() {

        if (Application.getPrefaceData(Constants.max_calories).isEmpty() || Application.getPrefaceData(Constants.max_steps).isEmpty()) {
            replaceFragment(new ProfileFragment(), R.id.main_frame_layout);
            return;
        }
        replaceFragment(new DailyStepsFragment(), R.id.main_frame_layout);


    }

    public void updateWidget() {
        Intent intent = new Intent(this, MyWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MyWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }


    private void setUpHeaderView() {
        CircleImageView userImage = navView.getHeaderView(0).findViewById(R.id.img_user);
        TextView userName = navView.getHeaderView(0).findViewById(R.id.txt_user_name);

        userName.setText(Application.getPrefaceData(Constants.name));
        if (!Application.getPrefaceData(Constants.img).isEmpty())
            Picasso.get().load(Application.getPrefaceData(Constants.img)).into(userImage);

        navView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkGoalsAndReplaceFragment();
                drawerLayout.closeDrawer(GravityCompat.START);

                deselectNavItems();

            }
        });
    }

    private void deselectNavItems() {
        int size = navView.getMenu().size();
        for (int i = 0; i < size; i++) {
            navView.getMenu().getItem(i).setChecked(false);
        }

    }


    private void setUpNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        item = menu.findItem(R.id.action_refresh);
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

                            callSharingIntent();
                        }

                        @Override
                        public void onDenied(String permission) {

                            Utils.showAlertToast(MainActivity.this, getResources().getString(R.string.msg_storage_permission));
                        }
                    });


        } else if (item.getItemId() == R.id.action_refresh) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            DailyStepsFragment frag = (DailyStepsFragment) fm.findFragmentById(R.id.main_frame_layout);
            frag.getStepData();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method opens all supported application who support image sharing
     */
    private void callSharingIntent() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DailyStepsFragment frag = (DailyStepsFragment) fm.findFragmentById(R.id.main_frame_layout);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_STREAM, Utils.getImageUri(MainActivity.this, Utils.createBitmapFromView(frag.llMain)));
        i.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.checkout_str));
        try {
            startActivity(Intent.createChooser(i, getResources().getString(R.string.share_msg)));
        } catch (android.content.ActivityNotFoundException ex) {

            ex.printStackTrace();
        }
    }

    public void showMenu(boolean b) {
        showSharing = b;
        invalidateOptionsMenu();

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_logout) {
            logout();
        } else if (Application.getPrefaceData(Constants.max_calories).isEmpty() || Application.getPrefaceData(Constants.max_steps).isEmpty()) {
            Utils.showAlertDialog(MainActivity.this, getResources().getString(R.string.alert), getResources().getString(R.string.error_max_steps_calories));
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        if (id == R.id.nav_summary) {
            replaceFragment(new SummaryFragment(), R.id.main_frame_layout);
        } else if (id == R.id.nav_setting) {
            replaceFragment(new ProfileFragment(), R.id.main_frame_layout);
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
            public void onConnected(Bundle bundle) {
                mGoogleApiClientH.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(@NonNull Status status) {
                        mGoogleApiClientH.disconnect();
                        mGoogleApiClientH = null;

                        cancelJobscheduler();
                        databaseHelper.myDao().deleteAll();

                        Application.clearSharedPreferences();
                        updateWidget();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                });
            }

            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });

    }

    private void cancelJobscheduler() {
        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.cancelAll();

        }

    }
}
