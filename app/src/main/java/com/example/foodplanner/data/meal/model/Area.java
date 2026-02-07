package com.example.foodplanner.data.meal.model;

import com.google.gson.annotations.SerializedName;

public class Area {
    @SerializedName("strArea")
    private String strArea;

    public String getStrArea() {
        return strArea;
    }

    public void setStrArea(String strArea) {
        this.strArea = strArea;
    }

    public String getFlagUrl() {
        // Map country names to flag URLs using flagsapi.com
        String countryCode = getCountryCode();
        if (countryCode != null && !countryCode.equals("UNKNOWN")) {
            return "https://flagsapi.com/" + countryCode + "/flat/64.png";
        }
        return null; // Getting null handled by Glide placeholder
    }

    private String getCountryCode() {
        switch (strArea) {
            case "American":
                return "US";
            case "British":
                return "GB";
            case "Canadian":
                return "CA";
            case "Chinese":
                return "CN";
            case "Croatian":
                return "HR";
            case "Dutch":
                return "NL";
            case "Egyptian":
                return "EG";
            case "Filipino":
                return "PH";
            case "French":
                return "FR";
            case "Greek":
                return "GR";
            case "Indian":
                return "IN";
            case "Irish":
                return "IE";
            case "Italian":
                return "IT";
            case "Jamaican":
                return "JM";
            case "Japanese":
                return "JP";
            case "Kenyan":
                return "KE";
            case "Malaysian":
                return "MY";
            case "Mexican":
                return "MX";
            case "Moroccan":
                return "MA";
            case "Polish":
                return "PL";
            case "Portuguese":
                return "PT";
            case "Russian":
                return "RU";
            case "Saudi Arabian":
                return "SA";
            case "Slovakian":
                return "SK";
            case "Spanish":
                return "ES";
            case "Syrian":
                return "SY";
            case "Thai":
                return "TH";
            case "Tunisian":
                return "TN";
            case "Turkish":
                return "TR";
            case "Ukrainian":
                return "UA";
            case "Uruguayan":
                return "UY";
            case "Vietnamese":
                return "VN";
            case "Armenian":
                return "AM";
            case "Tanzanian":
                return "TZ";
            case "Venezuelan":
            case "Venezuela":
            case "Venezulan":
                return "VE";
            case "Algerian":
                return "DZ";
            case "Argentinian":
                return "AR";
            case "Australian":
                return "AU";
            case "Norwegian":
                return "NO";
            case "Unknown":
                return "UNKNOWN";
            default:
                return null;
        }
    }
}
