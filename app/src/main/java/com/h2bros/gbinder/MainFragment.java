package com.h2bros.gbinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.h2bros.annotation.BindView;
import com.h2bros.annotation.OnClick;
import com.h2bros.gbinder.gcore.SecondActivity;
import com.h2bros.reflection.ButterKnife;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    @BindView(R.id.data_rv)
    RecyclerView dataRv;

    @OnClick(R.id.next_btn)
    void showNextActivity(Button nextBtn) {
        Intent intent = new Intent(getActivity(), SecondActivity.class);
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        setupView();
        return view;
    }

    private void setupView() {
        List<String> data = new ArrayList<>();
        data.add("Hihi");
        data.add("Hohi");
        data.add("Hiha");
        MainAdapter adapter = new MainAdapter(data);
        dataRv.setAdapter(adapter);
    }
}
