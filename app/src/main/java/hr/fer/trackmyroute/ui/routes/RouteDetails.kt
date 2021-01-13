package hr.fer.trackmyroute.ui.routes


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

class RouteDetails : AppCompatActivity() {
    var oldRoutePosition: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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
            dateTextView.setText(route.date)
            timeTextView.setText("Time: " + route.duration.toString())
            speedTextView.setText("Avg. speed:" + route.speed.toString() + " km/h")
            distanceTextView.setText("Distance:" + route.distace.toString() + " km")

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
    }


}
