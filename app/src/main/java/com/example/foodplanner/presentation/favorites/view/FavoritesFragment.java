package com.example.foodplanner.presentation.favorites.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.example.foodplanner.utils.SnackbarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.repositry.MealRepository;
import com.example.foodplanner.presentation.favorites.presenter.FavoritesPresenterContract;
import com.example.foodplanner.presentation.favorites.presenter.FavoritesPresenterImpl;
import com.example.foodplanner.utils.SessionManager;

import java.util.List;

public class FavoritesFragment extends Fragment implements FavoritesView,
        FavoriteAdapter.OnFavoriteClickListener {

    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private FavoritesPresenterContract presenter;
    private FavoriteAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    private android.widget.EditText etSearch;
    private android.widget.ImageButton btnSearch;
    private android.widget.TextView tvTitle;
    private io.reactivex.rxjava3.disposables.CompositeDisposable disposables = new io.reactivex.rxjava3.disposables.CompositeDisposable();
    private io.reactivex.rxjava3.subjects.PublishSubject<String> searchSubject = io.reactivex.rxjava3.subjects.PublishSubject
            .create();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        if (sessionManager.isGuest()) {
            // return; // Don't return, let UI initialize under overlay
        }

        initPresenter();
        setupAdapter();
        setupSwipeToDelete();
        setupSearch();
    }

    private String currentUserId;

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager == null) {
            // Should be initialized in initViews called from onViewCreated
            // If null here, re-initialize
            sessionManager = SessionManager.getInstance();
            currentUserId = sessionManager.getUserId();
        }

        if (sessionManager.isGuest()) {
            showEmptyFavorites();
            return;
        }

        // Check if user has changed
        String newUserId = sessionManager.getUserId();
        if (currentUserId == null || !newUserId.equals(currentUserId)) {
            if (presenter != null) {
                presenter.detachView();
            }
            currentUserId = newUserId;
            initPresenter();
        }

        if (presenter != null) {
            presenter.loadFavorites();
        }
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rv_favorites);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        etSearch = view.findViewById(R.id.et_search_favorites);
        btnSearch = view.findViewById(R.id.btn_search);
        tvTitle = view.findViewById(R.id.tv_title);

        sessionManager = SessionManager.getInstance();
        currentUserId = sessionManager.getUserId();

        rvFavorites.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        // Guest Mode Logic
        if (sessionManager.isGuest()) {
            View guestLayout = view.findViewById(R.id.layout_guest_mode);
            if (guestLayout != null) {
                guestLayout.setVisibility(View.VISIBLE);
                guestLayout.findViewById(R.id.btn_guest_login).setOnClickListener(v -> {
                    sessionManager.logout();
                    Navigation.findNavController(view).navigate(R.id.action_favorites_to_auth);
                });
            }
            // Hide main content
            if (rvFavorites != null)
                rvFavorites.setVisibility(View.GONE);
            if (layoutEmpty != null)
                layoutEmpty.setVisibility(View.GONE);
            if (btnSearch != null)
                btnSearch.setVisibility(View.GONE);
            if (tvTitle != null)
                tvTitle.setText(R.string.guest_mode_title);
        }
    }

    // ... setupSearch ...

    // ... hideKeyboard ...

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        // Use currentUserId which is updated
        presenter = new FavoritesPresenterImpl(repository, currentUserId);
        presenter.attachView(this);
    }

    private void setupSearch() {
        // Toggle Search Visibility
        btnSearch.setOnClickListener(v -> {
            if (sessionManager.isGuest()) {
                if (getView() != null) {
                    SnackbarUtils.showInfo(getView(), "Please login to search favorites");
                }
                return;
            }
            if (etSearch.getVisibility() == View.VISIBLE) {
                // Close Search
                etSearch.setVisibility(View.GONE);
                tvTitle.setVisibility(View.VISIBLE);
                etSearch.setText("");
                btnSearch.setImageResource(R.drawable.ic_search);
                hideKeyboard();
            } else {
                // Open Search
                etSearch.setVisibility(View.VISIBLE);
                tvTitle.setVisibility(View.GONE);
                etSearch.requestFocus();
                btnSearch.setImageResource(R.drawable.ic_close);
                showKeyboard();
            }
        });

        // Search Text Watcher
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // Debounce setup
        disposables.add(
                searchSubject
                        .debounce(300, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(query -> {
                            presenter.search(query);
                        }));
    }

    private void showKeyboard() {
        if (etSearch.requestFocus()) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private void setupAdapter() {
        adapter = new FavoriteAdapter(requireContext(), this);
        rvFavorites.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                presenter.loadFavorites();
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvFavorites);
    }

    @Override
    public void showFavorites(List<FavoriteMeal> favorites) {
        layoutEmpty.setVisibility(View.GONE);
        rvFavorites.setVisibility(View.VISIBLE);
        adapter.setFavorites(favorites);
    }

    @Override
    public void showEmptyFavorites() {
        rvFavorites.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFavoriteRemoved() {
        if (getView() != null) {
            SnackbarUtils.showSuccess(getView(), getString(R.string.favorite_removed));
        }
        presenter.loadFavorites(); // Reload list to update view
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

    @Override
    public void onFavoriteClick(FavoriteMeal favorite) {
        Bundle bundle = new Bundle();
        bundle.putString("mealId", favorite.getIdMeal());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_favorites_to_mealDetails, bundle);
    }

    @Override
    public void onRemoveFavorite(FavoriteMeal favorite) {
        presenter.removeFavorite(favorite);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
        disposables.clear();
    }
}
