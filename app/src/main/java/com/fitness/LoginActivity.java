package com.fitness;

import android.os.Bundle;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.AnimateGifMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.img)
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        initGoogleApiClient();
    }
}
