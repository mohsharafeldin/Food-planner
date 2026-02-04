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
import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.data.repository.MealRepository;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupAdapter();
        setupSwipeToDelete();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadFavorites();
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rv_favorites);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        sessionManager = new SessionManager(requireContext());
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        String userId = sessionManager.getUserId();
        presenter = new FavoritesPresenter(repository, userId);
        presenter.attachView(this);
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
    }
}
