package com.h2bros.gbinder.gcore;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.h2bros.annotation.BindView;
import com.h2bros.gbinder.R;
import com.h2bros.reflection.ButterKnife;

public class SecondActivity extends AppCompatActivity {

    @BindView(R.id.content_tv)
    TextView contentTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ButterKnife.bind(this);
        contentTv.setText("Huhu");
    }
}