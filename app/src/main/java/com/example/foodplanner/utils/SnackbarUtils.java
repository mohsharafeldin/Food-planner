package com.example.foodplanner.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.net.ConnectException;
import java.net.UnknownHostException;

public class SnackbarUtils {

    // Custom Colors
    private static final int COLOR_SUCCESS = Color.parseColor("#4CAF50"); // Green
    private static final int COLOR_ERROR = Color.parseColor("#D32F2F"); // Red
    private static final int COLOR_INFO = Color.parseColor("#2196F3"); // Blue
    private static final int COLOR_TEXT = Color.WHITE;

    public static void showShort(View view, String message) {
        show(view, message, Snackbar.LENGTH_SHORT, COLOR_INFO, com.example.foodplanner.R.drawable.ic_info);
    }

    public static void showLong(View view, String message) {
        show(view, message, Snackbar.LENGTH_LONG, COLOR_INFO, com.example.foodplanner.R.drawable.ic_info);
    }

    public static void showSuccess(View view, String message) {
        show(view, message, Snackbar.LENGTH_SHORT, COLOR_SUCCESS, com.example.foodplanner.R.drawable.ic_success);
    }

    public static void showError(View view, String message) {
        show(view, message, Snackbar.LENGTH_LONG, COLOR_ERROR, com.example.foodplanner.R.drawable.ic_error);
    }

    public static void showInfo(View view, String message) {
        show(view, message, Snackbar.LENGTH_SHORT, COLOR_INFO, com.example.foodplanner.R.drawable.ic_info);
    }

    public static void handleError(View view, Throwable t) {
        String message = "An unexpected error occurred";

        if (t instanceof UnknownHostException || t instanceof ConnectException) {
            message = "Network error. Please check your connection.";
        } else if (t.getMessage() != null) {
            message = t.getMessage();
        }

        showError(view, message);
    }

    private static void show(View view, String message, int duration, int backgroundColor, int iconResId) {
        if (view == null)
            return;

        Snackbar snackbar = Snackbar.make(view, message, duration);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(backgroundColor);

        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(COLOR_TEXT);
        textView.setMaxLines(3); // Allow multiline

        // Add icon
        textView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        textView.setCompoundDrawablePadding(16); // Padding between icon and text

        snackbar.show();
    }
}
