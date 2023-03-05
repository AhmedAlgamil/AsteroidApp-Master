package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Config.DEBUG
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.AsteroidsDatabaseDao
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.BuildConfig.DEBUG
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.AsteroidApiFilters
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

enum class AsteroidApiStatus { LOADING, ERROR, DONE }

class MainViewModel(
    val database: AsteroidsDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob: Job = Job()
    private val _navigateToAsteroidDetails = MutableLiveData<Asteroid>()

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _response = MutableLiveData<String>()
    private val _title = MutableLiveData<String>()
    private val _imageURL = MutableLiveData<String>()

    // The external immutable LiveData for the request status String
    val response: LiveData<String>
        get() = _response

    val title: LiveData<String>
        get() = _title

    val imageURL: LiveData<String>
        get() = _imageURL

    private lateinit var _asteroidLists: ArrayList<Asteroid>

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var currentAsteroid = MutableLiveData<Asteroid>()

    var asteroids = database.getAllAsteroids("2023-01-24", "2023-01-24")

    fun getAllAsteroidsFunction(startDate: String, endDate: String) {
//        asteroids = database.getAllAsteroids("2023-01-24","2023-01-24")
        asteroids = database.getAllAsteroids(startDate, endDate)
    }

    init {
        getImageOfTheDay()
        getAsteroidsAPI()
        initializeGettingAsteroid()
    }

//    private suspend fun getTodaysAsteroids(dateToday:String): LiveData<List<Asteroid>> {
//        return withContext(Dispatchers.IO) {
//            asteroids = database.getAllAsteroidsToday(dateToday)
//            asteroids
//        }
//    }

//    fun onGettingTodayAsteroids(toDayDate:String) {
//        viewModelScope.launch {
//            // Clear the database table.
//            getTodaysAsteroids(toDayDate)
//
//            // And clear tonight since it's no longer in the database
//            currentAsteroid.value = null
//        }
//    }

    fun onAsteroidClicked(asteroidItem: Asteroid) {
        _navigateToAsteroidDetails.value = asteroidItem
    }

    fun onMainFragmentDetailsNavigated() {
        _navigateToAsteroidDetails.value = null
    }

    private fun initializeGettingAsteroid() {
        uiScope.launch {
            currentAsteroid.value = getAsteroidsFromDataBase()
        }
    }

    private suspend fun getAsteroidsFromDataBase(): Asteroid? {
        return withContext(Dispatchers.IO) {
            var asteroid = database.getAsteroid()
            asteroid
        }
    }

    fun onStartShowing(
        id: Long,
        codename: String,
        closeApproachDate: String,
        absoluteMagnitude: Double,
        estimatedDiameter: Double,
        relativeVelocity: Double,
        distanceFromEarth: Double,
        isPotentiallyHazardous: Boolean,
    ) {
        viewModelScope.launch {
            val newAsteroid = Asteroid(
                id,
                codename,
                closeApproachDate,
                absoluteMagnitude,
                estimatedDiameter,
                relativeVelocity,
                distanceFromEarth,
                isPotentiallyHazardous,
            )
            insert(newAsteroid)
            currentAsteroid.value = getAsteroidsFromDataBase()
        }
    }

    val navigateToAsteroidDetails: LiveData<Asteroid>
        get() = _navigateToAsteroidDetails


    suspend fun insert(newAsteroid: Asteroid) {
        database.insert(newAsteroid)
    }

    fun onClear() {
        viewModelScope.launch {
            // Clear the database table.
            clear()

            // And clear tonight since it's no longer in the database
            currentAsteroid.value = null
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    private suspend fun update(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            database.update(asteroid)
        }
    }

    private fun getImageOfTheDay() {
        AsteroidApi.retrofitServiceImage.getImageOfTheDay()
            .enqueue(object : Callback<PictureOfDay> {
                override fun onFailure(call: Call<PictureOfDay>, t: Throwable) {
                    _response.value = "Failure: " + t.message
                    Log.d("this is error ", _response.value.toString())
                }

                override fun onResponse(
                    call: Call<PictureOfDay>,
                    response: Response<PictureOfDay>
                ) {
                    _title.value = response.body()?.title
                    _imageURL.value = response.body()?.url
                }
            })
    }

    private fun getAsteroidsAPI() {
        AsteroidApi.retrofitService.getProperties().enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                _response.value = "Failure: " + t.message
                Log.d("this is error ", _response.value.toString())
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                var obj = JSONObject(response.body())
                _response.value = parseAsteroidsJsonResult(obj).size.toString()
                _asteroidLists = parseAsteroidsJsonResult(obj)
                onClear()
                for (i in 0 until _asteroidLists.size - 1) {
                    onStartShowing(
                        _asteroidLists.get(i).id,
                        _asteroidLists.get(i).codename,
                        _asteroidLists.get(i).closeApproachDate,
                        _asteroidLists.get(i).absoluteMagnitude,
                        _asteroidLists.get(i).estimatedDiameter,
                        _asteroidLists.get(i).relativeVelocity,
                        _asteroidLists.get(i).distanceFromEarth,
                        _asteroidLists.get(i).isPotentiallyHazardous,
                    )
                }
            }
        })
    }

}