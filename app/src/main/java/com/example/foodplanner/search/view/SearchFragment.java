package com.example.foodplanner.search.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.foodplanner.R;
import com.example.foodplanner.data.meal.model.Meal;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.search.presenter.SearchPresenter;
import com.example.foodplanner.utils.Constants;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class SearchFragment extends Fragment implements SearchView, MealAdapter.OnMealClickListener {

    private AppCompatAutoCompleteTextView etSearch;
    private RecyclerView rvSearchResults;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private TextView chipSearchName, chipSearchCategory, chipSearchCountry, chipSearchIngredient;

    private SearchPresenter presenter;
    private MealAdapter adapter;
    private String currentSearchType = Constants.SEARCH_BY_NAME;

    private final PublishSubject<String> searchSubject = PublishSubject.create();
    private final CompositeDisposable disposables = new CompositeDisposable();

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
        initAdapter();
        setupSearchDebounce();
        setupChipListeners();
        setupSearchAction();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        chipSearchName = view.findViewById(R.id.chip_search_name);
        chipSearchCategory = view.findViewById(R.id.chip_search_category);
        chipSearchCountry = view.findViewById(R.id.chip_search_country);
        chipSearchIngredient = view.findViewById(R.id.chip_search_ingredient);
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        presenter = new SearchPresenter(repository);
        presenter.attachView(this);
    }

    private void initAdapter() {
        adapter = new MealAdapter(requireContext(), this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(adapter);
    }

    private void setupSearchDebounce() {
        // Add TextWatcher to emit search queries
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

        // Debounce search input with 300ms delay
        disposables.add(
                searchSubject
                        .debounce(300, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(query -> {
                            if (!query.isEmpty()) {
                                presenter.search(query, currentSearchType);
                            } else {
                                showEmptyResults();
                            }
                        }));
    }

    private void setupSearchAction() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
                if (!query.isEmpty()) {
                    presenter.search(query, currentSearchType);
                }
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void hideKeyboard() {
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            android.view.inputmethod.InputMethodManager inputManager = (android.view.inputmethod.InputMethodManager) getActivity()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setupChipListeners() {
        chipSearchName.setOnClickListener(v -> selectSearchType(Constants.SEARCH_BY_NAME));
        chipSearchCategory.setOnClickListener(v -> selectSearchType(Constants.SEARCH_BY_CATEGORY));
        chipSearchCountry.setOnClickListener(v -> selectSearchType(Constants.SEARCH_BY_COUNTRY));
        chipSearchIngredient.setOnClickListener(v -> selectSearchType(Constants.SEARCH_BY_INGREDIENT));
    }

    private void selectSearchType(String searchType) {
        currentSearchType = searchType;
        updateChipStyles();

        // Load suggestions for the selected type
        presenter.loadSuggestions(searchType);

        // Re-search if there's existing text
        String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        if (!query.isEmpty()) {
            presenter.search(query, currentSearchType);
        }
    }

    private void updateChipStyles() {
        // Reset all chips to inactive
        chipSearchName.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipSearchName.setTextColor(getResources().getColor(R.color.text_primary, null));

        chipSearchCategory.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipSearchCategory.setTextColor(getResources().getColor(R.color.text_primary, null));

        chipSearchCountry.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipSearchCountry.setTextColor(getResources().getColor(R.color.text_primary, null));

        chipSearchIngredient.setBackgroundResource(R.drawable.bg_chip_inactive);
        chipSearchIngredient.setTextColor(getResources().getColor(R.color.text_primary, null));

        // Set active chip
        TextView activeChip;
        if (Constants.SEARCH_BY_CATEGORY.equals(currentSearchType)) {
            activeChip = chipSearchCategory;
        } else if (Constants.SEARCH_BY_COUNTRY.equals(currentSearchType)) {
            activeChip = chipSearchCountry;
        } else if (Constants.SEARCH_BY_INGREDIENT.equals(currentSearchType)) {
            activeChip = chipSearchIngredient;
        } else {
            activeChip = chipSearchName;
        }

        activeChip.setBackgroundResource(R.drawable.bg_chip_active);
        activeChip.setTextColor(getResources().getColor(R.color.background_dark, null));
    }

    @Override
    public void showSearchResults(List<Meal> meals) {
        layoutEmptyState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        adapter.setMeals(meals);
    }

    @Override
    public void showEmptyResults() {
        rvSearchResults.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        adapter.setMeals(null);
    }

    @Override
    public void showSuggestions(List<String> suggestions) {
        if (etSearch != null && getContext() != null) {
            ArrayAdapter<String> suggestionAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    suggestions);
            etSearch.setAdapter(suggestionAdapter);
        }
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
        Toast.makeText(requireContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMealClick(String mealId) {
        // Navigate to meal details
        Bundle bundle = new Bundle();
        bundle.putString("mealId", mealId);
        Navigation.findNavController(requireView()).navigate(R.id.action_search_to_mealDetails, bundle);
    }

    @Override
    public void onFavoriteClick(String mealId) {
        Toast.makeText(requireContext(), "Adding to favorites...", Toast.LENGTH_SHORT).show();
        // TODO: Add to favorites via presenter
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
