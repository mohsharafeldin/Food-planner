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

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        View loginBtn = view.findViewById(R.id.btn_login);
        View signUpBtn = view.findViewById(R.id.tv_sign_up);

        if (loginBtn != null) {
            loginBtn.setOnClickListener(v -> {
                // Dummy login: set status in session manager and go to main
                android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            });
        }

        if (signUpBtn != null) {
            signUpBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_login_to_signup));
        }

        View guestBtn = view.findViewById(R.id.btn_guest);
        if (guestBtn != null) {
            guestBtn.setOnClickListener(v -> {
                com.example.foodplanner.utils.SessionManager sessionManager = new com.example.foodplanner.utils.SessionManager(
                        requireContext());
                sessionManager.setGuest(true);
                sessionManager.setLoggedIn(false); // Ensure loggedIn is false

                android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            });
        }

        return view;
    }
}