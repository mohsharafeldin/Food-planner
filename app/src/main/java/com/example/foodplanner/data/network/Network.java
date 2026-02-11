package com.example.foodplanner.data.network;

import com.example.foodplanner.data.datasource.remote.MealApiService;
import com.example.foodplanner.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network configuration singleton for Retrofit.
 */
public class Network {

    private static volatile Network instance;
    private final MealApiService mealApiService;

    private Network() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        mealApiService = retrofit.create(MealApiService.class);
    }

    public static synchronized Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    public MealApiService getMealApiService() {
        return mealApiService;
    }
}
