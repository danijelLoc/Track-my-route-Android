package hr.fer.trackmyroute.ui.routes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import hr.fer.trackmyroute.api.RoutesAdapter
import hr.fer.trackmyroute.R
import hr.fer.trackmyroute.api.RetrofitClient
import hr.fer.trackmyroute.api.RoutesRepository
import hr.fer.trackmyroute.api.RoutesViewModel
import hr.fer.trackmyroute.api.SharedPrefManager
import hr.fer.trackmyroute.data.model.Route
import hr.fer.trackmyroute.data.model.RouteResponse
import hr.fer.trackmyroute.data.model.RoutesResponse
import hr.fer.trackmyroute.ui.login.MainActivity
import hr.fer.trackmyroute.ui.newroute.NewRouteActivity
import kotlinx.android.synthetic.main.activity_route_list.*
import retrofit2.Call


class RouteListActivity : AppCompatActivity(), RoutesAdapter.OnRouteListener,
    RoutesAdapter.OnRemoveRouteListener {

    lateinit var routesAdapter: RoutesAdapter
    lateinit var viewModel: RoutesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_list)


        RetrofitClient.instance.getAllRoutes(SharedPrefManager.getInstance(this).user.id)
            .enqueue(object : retrofit2.Callback<RoutesResponse> {
                override fun onFailure(call: Call<RoutesResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<RoutesResponse>,
                    response: retrofit2.Response<RoutesResponse>
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

                        RoutesRepository.routeList =
                            ((response.body()?.routes as MutableList<Route>?)!!)
                        routesAdapter.listOfRoutes.getRoutesRepository()


                    } else {
                        Toast.makeText(
                            applicationContext,
                            response.body()?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            })


        listOfRoutesView.layoutManager = LinearLayoutManager(applicationContext)

        val decorator = DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
        decorator.setDrawable(
            ContextCompat.getDrawable(
                applicationContext,
                R.drawable.cell_divider
            )!!
        )
        listOfRoutesView.addItemDecoration(decorator)

        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                RoutesViewModel::class.java
            )

        viewModel.getRoutesRepository()

        routesAdapter = RoutesAdapter(viewModel, this, this)
        listOfRoutesView.adapter = routesAdapter

        viewModel.routeList.observe(this, Observer {
            routesAdapter.notifyDataSetChanged()
        })



        newRouteActionButton.setOnClickListener {
            val intent = Intent(this, NewRouteActivity::class.java)
            startActivity(intent)
        }

        logOutButton.setOnClickListener {
            SharedPrefManager.getInstance(applicationContext).clear()
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }


    override fun onResume() {
        super.onResume()
        if(!SharedPrefManager.getInstance(this).isLoggedIn){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        viewModel.getRoutesRepository()
    }

    override fun onRouteClick(position: Int) {
        Log.d("loc", "clicked $position");
        val intent = Intent(this, NewRouteActivity::class.java)
        intent.putExtra("position", position)
        startActivityForResult(intent, 0)
    }

    override fun onRemoveRouteClick(position: Int) {
        Log.d("loc", "remove $position")

        RetrofitClient.instance.deleteRoute(
            routesAdapter.listOfRoutes.getRouteFromRepository(
                position
            )
        )
            .enqueue(object : retrofit2.Callback<RouteResponse> {
                override fun onFailure(call: Call<RouteResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<RouteResponse>,
                    response: retrofit2.Response<RouteResponse>
                ) {
                    if (response.body() == null) {
                        if (response.code() == 404) {
                            Toast.makeText(
                                applicationContext,
                                "code: ${response.code()}", Toast.LENGTH_LONG
                            ).show()

                            Toast.makeText(
                                applicationContext,
                                "code: ${response.code()}", Toast.LENGTH_LONG
                            ).show()
                        }
                    } else if (!response.body()?.error!!) {
                        Toast.makeText(
                            applicationContext,
                            response.body().toString(),
                            Toast.LENGTH_LONG
                        ).show()
                        // save to live data on front end
                        viewModel.removeRouteFromRepository(position)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            response.body()?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        routesAdapter.listOfRoutes.removeRouteFromRepository(position)
        routesAdapter.listOfRoutes.getRoutesRepository()
    }

    override fun onStart() {
        super.onStart()
        if(!SharedPrefManager.getInstance(this).isLoggedIn){
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

}
