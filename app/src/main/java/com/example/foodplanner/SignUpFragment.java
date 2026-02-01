package com.example.foodplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SignUpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        View loginNavigateBtn = view.findViewById(R.id.tv_login);
        if (loginNavigateBtn != null) {
            loginNavigateBtn
                    .setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_signup_to_login));
        }

        return view;
    }
}
