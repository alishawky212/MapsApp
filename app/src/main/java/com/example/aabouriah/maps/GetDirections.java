package com.example.aabouriah.maps;

import com.example.aabouriah.maps.Entities.DirectionResults;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDirections {
    @GET("/maps/api/directions/json")
    Call<DirectionResults> getDirections(@Query("origin") String origin, @Query("destination") String destination);
}
