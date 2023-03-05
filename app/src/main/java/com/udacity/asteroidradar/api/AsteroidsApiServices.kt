package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants.BASE_URL
import com.udacity.asteroidradar.PictureOfDay
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

enum class AsteroidApiFilters(val value: String) { SHOW_WEEK_ASTEROID("week_asteroid"), SHOW_TODAY_ASTEROID("today_asteroids"), SHOW_SAVED_ASTEROIDS("saved_asteroids") }

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

private val retrofitPictureOfTheDay = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface AsteroidsApiServices {
    @GET("neo/rest/v1/feed?start_date=2023-01-17&end_date=2023-01-24&api_key=${BuildConfig.API_KEY}")
//    @GET("realestate")
    fun getProperties():
            Call<String>

    @GET("planetary/apod?api_key=${BuildConfig.API_KEY}")
    fun getImageOfTheDay():
            Call<PictureOfDay>
}


object AsteroidApi {
    val retrofitService: AsteroidsApiServices by lazy {
        retrofit.create(AsteroidsApiServices::class.java)
    }

    val retrofitServiceImage: AsteroidsApiServices by lazy {
        retrofitPictureOfTheDay.create(AsteroidsApiServices::class.java)
    }
}