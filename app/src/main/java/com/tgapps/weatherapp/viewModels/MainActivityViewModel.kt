package com.tgapps.weatherapp.viewModels

import android.app.Application
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivityViewModel : ViewModel() {
    val isPermissionEnabled: MutableLiveData<Boolean>
        get() {
            return MutableLiveData<Boolean>()
        }

    val userLocation: MutableLiveData<Location>
        get() {
            return MutableLiveData<Location>()
        }
    }