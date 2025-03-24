package com.action.weather.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.action.weather.ForeCast
import com.action.weather.vm.WeatherViewModel

const val Permission_Request_Code = 123

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Main : Screen("main", "Home", Icons.Filled.Home)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object MoreLocations : Screen("moreLocations", "More Locations", Icons.Filled.LocationOn)
}
@Composable
fun WeatherApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Main.route, Modifier.padding(innerPadding)) {
            composable(Screen.Main.route) { WeatherScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
            composable(Screen.MoreLocations.route) { MoreLocationsScreen(navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.Main, Screen.Settings, Screen.MoreLocations)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun WeatherScreen(navController: NavController, viewModel: WeatherViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions.all { it.value }
        }
    )

    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") }
        )

        TextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") }
        )

        Button(onClick = {
            if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                viewModel.fetchForecast(latitude.toDouble(), longitude.toDouble())
            } else {
                viewModel.errorMessage.value = "Please enter latitude and longitude."
            }
        }) {
            Text("Get Forecast")
        }

        Button(onClick = {
            if (hasLocationPermission) {
                viewModel.fetchLocationAndForecast(context)
            } else {
                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }

        }) {
            Text("Get Location Forecast")
        }

        if (viewModel.forecastData.value != null) {
            DisplayForecast(viewModel.forecastData.value!!)
        } else if (viewModel.errorMessage.value != null) {
            Text(viewModel.errorMessage.value!!)
        }

    }
}

@Composable
fun DisplayForecast(forecast: ForeCast) {
    Column {
        Text(text = "City: ${forecast.city?.name}")
        forecast.list?.forEach { item ->
            Text(text = "Date: ${item.dtTxt}")
            Text(text = "Temperature: ${item.main?.temp}°C")
            Text(text = "Description: ${item.weather?.get(0)?.description}")
            Text(text = "Humidity: ${item.main?.humidity}%")
            Divider()
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings Screen")
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

@Composable
fun MoreLocationsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("More Locations Screen")
        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}