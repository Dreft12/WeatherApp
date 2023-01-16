package com.tgapps.weatherapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.tgapps.weatherapp.app.utils.PermissionUtils
import com.tgapps.weatherapp.app.utils.PermissionUtils.isPermissionGranted
import com.tgapps.weatherapp.databinding.ActivityMainBinding
import com.tgapps.weatherapp.utils.DateUtils.convertToDate
import com.tgapps.weatherapp.utils.setupClearButtonWithAction
import com.tgapps.weatherapp.viewModels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.truncate

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
            Timber.tag(TAG).e("onCreate: New Map initialized")
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        initMap(savedInstanceState)
        initViews()
        observeFromViewModel()
        listeners()
    }

    private fun initViews() {
        binding.searchCity.apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.ic_baseline_search_24
                ), null, null, null
            )
            compoundDrawablePadding = 10
        }
    }

    private fun initMap(savedInstanceState: Bundle?) {
        binding.map.onCreate(savedInstanceState)
        binding.map.onResume()
        binding.map.getMapAsync(this)
        mainActivityViewModel.initPlaces()
    }

    private fun listeners() {
        binding.myLocation.setOnClickListener {
            getLastLocation()
        }

        binding.searchCity.doAfterTextChanged {
            mainActivityViewModel.searchValue.value = it.toString()
            mainActivityViewModel.autoCompletePlaces(it.toString())
        }

        binding.searchCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mainActivityViewModel.loadCities()
            }
            false
        }

        binding.closeInfoBtn.setOnClickListener {
            binding.cardContainer.visibility = View.INVISIBLE
        }

        binding.searchCity.setupClearButtonWithAction()
    }

    private fun observeFromViewModel() {
        mainActivityViewModel.isPermissionEnabled.observe(this) {
        }

        mainActivityViewModel.userLocation.observe(this) {

        }

        mainActivityViewModel.citiesList.observe(this) {
            val adapter =
                ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, it)
            binding.searchCity.apply {
                setAdapter(adapter)
                threshold = 5
                setOnItemClickListener { parent, _, position, _ ->
                    mainActivityViewModel.searchValue.value =
                        parent.getItemAtPosition(position).toString()
                    mainActivityViewModel.loadCities()

                }
            }
        }

        mainActivityViewModel.city.observe(this) {
            val targetLocation = Location(LocationManager.GPS_PROVIDER)
            targetLocation.longitude = it.location.lon
            targetLocation.latitude = it.location.lat
            mainActivityViewModel.moveCamera(targetLocation, 11f)
            binding.titleCityName.text = getString(R.string.title_name_city, it.location.name)
            binding.tempC.text =
                String.format(getString(R.string.tempC_value) + " " + it.current.tempC.toString())
            binding.tempF.text = String.format(getString(R.string.tempF_value) + " " + it.current.tempF.toString())
            binding.humidity.text = String.format(getString(R.string.humidity_value) + " " + it.current.humidity.toString() + "%%")
            binding.localTime.text = String.format(getString(R.string.local_time) + " " + it.location.localTime)
            binding.condition.text = String.format(getString(R.string.condition_value) + " " + it.current.condition.text)
            binding.feelsLikeC.text = String.format(getString(R.string.title_feels_like_c) + " " + it.current.feelsLikeC)
            binding.feelsLikeF.text = String.format(getString(R.string.title_feels_like_f) + " " + it.current.feelsLikeF)
            binding.cardContainer.visibility = View.VISIBLE

        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            if (it != null) {
                mainActivityViewModel.userLocation.value = it
                mainActivityViewModel.moveCamera(it, 15.0f)
            }
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