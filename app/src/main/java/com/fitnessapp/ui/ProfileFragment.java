package com.fitnessapp.ui;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.fitnessapp.Application;
import com.fitnessapp.BaseActivity;
import com.fitnessapp.R;
import com.fitnessapp.util.Constants;
import com.fitnessapp.util.GoogleApiHelper;
import com.fitnessapp.util.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    @BindView(R.id.txt_name)
    TextView txtName;
    @BindView(R.id.txt_height)
    TextView txtHeight;
    @BindView(R.id.txt_weight)
    TextView txtWeight;
    @BindView(R.id.img_user)
    CircleImageView imgUser;
    Unbinder unbinder;
    @BindView(R.id.edt_steps)
    EditText edtSteps;
    @BindView(R.id.edt_calories)
    EditText edtCalories;
    @BindView(R.id.frame)
    FrameLayout frame;
    private GoogleApiClient mGoogleApiClient;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);

        getActivity().setTitle(getResources().getString(R.string.profile));

        setValues();
        checkFromMainActivity();

        mGoogleApiClient = new GoogleApiHelper(getActivity()).getGoogleApiClient();
        new GetUserWeightAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ((MainActivity) getActivity()).showMenu(false);

        return view;
    }

    /**
     * This function check that if this fragment called from main activity for the first time,
     * If yes then it will ask for your target calories and target steps
     */
    private void checkFromMainActivity() {
        if (Application.getPrefaceData(Constants.max_calories).isEmpty() || Application.getPrefaceData(Constants.max_steps).isEmpty()) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.showAlertDialog(getActivity(), "Alert", getResources().getString(R.string.error_max_steps_calories));

                }
            }, 500);

        }
    }

    private void setValues() {
        txtName.setText(Application.getPrefaceData(Constants.name));
        if (!Application.getPrefaceData(Constants.img).isEmpty())
            Picasso.get().load(Application.getPrefaceData(Constants.img)).into(imgUser);
        edtCalories.setText(Application.getPrefaceData(Constants.max_calories));
        edtSteps.setText(Application.getPrefaceData(Constants.max_steps));
        txtHeight.setText(Application.getPrefaceData(Constants.height));
        txtWeight.setText(Application.getPrefaceData(Constants.weight));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_save)
    public void onClick() {
        if (edtCalories.getText().toString().isEmpty() || edtSteps.getText().toString().isEmpty()) {
            Utils.showAlertToast(getActivity(), getResources().getString(R.string.error_max_steps_calories));
            return;
        }

        if (Integer.parseInt(edtSteps.getText().toString()) == 0) {
            Utils.showAlertToast(getActivity(), getResources().getString(R.string.error_max_steps_blank));
            edtSteps.requestFocus();
            return;
        } else if (Integer.parseInt(edtCalories.getText().toString()) == 0) {
            Utils.showAlertToast(getActivity(), getResources().getString(R.string.error_max_calories_blank));
            edtCalories.requestFocus();
            return;
        }

        Application.setPreferences(Constants.max_steps, edtSteps.getText().toString());
        Application.setPreferences(Constants.max_calories, edtCalories.getText().toString());


        ((BaseActivity) getActivity()).replaceFragment(new DailyStepsFragment(), R.id.main_frame_layout);
    }


    private class GetUserWeightAsync extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            getUserWeight();
            getUserHeight();
            return null;
        }
    }

    private void getUserWeight() {
        if (mGoogleApiClient == null) {
            Log.e("APICLient", "was null ====");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(1, calendar.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, dataReadRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data


        try {
            if (dataReadResult.getDataSets() != null && dataReadResult.getDataSets().size() > 0) {
                final float weight = dataReadResult.getDataSets().get(0).getDataPoints().get(0).getValue(Field.FIELD_WEIGHT).asFloat();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtWeight.setText(weight + "");
                        Application.setPreferences(Constants.weight, weight + "");
                    }
                });
            }
        } catch (Exception ex) {
            Application.setPreferences(Constants.weight, "");
        }


    }

    private void getUserHeight() {
        Calendar calendar = Calendar.getInstance();
        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEIGHT)
                .setTimeRange(1, calendar.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, dataReadRequest).await(1, TimeUnit.MINUTES);

        //Used for aggregated data

        try {
            if (dataReadResult.getDataSets() != null && dataReadResult.getDataSets().size() > 0) {
                final float height = dataReadResult.getDataSets().get(0).getDataPoints().get(0).getValue(Field.FIELD_HEIGHT).asFloat();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        txtHeight.setText(Utils.getHeightenCm(height) + "");
                        Application.setPreferences(Constants.height, Utils.getHeightenCm(height) + "");

                    }
                });
            }

        } catch (Exception ex) {
            Application.setPreferences(Constants.height, "");
        }

    }
}
