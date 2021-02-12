package hr.fer.trackmyroute.ui.newroute

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import hr.fer.trackmyroute.R
import hr.fer.trackmyroute.api.RetrofitClient
import hr.fer.trackmyroute.api.RoutesViewModel
import hr.fer.trackmyroute.api.SharedPrefManager
import hr.fer.trackmyroute.data.model.*
import hr.fer.trackmyroute.ui.login.MainActivity
import hr.fer.trackmyroute.ui.routes.RouteListActivity
import hr.fer.trackmyroute.viewmodel.distanceViewModel
import hr.fer.trackmyroute.viewmodel.durationViewModel
import kotlinx.android.synthetic.main.activity_newroute.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import retrofit2.Call
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.math.*

class NewRouteActivity : AppCompatActivity(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private var lastKnownLocationCheck: LatLng? = null
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: LatLng? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastKnownLocationNotNull: Boolean = false

    @Override
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //         remove title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_newroute)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // request permission for location access
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_FINE_LOCATION
        )

        var avgSpeed: Double = 0.0
        var avgSpeedString: String

        val routesViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                RoutesViewModel::class.java
            )
        val extras = intent.extras

        val distanceViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                distanceViewModel::class.java
            )

        val durationViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                durationViewModel::class.java
            )

        distanceViewModel.locationModel.value = LocationModel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }

                /*if (lastKnownLocation != null) {
                    return
                }*/
                // position camera on last known location
                /*for (location in locationResult.locations){
                    lastKnownLocation = LatLng(location.latitude, location.longitude)
                    distanceViewModel.locationList!!.add(Location(
                            location.latitude,
                            location.longitude,
                            LocalTime.now()
                        ))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, ZOOM_RATE))
                    drawRoute()

                    //distanceViewModel.locationModel.value =distanceViewModel.locationList!!.get(-1)
                }*/
                lastKnownLocation = LatLng(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                //if (fusedLocationClient != null)
                //fusedLocationClient.removeLocationUpdates(locationCallback)

                if (lastKnownLocationCheck == null) {
                    if (distanceViewModel.recordFlag) {
                        distanceViewModel.locationList!!.add(
                            Location(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude
                                //LocalDateTime.now().toString()
                            )
                        )
                        drawRoute()
                        lastKnownLocationCheck = lastKnownLocation
                    }
                    if (lastKnownLocationNotNull)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation))
                    else
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                lastKnownLocation,
                                ZOOM_RATE
                            )
                        )
                    lastKnownLocationNotNull = true
                    val locationModel = distanceViewModel.locationModel.value
                    locationModel!!.numberOfUpdatedLocations =
                        locationModel?.numberOfUpdatedLocations + 1
                    distanceViewModel.locationModel.value = locationModel
                } else {
                    if (calculateDistance(
                            lastKnownLocation!!.latitude, lastKnownLocation!!.longitude,
                            lastKnownLocationCheck!!.latitude, lastKnownLocationCheck!!.longitude,
                            0.0
                        ) > 0.01
                    ) {
                        if (distanceViewModel.recordFlag) {
                            distanceViewModel.locationList!!.add(
                                Location(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                    //LocalDateTime.now().toString()
                                )
                            )
                            drawRoute()
                            lastKnownLocationCheck = lastKnownLocation
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastKnownLocation))
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, ZOOM_RATE))
                        val locationModel = distanceViewModel.locationModel.value
                        locationModel!!.numberOfUpdatedLocations =
                            locationModel?.numberOfUpdatedLocations + 1
                        distanceViewModel.locationModel.value = locationModel
                    }
                }
            }
        }


        distanceViewModel.locationModel.observe(this, Observer {
            /*distanceViewModel.locationList!!.add(Location(
            lastKnownLocation!!.latitude,
            lastKnownLocation!!.longitude/*,
                        LocalTime.now()*/
        ))*/
            //distanceTextView.text = distanceViewModel.locationModel.value.toString()
            if (distanceViewModel.recordFlag)
                distanceViewModel.fetchDataFromRepository()
            else {
                if (fusedLocationClient != null) {
                    //requestLocationUpdates()
                }
            }
        })



        distanceViewModel.resultOfDataFetch.observe(this, Observer {
            distanceTextView.text = String.format("%7.3f km", distanceViewModel.distance)
            if (fusedLocationClient != null) {
                //requestLocationUpdates()
            }

        })


        durationViewModel.resultOfDataFetch.observe(this, Observer {
            var durationString = durationViewModel.duration.hour.toString()
            durationString = durationString.plus(":")
            durationString = durationString.plus(durationViewModel.duration.minute.toString())
            durationString = durationString.plus(":")
            durationString = durationString.plus(durationViewModel.duration.second.toString())
            durationTextView.text = durationString
            if (abs(durationViewModel.durationInHours) > 1e-8)
                avgSpeed = distanceViewModel.distance / durationViewModel.durationInHours
            avgSpeedString = String.format("%7.3f km/h", avgSpeed)
            averageSpeedTextView.text = avgSpeedString
        })



        startButton.setOnClickListener {
            distanceViewModel.onStart()
            durationViewModel.fetchDataFromRepository()
        }

        // SAVE ##################################################################################
        stopButton.setOnClickListener {
            durationViewModel.onStop()
            distanceViewModel.onStop()
            Thread.sleep(1000)
            var route: Route = Route()
            var routeName = editTextRouteName.text.toString()

            if (routeName.isEmpty()) {
                editTextRouteName.error = "Route name required"
                editTextRouteName.requestFocus()
                return@setOnClickListener
            }

            route.name = routeName
            route.date = LocalDateTime.now().toString() //"2021-01-08T12:02:45.137"
            route.distance = distanceViewModel.distance
            route.duration = durationViewModel.durationInSeconds
            route.speed = route.distance / durationViewModel.durationInHours
            route.user = SharedPrefManager.getInstance(applicationContext).user


            // save route
            RetrofitClient.instance.saveRoute(route)
                .enqueue(object : retrofit2.Callback<Route> {
                    override fun onFailure(call: Call<Route>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<Route>,
                        response: retrofit2.Response<Route>
                    ) {
                        if (response.code() == 200) {
                            val newRoute: Route = response.body()!!
                            routesViewModel.saveRouteToRepository(newRoute)

                            // save locations +++++++++++++++++++++++++++++++++++++++++++++++++
                            var locations = distanceViewModel.locationList
                            for ((index, value) in locations.withIndex()) {
                                var location = value;
                                location.route = newRoute
                                locations.set(index, location)
                            }
                            RetrofitClient.instance.saveRouteLocations(
                                newRoute.id,
                                locations
                            )
                                .enqueue(object : retrofit2.Callback<RouteLocationResponse> {
                                    override fun onFailure(
                                        call: Call<RouteLocationResponse>,
                                        t: Throwable
                                    ) {

                                    }

                                    override fun onResponse(
                                        call: Call<RouteLocationResponse>,
                                        response: retrofit2.Response<RouteLocationResponse>
                                    ) {
                                        if (response.code() == 200) {
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                response.body()?.message,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                })
                        }
                    }
                })
            // this is the way
            val intent = Intent(this, RouteListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    /*@Override
    override fun onPause() {
        var retVal: Boolean
        retVal = moveTaskToBack(true)
        super.onPause()
    }

    override fun onResume() {
        var retVal: Boolean
        retVal = moveTaskToBack(false)
        super.onResume()

    }*/


    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission is granted
                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isMyLocationButtonEnabled = true

                        // setup location change listening
                        val locationRequest = LocationRequest()
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        locationRequest.interval = 3000
                        locationRequest.fastestInterval = 3000
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest, locationCallback, null
                        )

                    }

                } else {
                    // TODO handle permission not granted
                }
                return
            }

        }
    }

    private fun requestLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.setInterval(5000) // two minute interval

        locationRequest.setFastestInterval(5000)
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    private fun getGeoContext(): GeoApiContext {

        val apiContext = GeoApiContext()
        apiContext.setApiKey(resources.getString(R.string.google_maps_key))
        return apiContext
    }

    private fun drawRoute() {
        val now = DateTime()
        var origin: String

        if (lastKnownLocationCheck != null)
            origin = "${lastKnownLocationCheck?.latitude}, ${lastKnownLocationCheck?.longitude}"
        else
            origin = "${lastKnownLocation?.latitude}, ${lastKnownLocation?.longitude}"

        val req = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.WALKING).origin(origin)
            .destination("${lastKnownLocation?.latitude}, ${lastKnownLocation?.longitude}")
            .departureTime(now)


        req.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                // Handle successful request.
                runOnUiThread {
                    /*mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(result.routes[0].legs[0].startLocation.lat,
                                result.routes[0].legs[0].startLocation.lng)).icon(BitmapDescriptorFactory.defaultMarker(146F)))
                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(result.routes[0].legs[0].endLocation.lat,
                                result.routes[0].legs[0].endLocation.lng)).icon(BitmapDescriptorFactory.defaultMarker(146F)))*/
                    if (lastKnownLocationCheck != null) {
                        mMap.addCircle(
                            CircleOptions().center(
                                LatLng(
                                    result.routes[0].legs[0].endLocation.lat,
                                    result.routes[0].legs[0].endLocation.lng
                                )
                            ).radius(1.5).strokeColor(Color.rgb(68, 114, 196))
                                .fillColor(Color.rgb(68, 114, 196))
                        )
                    } else {
                        mMap.addCircle(
                            CircleOptions().center(
                                LatLng(
                                    result.routes[0].legs[0].startLocation.lat,
                                    result.routes[0].legs[0].startLocation.lng
                                )
                            ).radius(1.5).strokeColor(Color.rgb(68, 114, 196))
                                .fillColor(Color.rgb(68, 114, 196))
                        )
                    }

                    val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)
                    mMap.addPolyline(
                        PolylineOptions().addAll(decodedPath).color(Color.rgb(68, 114, 196))
                    )
                }

            }

            override fun onFailure(e: Throwable) {
                // Handle error.
            }
        })

    }

    companion object {
        val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0
        val FER_LOCATION = "45.801432, 15.971117"
        val ZOOM_RATE = 16.0f
    }

    override fun onStart() {
        super.onStart()

        if (!SharedPrefManager.getInstance(this).isLoggedIn) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        oldDistance: Double
    ): Double {
        var distance: Double
        val radius: Int = 6371
        var dLat: Double = degreeToRadian(lat2 - lat1)
        var dLon: Double = degreeToRadian(lon2 - lon1)

        var a: Double = sin(dLat / 2) * sin(dLat / 2) +
                cos(degreeToRadian(lat1)) * cos(degreeToRadian(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        var c: Double = 2 * atan2(sqrt(a), sqrt(1 - a))

        distance = radius * c + oldDistance

        return distance
    }

    fun degreeToRadian(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

}