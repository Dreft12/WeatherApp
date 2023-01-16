package com.tgapps.weatherapp.viewModels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tgapps.weatherapp.BuildConfig
import com.tgapps.weatherapp.models.City
import com.tgapps.weatherapp.services.networking.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application),
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMyLocationButtonClickListener {

    private lateinit var map: GoogleMap
    val city = MutableLiveData<City>()
    val searchValue = MutableLiveData<String>()
    private val placesClient = MutableLiveData<PlacesClient>()
    private val placeNames: MutableList<Place.Field> = mutableListOf()
    private var context: Application
    private lateinit var request: FindCurrentPlaceRequest
    var citiesList = MutableLiveData<ArrayList<String>>()
    var historyCity = ArrayList<String>()
    var sharedPreferences: SharedPreferences

    init {
        context = application
        sharedPreferences = application.getSharedPreferences("WeatherApp", Context.MODE_PRIVATE)
        historyCity = if (sharedPreferences.contains("history")) Gson().fromJson(
            sharedPreferences.getString(
                "history",
                ""
            ), object : TypeToken<ArrayList<String>>() {}.type
        ) else ArrayList()
    }

    val isPermissionEnabled: MutableLiveData<Boolean>
        get() {
            return MutableLiveData<Boolean>()
        }

    val userLocation: MutableLiveData<Location>
        get() {
            return MutableLiveData<Location>()
        }

    fun initPlaces() {
        Places.initialize(context, BuildConfig.MAPS_API_KEY)
        placesClient.value = Places.createClient(context)
        placeNames.add(Place.Field.NAME)
        placeNames.add(Place.Field.ADDRESS)
        placeNames.add(Place.Field.LAT_LNG)
        request = FindCurrentPlaceRequest.newInstance(placeNames)
    }

    private fun writeHistory() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.putString("history", Gson().toJson(historyCity))
        editor.apply()
    }

    fun loadCities() {
        CoroutineScope(Dispatchers.IO).launch {
            val cities = repository.getCurrentWeather(searchValue.value.toString().split(",")[0])
            when (cities.isSuccessful) {
                true -> {
                    with(cities.body()) {
                        city.postValue(this)
                        this?.let { historyCity.add(it.location.name) }
                        writeHistory()
                    }
                }
                else -> {
                    Timber.e(cities.message())
                }
            }
        }
    }

    fun initMap(map: GoogleMap) {
        this.map = map
    }

    @SuppressLint("MissingPermission")
    fun configMap() {
        try {
            this.map.isMyLocationEnabled = true
            this.map.uiSettings.isMyLocationButtonEnabled = false
            this.map.setOnMyLocationButtonClickListener(this)
            this.map.setOnMyLocationClickListener(this)
        } catch (e: java.lang.Exception) {
            println(e.message)
        }

    }

    override fun onMyLocationClick(p0: Location) {
        userLocation.value = p0
    }

    override fun onMyLocationButtonClick(): Boolean {
        userLocation.value?.let { moveCamera(it, 15.0f) }
        return false
    }

    fun moveCamera(location: Location, zoom: Float) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ),
                zoom
            )
        )
    }

    fun autoCompletePlaces(query: String) {
        val token = AutocompleteSessionToken.newInstance()
        val request =
            FindAutocompletePredictionsRequest.builder().setSessionToken(token).setTypeFilter(
                TypeFilter.CITIES
            ).setQuery(query)
                .build()
        placesClient.value?.findAutocompletePredictions(request)?.addOnSuccessListener { response ->
            val listTemp = ArrayList<String>()
            for (item in response.autocompletePredictions) {
                listTemp.add(item.getFullText(null).toString())
            }
            citiesList.value = listTemp
        }?.addOnFailureListener {
            val apiException = it as ApiException
            Timber.tag("Error API:").e(apiException)
        }
    }
}