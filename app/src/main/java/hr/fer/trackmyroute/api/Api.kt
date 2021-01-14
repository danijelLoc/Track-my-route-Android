package hr.fer.trackmyroute.api


import hr.fer.trackmyroute.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface Api {

    @POST("/loginUser")
    fun userLogin(
            @Body userSimple: UserSimple
    ):Call<LoginResponse>

    @POST("/registerUser")
    fun registerUser(@Body user: User): Call<RegisterResponse>

    @GET("/routes")
    fun getAllRoutes(@Query("user_id") user_id:Long?):Call<RoutesResponse>

    @POST("/saveRoute")
    fun saveRoute(@Body route: Route): Call<RouteResponse>

    @POST("/deleteRoute")
    fun deleteRoute(@Body route: Route): Call<RouteResponse>

    @POST("/saveRouteLocations")
    fun saveRouteLocations(@Body routeLocations: List<Location>): Call<RouteLocationResponse>

}