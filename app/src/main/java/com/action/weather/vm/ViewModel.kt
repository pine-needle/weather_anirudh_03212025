package com.action.weather.vm

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.action.weather.ForeCast
import com.action.weather.data.WeatherApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherApi: WeatherApi,
    private val apiKey: String
) : ViewModel() {

    var forecastData = mutableStateOf<ForeCast?>(null)
    var errorMessage = mutableStateOf<String?>(null)

    fun fetchForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getForecast(lat, lon, apiKey)
                forecastData.value = response
                errorMessage.value = null
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An error occurred."
                forecastData.value = null
            }
        }
    }

    fun fetchLocationAndForecast(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = getLocation(context, locationManager)

        if (location != null) {
            fetchForecast(location.latitude, location.longitude)
        } else {
            errorMessage.value = "Location not available"
        }
    }

    private fun getLocation(context: Context, locationManager: LocationManager): android.location.Location? {
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var location: android.location.Location? = null

        if (isGpsEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        if (location == null && isNetworkEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        return location
    }
}