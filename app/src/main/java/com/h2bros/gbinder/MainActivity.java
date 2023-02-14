package com.h2bros.gbinder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.h2bros.annotation.BindView;
import com.h2bros.reflection.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.content_tv)
    TextView contentTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        contentTv.setText("GButterKnife");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_frame, new MainFragment())
                .commit();
    }
}