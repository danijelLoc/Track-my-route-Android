package hr.fer.trackmyroute.ui.routes


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import hr.fer.trackmyroute.R
import hr.fer.trackmyroute.api.RetrofitClient
import hr.fer.trackmyroute.api.RoutesRepository
import hr.fer.trackmyroute.api.RoutesViewModel
import hr.fer.trackmyroute.api.SharedPrefManager
import hr.fer.trackmyroute.data.model.*
import kotlinx.android.synthetic.main.activity_route_details.*
import kotlinx.android.synthetic.main.activity_route_list.*
import org.joda.time.DateTime
import retrofit2.Call

class RouteDetails : AppCompatActivity(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    var oldRoutePosition: Int? = null
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: LatLng? = null
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_details)
        Log.d("loc", "called create")
        val viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                RoutesViewModel::class.java
            )
        val extras = intent.extras
        var routeLocations: List<RouteLocation> = listOf()
        if (extras != null) {
            var position: Int = extras["position"] as Int;
            oldRoutePosition = position
            Log.d("loc", position.toString())
            var route: Route = viewModel.getRouteFromRepository(position)
            var title: String? = route.name
            routeTitleEditText.setText(title)
            dateTextView.setText(route.date)
            timeTextView.setText("Time: " + route.duration.toString())
            speedTextView.setText("Avg. speed:" + route.speed.toString() + " km/h")
            distanceTextView.setText("Distance:" + route.distance.toString() + " km")

            RetrofitClient.instance.getRouteLocations(route.id)
                    .enqueue(object : retrofit2.Callback<RouteLocationsResponse> {
                        override fun onFailure(call: Call<RouteLocationsResponse>, t: Throwable) {
                            Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onResponse(
                                call: Call<RouteLocationsResponse>,
                                response: retrofit2.Response<RouteLocationsResponse>
                        ) {

                            if (response.body() == null) {
                                if (response.code() == 404) {
                                    Toast.makeText(
                                            applicationContext,
                                            "code create: ${response.code()}", Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                            applicationContext,
                                            "code: ${response.code()}", Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else if (!response.body()?.error!!) {
//                        Toast.makeText(
//                            applicationContext,
//                            response.body().toString(),
//                            Toast.LENGTH_LONG
//                        ).show()
                                routeLocations = ((response.body()?.routeLocations as MutableList<RouteLocation>?)!!)

                            } else {
                                Toast.makeText(
                                        applicationContext,
                                        response.body()?.message,
                                        Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                    })
        } else Log.d("loc", "empty extras")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // request permission for location access
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_FINE_LOCATION)

        for (i in 1..routeLocations.size) {
            drawRoute(routeLocations[i-1], routeLocations[i])
        }




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
    }

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

    private fun drawRoute(location1: RouteLocation, location2: RouteLocation) {
        val now = DateTime()
        var origin: String = "${location1.latitude}, ${location1.longitude}"
        var destination: String = "${location2.latitude}, ${location2.longitude}"

        val req = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.WALKING).
        origin(origin).destination(destination).departureTime(now)


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

                    mMap.addCircle(
                           CircleOptions().center(
                                    LatLng(result.routes[0].legs[0].startLocation.lat,
                                            result.routes[0].legs[0].startLocation.lng)).radius(1.5).strokeColor(Color.rgb(68, 114, 196)).fillColor(Color.rgb(68, 114, 196)))
                    mMap.addCircle(
                            CircleOptions().center(
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


}
