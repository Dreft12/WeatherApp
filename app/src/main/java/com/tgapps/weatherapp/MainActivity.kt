package com.tgapps.weatherapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.tgapps.weatherapp.databinding.ActivityMainBinding
import com.tgapps.weatherapp.utils.PermissionUtils
import com.tgapps.weatherapp.utils.PermissionUtils.isPermissionGranted
import com.tgapps.weatherapp.viewModels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    OnRequestPermissionsResultCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(
            this, MapsInitializer.Renderer.LATEST
        ) {
            Log.e(TAG, "onCreate: New Map initialized")
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        initMap(savedInstanceState)
        observeFromViewModel()
        listeners()
    }

    private fun initMap(savedInstanceState: Bundle?) {
        binding.map.onCreate(savedInstanceState)
        binding.map.onResume()
        binding.map.getMapAsync(this)
    }

    private fun listeners() {
        binding.myLocation.setOnClickListener {
            getLastLocation()
        }

        binding.searchCity.doAfterTextChanged {
            mainActivityViewModel.searchValue.value = it.toString()
        }

        binding.searchCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mainActivityViewModel.loadCities()
            }
            false
        }
    }

    private fun observeFromViewModel() {
        mainActivityViewModel.isPermissionEnabled.observe(this) {
        }

        mainActivityViewModel.userLocation.observe(this) {

        }

        mainActivityViewModel.city.observe(this) {
             val targetLocation = Location(LocationManager.GPS_PROVIDER)
             targetLocation.longitude = it.location.lon
             targetLocation.latitude = it.location.lat
             mainActivityViewModel.moveCamera(targetLocation, 11f)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            mainActivityViewModel.userLocation.value = it
            mainActivityViewModel.moveCamera(it, 15.0f)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mainActivityViewModel.initMap(p0)
        mainActivityViewModel.configMap()
        enableLocation()
    }

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mainActivityViewModel.isPermissionEnabled.value = true
            getLastLocation()
            return
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                ACCESS_COARSE_LOCATION
            )
        ) {
            enableLocation()
        } else {
            mainActivityViewModel.isPermissionEnabled.value = false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}