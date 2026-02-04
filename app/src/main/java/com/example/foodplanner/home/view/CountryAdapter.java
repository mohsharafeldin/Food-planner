package com.example.foodplanner.home.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodplanner.R;
import com.example.foodplanner.data.model.Area;

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

        private final TextView tvCountryFlag;
        private final TextView tvCountryName;

        CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCountryFlag = itemView.findViewById(R.id.tv_country_flag);
            tvCountryName = itemView.findViewById(R.id.tv_country_name);
        }

        void bind(Area country) {
            tvCountryName.setText(country.getStrArea());
            tvCountryFlag.setText(getFlagEmoji(country.getStrArea()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCountryClick(country);
                }
            });
        }

        private String getFlagEmoji(String countryName) {
            switch (countryName) {
                case "Algerian":
                    return "🇩🇿";
                case "American":
                    return "🇺🇸";
                case "Argentinian":
                    return "🇦🇷";
                case "Australian":
                    return "🇦🇺";
                case "British":
                    return "🇬🇧";
                case "Canadian":
                    return "🇨🇦";
                case "Chinese":
                    return "🇨🇳";
                case "Croatian":
                    return "🇭🇷";
                case "Dutch":
                    return "🇳🇱";
                case "Egyptian":
                    return "🇪🇬";
                case "Filipino":
                    return "🇵🇭";
                case "French":
                    return "🇫🇷";
                case "Greek":
                    return "🇬🇷";
                case "Indian":
                    return "🇮🇳";
                case "Irish":
                    return "🇮🇪";
                case "Italian":
                    return "🇮🇹";
                case "Jamaican":
                    return "🇯🇲";
                case "Japanese":
                    return "🇯🇵";
                case "Kenyan":
                    return "🇰🇪";
                case "Malaysian":
                    return "🇲🇾";
                case "Mexican":
                    return "🇲🇽";
                case "Moroccan":
                    return "🇲🇦";
                case "Norwegian":
                    return "🇳🇴";
                case "Polish":
                    return "🇵🇱";
                case "Portuguese":
                    return "🇵🇹";
                case "Russian":
                    return "🇷🇺";
                case "Saudi Arabian":
                    return "🇸🇦";
                case "Slovakian":
                    return "🇸🇰";
                case "Spanish":
                    return "🇪🇸";
                case "Syrian":
                    return "🇸🇾";
                case "Thai":
                    return "🇹🇭";
                case "Tunisian":
                    return "🇹🇳";
                case "Turkish":
                    return "🇹🇷";
                case "Ukrainian":
                    return "🇺🇦";
                case "Uruguayan":
                    return "🇺🇾";
                case "Venezuelan":
                case "Venezuela":
                case "Venezulan": // Typo in API
                    return "🇻🇪";
                case "Vietnamese":
                    return "🇻🇳";
                default:
                    android.util.Log.d("FlagDebug", "Unknown country: " + countryName);
                    return "🍽️";
            }
        }
    }
}
