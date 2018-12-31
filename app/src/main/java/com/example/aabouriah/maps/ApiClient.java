package com.example.aabouriah.maps;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static ApiClient instance = null;
    private static final String BASE_URL = "http://maps.googleapis.com/";
    private Retrofit retrofit = null;

    private GetDirections directionsService;

    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }

        return instance;
    }

    private ApiClient() {
        buildRetrofit();
    }

    private void buildRetrofit() {

        if (retrofit == null)
        {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        this.directionsService = retrofit.create(GetDirections.class);
    }

    public GetDirections getUserService() {
        return this.directionsService;
    }
}
