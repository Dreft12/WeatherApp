package com.tgapps.weatherapp.viewModels

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.tgapps.weatherapp.models.City
import com.tgapps.weatherapp.services.networking.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val repository: Repository) : ViewModel(),
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMyLocationButtonClickListener {

    lateinit var map: MutableLiveData<GoogleMap>
    val city = MutableLiveData<City>()
    val searchValue = MutableLiveData<String>()

    fun loadCities() {
        CoroutineScope(Dispatchers.IO).launch {
            val cities = repository.getCurrentWeather(searchValue.value.toString())
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

    val isPermissionEnabled: MutableLiveData<Boolean>
        get() {
            return MutableLiveData<Boolean>()
        }

    val userLocation: MutableLiveData<Location>
        get() {
            return MutableLiveData<Location>()
        }

    fun initMap(map: GoogleMap) {
        this.map = MutableLiveData(map)
    }

    @SuppressLint("MissingPermission")
    fun configMap() {
        this.map.value?.isMyLocationEnabled = true
        this.map.value?.uiSettings?.isMyLocationButtonEnabled = false
        this.map.value?.setOnMyLocationButtonClickListener(this)
        this.map.value?.setOnMyLocationClickListener(this)
    }



    private suspend fun getCitiesByName(city: String) {
        CoroutineScope(Dispatchers.IO).launch {


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
        map.value?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ),
                zoom
            )
        )
    }
}