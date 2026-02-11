package com.example.foodplanner.presentation.mealdetails.view;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.foodplanner.utils.SnackbarUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodplanner.R;
import com.example.foodplanner.data.model.Meal;
import com.example.foodplanner.repositry.MealRepository;
import com.example.foodplanner.presentation.mealdetails.presenter.MealDetailsPresenterContract;
import com.example.foodplanner.presentation.mealdetails.presenter.MealDetailsPresenterImpl;
import com.example.foodplanner.utils.Constants;
import com.example.foodplanner.utils.SessionManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MealDetailsFragment extends Fragment implements MealDetailsView {

    private ImageView ivMealImage;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageButton btnBack;
    private TextView tvMealName, tvInstructions, tvVideoLabel, tvCategoryBadge, tvTags;
    private ImageButton btnFavorite;
    // private ImageView ivVideoThumbnail; // Removed
    // private ImageButton btnPlayVideo; // Removed
    private YouTubePlayerView youTubePlayerView;
    private LinearLayout layoutVideoSection;
    private MaterialButton btnAddToPlan, btnAddToCalendar;
    private RecyclerView rvIngredients;
    private ProgressBar progressBar;

    private MealDetailsPresenterContract presenter;
    private IngredientAdapter ingredientAdapter;
    private SessionManager sessionManager;
    private boolean isFavorite = false;

    // Permission Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showCalendarDialog();
                } else {
                    // Fallback to Intent if permission denied or manually show snackbar
                    if (getView() != null) {
                        SnackbarUtils.showError(getView(), "Permission denied. Using Calendar App instead.");
                    }
                    showCalendarDialogInternal(true); // true = force intent
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    private String plannedDate;
    private String plannedMealType;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupListeners();
        loadMealDetails();

        // Cache plan selection if coming from Planner
        if (sessionManager.hasPlanSelection()) {
            plannedDate = sessionManager.getSelectedPlanDate();
            plannedMealType = sessionManager.getSelectedMealType();
            // Do not clear here, wait until added or cancelled
        }
    }

    private void initViews(View view) {
        ivMealImage = view.findViewById(R.id.iv_meal_image);
        btnBack = view.findViewById(R.id.btn_back); // Back button
        tvMealName = view.findViewById(R.id.tv_meal_name);
        btnFavorite = view.findViewById(R.id.btn_favorite);
        rvIngredients = view.findViewById(R.id.rv_ingredients);
        progressBar = view.findViewById(R.id.progress_bar);

        // New layout-specific views
        tvCategoryBadge = view.findViewById(R.id.tv_category_badge);
        tvTags = view.findViewById(R.id.tv_tags);
        // rvInstructions = view.findViewById(R.id.rv_instructions); // Removed
        tvInstructions = view.findViewById(R.id.tv_instructions);
        layoutVideoSection = view.findViewById(R.id.layout_video_section);
        // ivVideoThumbnail = view.findViewById(R.id.iv_video_thumbnail); // Removed
        // btnPlayVideo = view.findViewById(R.id.btn_play_video); // Removed
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);
        View cvVideoContainer = view.findViewById(R.id.cv_video_container);
        MaterialButton btnWatchOnYouTube = view.findViewById(R.id.btn_watch_on_youtube);

        btnAddToPlan = view.findViewById(R.id.btn_add_to_plan);
        btnAddToCalendar = view.findViewById(R.id.btn_add_to_calendar);

        // Set these to null as they don't exist in new layout or not used
        collapsingToolbar = null;
        tvVideoLabel = null;

        sessionManager = SessionManager.getInstance();
        currentUserId = sessionManager.getUserId();
        ingredientAdapter = new IngredientAdapter(requireContext());
        if (rvIngredients != null) {
            rvIngredients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            rvIngredients.setAdapter(ingredientAdapter);
        }

        // Guest Mode Logic
        if (sessionManager.isGuest()) {
            if (btnAddToPlan != null) {

            }
        }
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        String userId = sessionManager.getUserId();
        presenter = new MealDetailsPresenterImpl(repository, userId);
        presenter.attachView(this);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                if (sessionManager.isGuest()) {
                    showError("Please login to add favorites");
                    return;
                }
                presenter.toggleFavorite(isFavorite);
            });
        }

        if (btnAddToPlan != null) {
            btnAddToPlan.setOnClickListener(v -> {
                if (sessionManager.isGuest()) {
                    showError("Please login to add to plan");
                    return;
                }

                // Use cached selection if available (preserves behavior across multiple clicks)
                if (plannedDate != null && plannedMealType != null) {
                    presenter.addToPlan(plannedDate, plannedMealType);
                } else {
                    // Fallback to date picker if accessed directly (from Home/Search)
                    showAddToPlanDialog();
                }
            });
        }

        if (btnAddToCalendar != null) {
            btnAddToCalendar.setOnClickListener(v -> {
                if (sessionManager.isGuest()) {
                    showError("Please login to add to calendar");
                    return;
                }
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    showCalendarDialogInternal(false);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR);
                }
            });
        }
    }

    private void showCalendarDialogInternal(boolean forceIntent) {
        if (tvMealName.getText().toString().isEmpty())
            return;
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            String title = tvMealName.getText().toString();
            String description = "Ingredients: " + ingredientAdapter.getIngredientsText();
            long time = calendar.getTimeInMillis();

            if (forceIntent) {
                com.example.foodplanner.utils.CalendarHelper.addMealToCalendar(requireContext(), requireView(), title,
                        description,
                        time);
            } else {
                com.example.foodplanner.utils.CalendarHelper.addEventToCalendarProvider(requireContext(), requireView(),
                        title,
                        description, time);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showCalendarDialog() {
        showCalendarDialogInternal(false);
    }

    private void loadMealDetails() {
        Bundle args = getArguments();
        if (args != null) {
            String mealId = args.getString("mealId");
            if (mealId != null) {
                presenter.loadMealDetails(mealId);
            }
        }
    }

    private void showAddToPlanDialog() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    showMealTypeDialog(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showMealTypeDialog(String date) {
        String[] mealTypes = {
                Constants.MEAL_TYPE_BREAKFAST,
                Constants.MEAL_TYPE_LUNCH,
                Constants.MEAL_TYPE_DINNER
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_meal_type)
                .setItems(mealTypes, (dialog, which) -> {
                    presenter.addToPlan(date, mealTypes[which]);
                })
                .show();
    }

    @Override
    public void showMealDetails(Meal meal) {
        if (meal == null)
            return;

        try {
            if (tvMealName != null) {
                tvMealName.setText(meal.getStrMeal());
            }
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(meal.getStrMeal());
            }
            if (tvCategoryBadge != null) {
                String categoryArea = meal.getStrCategory();
                if (meal.getStrArea() != null && !meal.getStrArea().isEmpty()) {
                    categoryArea += " | " + meal.getStrArea();
                }
                tvCategoryBadge.setText(categoryArea);
            }

            if (tvTags != null) {
                if (meal.getStrTags() != null && !meal.getStrTags().isEmpty()) {
                    // Split tags by comma and add hash
                    String[] tags = meal.getStrTags().split(",");
                    StringBuilder tagsBuilder = new StringBuilder();
                    for (String tag : tags) {
                        tagsBuilder.append("#").append(tag.trim()).append(" ");
                    }
                    tvTags.setText(tagsBuilder.toString().trim());
                    tvTags.setVisibility(View.VISIBLE);
                } else {
                    tvTags.setVisibility(View.GONE);
                }
            }
            if (tvInstructions != null) {
                tvInstructions.setText(meal.getStrInstructions());
            }

            if (ivMealImage != null) {
                Glide.with(this)
                        .load(meal.getStrMealThumb())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_food_logo)
                                .error(R.drawable.ic_food_logo))
                        .into(ivMealImage);
            }

            if (ingredientAdapter != null && meal.getIngredientsList() != null) {
                ingredientAdapter.setIngredients(meal.getIngredientsList());
            }

            // --- Enhanced Video Logic ---
            String videoUrl = meal.getStrYoutube();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                if (layoutVideoSection != null)
                    layoutVideoSection.setVisibility(View.VISIBLE);

                // Determine Video Type
                if (isYouTubeUrl(videoUrl)) {
                    String videoId = meal.getYoutubeVideoId();
                    loadYouTubeInWebView(videoId);
                } else if (isDirectMedia(videoUrl)) {
                    playWithLocalExoPlayer(videoUrl);
                } else {
                    // Generic Web Type
                    openInWebView(videoUrl);
                }

                // Fallback Button Action
                MaterialButton btnWatchOnYouTube = getView().findViewById(R.id.btn_watch_on_youtube);
                if (btnWatchOnYouTube != null) {
                    btnWatchOnYouTube.setVisibility(View.VISIBLE);
                    btnWatchOnYouTube.setOnClickListener(v -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            if (getView() != null) {
                                SnackbarUtils.showError(getView(), "No app found to play video");
                            }
                        }
                    });
                }
            } else if (layoutVideoSection != null) {
                layoutVideoSection.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helper Methods ---

    private boolean isYouTubeUrl(String url) {
        return url != null && (url.contains("youtube.com") || url.contains("youtu.be"));
    }

    private boolean isDirectMedia(String url) {
        if (url == null)
            return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".m3u8") || lower.endsWith(".mpd")
                || lower.endsWith(".webm") || lower.endsWith(".mkv") || lower.endsWith(".avi")
                || lower.endsWith(".mov");
    }

    // ... (rest of helper methods remain the same until we hit Lifecycle)

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
        // Also pause WebView
        WebView webView = getView().findViewById(R.id.webview_video);
        if (webView != null)
            webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // WebView onResume
        WebView webView = getView().findViewById(R.id.webview_video);
        if (webView != null)
            webView.onResume();
        // ExoPlayer auto-resume not strictly forced, let user press play

        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance();
            currentUserId = sessionManager.getUserId();
        }

        // Check if user has changed
        String newUserId = sessionManager.getUserId();
        if (currentUserId == null || !newUserId.equals(currentUserId)) {
            if (presenter != null) {
                presenter.detachView();
            }
            currentUserId = newUserId;
            initPresenter();
            // Reload meal details to update favorite status
            loadMealDetails();
        }
    }

    private String currentUserId;

    @Override
    public void showFavoriteStatus(boolean isFavorite) {
        this.isFavorite = isFavorite;
        updateFavoriteIcon(isFavorite);
    }

    @Override
    public void onFavoriteAdded() {
        isFavorite = true;
        updateFavoriteIcon(true);
        if (getView() != null) {
            SnackbarUtils.showSuccess(getView(), getString(R.string.favorite_added));
        }
    }

    @Override
    public void onFavoriteRemoved() {
        isFavorite = false;
        updateFavoriteIcon(false);
        if (getView() != null) {
            SnackbarUtils.showSuccess(getView(), getString(R.string.favorite_removed));
        }
    }

    private void updateFavoriteIcon(boolean isFav) {
        if (btnFavorite != null) {
            if (isFav) {
                btnFavorite.setImageResource(R.drawable.ic_favorite);
                btnFavorite.setColorFilter(android.graphics.Color.RED);
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                btnFavorite
                        .setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white));
            }
        }
    }

    // ...

    @Override
    public void onAddedToPlan() {
        if (getView() != null) {
            SnackbarUtils.showSuccess(getView(), getString(R.string.meal_added_to_plan));
        }
        sessionManager.clearPlanSelection();
        // Explicitly navigate to PlannerFragment
        Navigation.findNavController(requireView()).navigate(R.id.plannerFragment);
    }

    private void loadYouTubeInWebView(String videoId) {
        if (youTubePlayerView != null)
            youTubePlayerView.setVisibility(View.GONE);
        if (exoPlayerView != null)
            exoPlayerView.setVisibility(View.GONE);

        WebView webView = getView().findViewById(R.id.webview_video);
        if (webView != null) {
            webView.setVisibility(View.VISIBLE);
            if (videoId == null) {
                webView.setVisibility(View.GONE);
                return;
            }

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(true);
            // High-end Desktop UA
            settings.setUserAgentString(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            webView.setWebChromeClient(new WebChromeClient());

            // Using youtube-nocookie.com to bypass domain restrictions
            String html = "<!DOCTYPE html><html style='height:100%;'><body style='margin:0;padding:0;background:black;overflow:hidden;height:100%;'>"
                    +
                    "<iframe width='100%' height='100%' style='border:0;' " +
                    "src='https://www.youtube-nocookie.com/embed/" + videoId + "?autoplay=0&hl=en&rel=0&playsinline=1' "
                    +
                    "allowfullscreen></iframe>" +
                    "</body></html>";

            webView.loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "utf-8", null);
        }
    }

    private void initializeLocalExoPlayer(String url) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(requireContext()).build();
            exoPlayerView = getView().findViewById(R.id.exo_player_view);
            if (exoPlayerView != null) {
                exoPlayerView.setPlayer(exoPlayer);
            }
        }

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        if (exoPlayer != null) {
            exoPlayer.setMediaItem(mediaItem);
            exoPlayer.setPlayWhenReady(false); // Strictly disable auto-play
            exoPlayer.prepare();
        }
    }

    private void playWithLocalExoPlayer(String url) {
        if (youTubePlayerView != null)
            youTubePlayerView.setVisibility(View.GONE);
        WebView webView = getView().findViewById(R.id.webview_video);
        if (webView != null)
            webView.setVisibility(View.GONE);

        exoPlayerView = getView().findViewById(R.id.exo_player_view);
        if (exoPlayerView != null)
            exoPlayerView.setVisibility(View.VISIBLE);

        initializeLocalExoPlayer(url);
    }

    private void openInWebView(String url) {
        // Embed non-YouTube web content in the WebView
        if (youTubePlayerView != null)
            youTubePlayerView.setVisibility(View.GONE);
        if (exoPlayerView != null)
            exoPlayerView.setVisibility(View.GONE);

        WebView webView = getView().findViewById(R.id.webview_video);
        if (webView != null) {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(url);
        }
    }

    @Override
    public void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarUtils.showError(getView(), message);
        }
    }

    @Override
    public void showNetworkError() {
        if (getView() != null) {
            SnackbarUtils.showError(getView(), getString(R.string.network_error));
        }
    }

    // ExoPlayer variables
    private ExoPlayer exoPlayer;
    private PlayerView exoPlayerView;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        presenter.detachView();
    }

}
