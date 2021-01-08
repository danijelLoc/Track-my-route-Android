package hr.fer.trackmyroute.ui.newroute

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import hr.fer.trackmyroute.R
import hr.fer.trackmyroute.api.RetrofitClient
import hr.fer.trackmyroute.api.RoutesViewModel
import hr.fer.trackmyroute.data.model.Route
import hr.fer.trackmyroute.data.model.RouteResponse
import kotlinx.android.synthetic.main.activity_route_details.*
import kotlinx.android.synthetic.main.activity_route_list.*
import retrofit2.Call
import com.google.android.gms.location.*
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import hr.fer.trackmyroute.api.SharedPrefManager
import hr.fer.trackmyroute.data.model.User
import kotlinx.android.synthetic.main.activity_newroute.*
import kotlinx.android.synthetic.main.activity_profile.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode


import hr.fer.trackmyroute.data.model.RegisterResponse

import hr.fer.trackmyroute.ui.login.MainActivity
import hr.fer.trackmyroute.viewmodel.*
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.activity_newroute.*
import hr.fer.trackmyroute.ui.login.*
import org.joda.time.DateTime
import kotlin.math.abs

class NewRouteActivity : AppCompatActivity(), OnMapReadyCallback,
ActivityCompat.OnRequestPermissionsResultCallback {
    var oldRoutePosition: Int? = null

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: LatLng? = null
    private lateinit var locationCallback: LocationCallback


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
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_FINE_LOCATION)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                if (lastKnownLocation != null) {
                    return
                }
                // position camera on last known location
                for (location in locationResult.locations){
                    lastKnownLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, ZOOM_RATE))
                    drawRoute()
                }
            }
        }

        var avgSpeed: Double = 0.0
        var avgSpeedString: String

        val distanceViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                distanceViewModel::class.java)

        val myViewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                durationViewModel::class.java)

        distanceViewModel.resultOfDataFetch.observe(this, Observer {
            distanceTextView.text = it
            if (abs(myViewModel.durationInHours) > 1e-8)
                avgSpeed = distanceViewModel.distance / myViewModel.durationInHours
            avgSpeedString = String.format("%7.2f km/h", avgSpeed)
            averageSpeedTextView.text = avgSpeedString

        })

        myViewModel.resultOfDataFetch.observe(this, Observer {
            durationTextView.text = it
        })



        startButton.setOnClickListener {
            myViewModel.fetchDataFromRepository()
            distanceViewModel.fetchDataFromRepository()
        }

        stopButton.setOnClickListener {
            myViewModel.onStop()
            distanceViewModel.onStop()
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }



    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        // Permission is granted
                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isMyLocationButtonEnabled = true

                        // setup location change listening
                        val locationRequest = LocationRequest()
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        locationRequest.interval = 1000
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest, locationCallback, null)

                    }

                } else {
                    // TODO handle permission not granted
                }
                return
            }

        }
    }


    private fun getGeoContext(): GeoApiContext {

        val apiContext = GeoApiContext()
        apiContext.setApiKey(resources.getString(R.string.google_maps_key))
        return apiContext
    }

    private fun drawRoute() {
        val now = DateTime()
        val origin = "${lastKnownLocation?.latitude}, ${lastKnownLocation?.longitude}"

        val req = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.WALKING).
        origin(origin).destination(FER_LOCATION).departureTime(now)


        req.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                // Handle successful request.
                runOnUiThread {
                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(result.routes[0].legs[0].startLocation.lat,
                                result.routes[0].legs[0].startLocation.lng)) )
                    mMap.addMarker(
                        MarkerOptions().position(
                            LatLng(result.routes[0].legs[0].endLocation.lat,
                                result.routes[0].legs[0].endLocation.lng)))

                    val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)
                    mMap.addPolyline(PolylineOptions().addAll(decodedPath))
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

        if(!SharedPrefManager.getInstance(this).isLoggedIn){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_details)
        Log.d("loc", "called create")
        val viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                RoutesViewModel::class.java
            )
        val extras = intent.extras
        if (extras != null) {
            var position: Int = extras["position"] as Int;
            oldRoutePosition = position
            Log.d("loc", position.toString())
            var route: Route = viewModel.getRouteFromRepository(position)
            var title: String? = route.name
            routeTitleEditText.setText(title)
        } else Log.d("loc", "empty extras")




        saveRouteButton.setOnClickListener {
            var route = Route()
            if (oldRoutePosition != null) {
                route = viewModel.getRouteFromRepository(oldRoutePosition!!)
            }
            route.name = routeTitleEditText.text.toString()


            RetrofitClient.instance.saveRoute(route)
                .enqueue(object : retrofit2.Callback<RouteResponse> {
                    override fun onFailure(call: Call<RouteResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(
                        call: Call<RouteResponse>,
                        response: retrofit2.Response<RouteResponse>
                    ) {
                        if (!response.body()?.error!!) {
                            // save to live data on front end
                            if (oldRoutePosition != null) {
                                viewModel.updateRouteInRepository(oldRoutePosition!!, route)
//                                listOfRoutesView.recycledViewPool.clear();

                            } else viewModel.saveRouteToRepository(route)
                        }
                        Toast.makeText(
                            applicationContext,
                            response.body()?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            finish()
        }
    }*/




}