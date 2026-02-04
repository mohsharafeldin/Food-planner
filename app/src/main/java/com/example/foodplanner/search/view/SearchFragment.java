package com.example.foodplanner.search.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.data.model.Meal;
import com.example.foodplanner.data.repository.MealRepository;
import com.example.foodplanner.search.presenter.SearchPresenter;
import com.example.foodplanner.utils.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends Fragment implements SearchView, MealAdapter.OnMealClickListener {

    private AutoCompleteTextView etSearch;
    private TextView chipSearchName, chipSearchCategory, chipSearchCountry, chipSearchIngredient;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    private SearchPresenter presenter;
    private MealAdapter mealAdapter;
    private String currentSearchType = Constants.SEARCH_BY_NAME;

    private PublishSubject<String> searchSubject = PublishSubject.create();
    private Disposable searchDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupAdapters();
        setupListeners();
        setupSearchDebounce();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        chipSearchName = view.findViewById(R.id.chip_search_name);
        chipSearchCategory = view.findViewById(R.id.chip_search_category);
        chipSearchCountry = view.findViewById(R.id.chip_search_country);
        chipSearchIngredient = view.findViewById(R.id.chip_search_ingredient);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        progressBar = view.findViewById(R.id.progress_bar);
        layoutEmpty = view.findViewById(R.id.layout_empty_state);
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        presenter = new SearchPresenter(repository);
        presenter.attachView(this);
    }

    private void setupAdapters() {
        mealAdapter = new MealAdapter(requireContext(), this);
        rvSearchResults.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(mealAdapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSubject.onNext(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        etSearch.setOnItemClickListener((parent, view1, position1, id1) -> {
            performSearch();
        });

        // Search type chip click listeners
        View.OnClickListener chipClickListener = v -> {
            int id = v.getId();

            // Reset all chips to inactive state
            resetChipStyles();

            // Set clicked chip to active state
            ((TextView) v).setBackgroundResource(R.drawable.bg_chip_active);
            ((TextView) v).setTextColor(getResources().getColor(R.color.background_dark, null));

            // Update search type based on selected chip
            if (id == R.id.chip_search_name) {
                currentSearchType = Constants.SEARCH_BY_NAME;
                etSearch.setHint("Search by meal name...");
                etSearch.setAdapter(null); // Clear suggestions
            } else if (id == R.id.chip_search_category) {
                currentSearchType = Constants.SEARCH_BY_CATEGORY;
                etSearch.setHint("Search by category (e.g. Seafood, Beef)...");
                presenter.loadSuggestions(currentSearchType);
            } else if (id == R.id.chip_search_country) {
                currentSearchType = Constants.SEARCH_BY_COUNTRY;
                etSearch.setHint("Search by country (e.g. Italian, Mexican)...");
                presenter.loadSuggestions(currentSearchType);
            } else if (id == R.id.chip_search_ingredient) {
                currentSearchType = Constants.SEARCH_BY_INGREDIENT;
                etSearch.setHint("Search by ingredient (e.g. Chicken, Rice)...");
                presenter.loadSuggestions(currentSearchType);
            }

            // Re-trigger search with new type if there's existing text
            String currentQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            if (!currentQuery.isEmpty()) {
                presenter.search(currentQuery, currentSearchType);
            }
        };

        if (chipSearchName != null) {
            chipSearchName.setOnClickListener(chipClickListener);
        }
        if (chipSearchCategory != null) {
            chipSearchCategory.setOnClickListener(chipClickListener);
        }
        if (chipSearchCountry != null) {
            chipSearchCountry.setOnClickListener(chipClickListener);
        }
        if (chipSearchIngredient != null) {
            chipSearchIngredient.setOnClickListener(chipClickListener);
        }
    }

    private void resetChipStyles() {
        int inactiveColor = getResources().getColor(R.color.text_primary, null);
        if (chipSearchName != null) {
            chipSearchName.setBackgroundResource(R.drawable.bg_chip_inactive);
            chipSearchName.setTextColor(inactiveColor);
        }
        if (chipSearchCategory != null) {
            chipSearchCategory.setBackgroundResource(R.drawable.bg_chip_inactive);
            chipSearchCategory.setTextColor(inactiveColor);
        }
        if (chipSearchCountry != null) {
            chipSearchCountry.setBackgroundResource(R.drawable.bg_chip_inactive);
            chipSearchCountry.setTextColor(inactiveColor);
        }
        if (chipSearchIngredient != null) {
            chipSearchIngredient.setBackgroundResource(R.drawable.bg_chip_inactive);
            chipSearchIngredient.setTextColor(inactiveColor);
        }
    }

    private void setupSearchDebounce() {
        searchDisposable = searchSubject
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(query -> presenter.search(query, currentSearchType));
    }

    private void performSearch() {
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        if (!query.isEmpty()) {
            presenter.search(query, currentSearchType);
        }
    }

    @Override
    public void showSearchResults(List<Meal> meals) {
        layoutEmpty.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        mealAdapter.setMeals(meals);
    }

    @Override
    public void showEmptyResults() {
        rvSearchResults.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
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
    public void showSuggestions(List<String> suggestions) {
        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            etSearch.setAdapter(adapter);
        }
    }

    @Override
    public void onMealClick(String mealId) {
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_search_to_mealDetails, bundle);
    }

    @Override
    public void onFavoriteClick(String mealId) {
        // Navigate to meal details where user can properly add to favorites
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_search_to_mealDetails, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
        if (searchDisposable != null && !searchDisposable.isDisposed()) {
            searchDisposable.dispose();
        }
    }
}
