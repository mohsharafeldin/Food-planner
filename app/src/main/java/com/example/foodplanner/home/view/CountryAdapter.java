package com.example.foodplanner.home.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.data.meal.model.Area;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> {

    private List<Area> countries = new ArrayList<>();
    private final Context context;
    private final OnCountryClickListener listener;

    public interface OnCountryClickListener {
        void onCountryClick(Area country);
    }

    public CountryAdapter(Context context, OnCountryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCountries(List<Area> countries) {
        this.countries = countries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_country, parent, false);
        return new CountryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {
        Area country = countries.get(position);
        holder.bind(country);
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    class CountryViewHolder extends RecyclerView.ViewHolder {

        private final android.widget.ImageView ivCountryFlag;
        private final TextView tvCountryName;

        CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCountryFlag = itemView.findViewById(R.id.iv_country_flag);
            tvCountryName = itemView.findViewById(R.id.tv_country_name);
        }

        void bind(Area country) {
            tvCountryName.setText(country.getStrArea());

            com.bumptech.glide.Glide.with(context)
                    .load(country.getFlagUrl())
                    .apply(new com.bumptech.glide.request.RequestOptions()
                            .placeholder(R.drawable.ic_food_logo)
                            .error(R.drawable.ic_food_logo))
                    .into(ivCountryFlag);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCountryClick(country);
                }
            });
        }
    }
}
