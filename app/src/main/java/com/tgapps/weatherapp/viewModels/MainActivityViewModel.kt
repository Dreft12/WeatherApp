package com.tgapps.weatherapp.viewModels

import android.annotation.SuppressLint
import android.app.Application
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
    lateinit var request: FindCurrentPlaceRequest
    var citiesList = MutableLiveData<ArrayList<String>>()

    init {
        context = application
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

    fun loadCities() {
        CoroutineScope(Dispatchers.IO).launch {
            val cities = repository.getCurrentWeather(searchValue.value.toString().split(",")[0])
            when (cities.isSuccessful) {
                true -> {
                    with(cities.body()) {
                        city.postValue(this)
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
        this.map.isMyLocationEnabled = true
        this.map.uiSettings.isMyLocationButtonEnabled = false
        this.map.setOnMyLocationButtonClickListener(this)
        this.map.setOnMyLocationClickListener(this)
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