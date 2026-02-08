package com.example.foodplanner.favorites.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.favorites.presenter.FavoritesPresenter;
import com.example.foodplanner.utils.SessionManager;

import java.util.List;

public class FavoritesFragment extends Fragment implements FavoritesView,
        FavoriteAdapter.OnFavoriteClickListener {

    private RecyclerView rvFavorites;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private FavoritesPresenter presenter;
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
            sessionManager = new SessionManager(requireContext());
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

        sessionManager = new SessionManager(requireContext());
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
        presenter = new FavoritesPresenter(repository, currentUserId);
        presenter.attachView(this);
    }

    private void setupSearch() {
        // Toggle Search Visibility
        btnSearch.setOnClickListener(v -> {
            if (sessionManager.isGuest()) {
                Toast.makeText(requireContext(), "Please login to search favorites", Toast.LENGTH_SHORT).show();
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
                // Get the favorite and remove it
                // For now, let's reload the data
                // We need to get the actual item from adapter to remove it via presenter
                // Since adapter doesn't expose list easily, we rely on presenter's
                // loadFavorites
                // But swipe needs the item.
                // Let's assume implementing swipe properly requires adapter.getItem(position)
                // which might not exist.
                // The original code passed 'presenter.loadFavorites()' on swipe, which implies
                // it didn't actually delete it via presenter properly using the swiped item?
                // Wait, the original code had:
                // onSwiped { presenter.loadFavorites(); }
                // This means swipe did NOTHING to the DB. It just reloaded.
                // That looks like a bug or incomplete feature in the original code.
                // I won't fix unrelated bugs unless necessary, but this might be confusing.
                // I will leave it as is for now to avoid scope creep, or just call
                // loadFavorites as before.
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
        Toast.makeText(requireContext(), R.string.favorite_removed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
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
