package hr.fer.trackmyroute.ui.routes


import android.Manifest
import android.annotation.SuppressLint
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
import com.google.android.gms.maps.model.*
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

    companion object {
        val MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0
        val FER_LOCATION = "45.801432, 15.971117"
        val ZOOM_RATE = 17.0f
    }

    var oldRoutePosition: Int? = null
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: LatLng? = null
    private lateinit var locationCallback: LocationCallback

    @SuppressLint("SetTextI18n")
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
            var date = route.date
            var dateString = date.substring(8,10)
            dateString = dateString.plus(".")
            dateString = dateString.plus(date.substring(5,7))
            dateString = dateString.plus(".")
            dateString = dateString.plus(date.substring(0,4))
            dateString = dateString.plus(". ")
            dateString = dateString.plus(date.substring(11,16))
            dateTextView.setText(dateString)
            var duration = route.duration.toInt()
            val builder = StringBuilder()
            if ((duration/3600)<10) {
                builder.append("0")
            }
            builder.append((duration / 3600).toString())
            builder.append(":")
            duration = duration % 3600
            if ((duration/60)<10) {
                builder.append("0")
            }
            builder.append((duration / 60).toString())
            builder.append(":")
            duration = duration % 60
            if (duration<10) {
                builder.append("0")
            }
            builder.append(duration.toString())
            timeTextView.setText("Time:    " + builder)
            speedTextView.setText(String.format("Avg. speed: %7.2f km/h", route.speed))
            distanceTextView.setText(String.format("Distance: %7.2f km", route.distance))

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
                                    "No locations found", Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "code: ${response.code()}", Toast.LENGTH_LONG
                                ).show()
                            }
                        } else if (!response.body()?.error!!) {
                            routeLocations =
                                ((response.body()?.routeLocations as MutableList<RouteLocation>?)!!)
                            for (i in 1..routeLocations.size) {
                                var start: Boolean = false
                                var end: Boolean = false
                                if (i == routeLocations.size)
                                    end = true
                                if (i == 1)
                                    start = true
                                if (end)
                                    drawRoute(
                                        routeLocations[i - 1],
                                        routeLocations[i - 1], start, end
                                    )
                                else
                                    drawRoute(routeLocations[i - 1], routeLocations[i], start, end)
                            }

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



        saveRouteButton.setOnClickListener {
            var route = Route()
            if (oldRoutePosition != null) {
                route = viewModel.getRouteFromRepository(oldRoutePosition!!)
            }
            route.name = routeTitleEditText.text.toString()


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
                            // save to live data on front end
                            if (oldRoutePosition != null) {
                                viewModel.updateRouteInRepository(oldRoutePosition!!, route)
//                                listOfRoutesView.recycledViewPool.clear();
                                finish()
                            } else {
                                viewModel.saveRouteToRepository(route)
                                finish()
                            }
                        }
                        /*Toast.makeText(
                            applicationContext,
                            response.body()?.message,
                            Toast.LENGTH_LONG
                        ).show()*/
                    }
                })
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }


    private fun getGeoContext(): GeoApiContext {

        val apiContext = GeoApiContext()
        apiContext.setApiKey(resources.getString(R.string.google_maps_key))
        return apiContext
    }

    private fun drawRoute(
        location1: RouteLocation,
        location2: RouteLocation,
        start: Boolean,
        end: Boolean
    ) {
        val now = DateTime()
        var origin: String = "${location1.latitude}, ${location1.longitude}"
        var destination: String = "${location2.latitude}, ${location2.longitude}"

        val req = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.WALKING).origin(origin)
            .destination(destination).departureTime(now)
        Log.d("origin", origin)

        req.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                // Handle successful request.

                runOnUiThread {
//                    mMap.addMarker(MarkerOptions().position().)
                    if (start && end)
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    location1.latitude,
                                    location1.longitude
                                )
                            ).title("Start/End")
                        )
                    else if (start) {
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    location1.latitude,
                                    location1.longitude
                                )
                            ).title("Start")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                    } else if (end) {
                        mMap.addMarker(
                            MarkerOptions().position(
                                LatLng(
                                    location2.latitude,
                                    location2.longitude
                                )
                            ).title("End")
                        )
                    }
                    mMap.addCircle(
                        CircleOptions().center(
                            LatLng(
                                location1.latitude,
                                location1.longitude
                            )
                        ).radius(1.5).strokeColor(Color.rgb(0, 90, 220))
                            .fillColor(Color.rgb(0, 100, 250))
                    )

                    mMap.addCircle(
                        CircleOptions().center(
                            LatLng(
                                location2.latitude,
                                location2.longitude
                            )
                        ).radius(1.5).strokeColor(Color.rgb(0, 90, 220))
                            .fillColor(Color.rgb(0, 100, 250))
                    )

                    val decodedPath = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)
                   mMap.addPolyline(PolylineOptions().addAll(decodedPath).color(Color.rgb(0, 100, 250)).width(7.5F))
                    if (start)
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location1.latitude,
                                    location1.longitude
                                ), ZOOM_RATE
                            )
                        )
                }

            }

            override fun onFailure(e: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Cant show locations",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }

}
