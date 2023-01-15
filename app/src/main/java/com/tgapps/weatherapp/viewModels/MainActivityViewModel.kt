package com.tgapps.weatherapp.viewModels

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MainActivityViewModel : ViewModel(),
    GoogleMap.OnMyLocationClickListener,
    GoogleMap.OnMyLocationButtonClickListener {
    lateinit var map: MutableLiveData<GoogleMap>

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