package hr.fer.trackmyroute.api


import hr.fer.trackmyroute.data.model.LoginResponse
import hr.fer.trackmyroute.data.model.RegisterResponse
import hr.fer.trackmyroute.data.model.User
import hr.fer.trackmyroute.data.model.UserSimple
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface Api {

    @POST("/loginUser")
    fun userLogin(
            @Body userSimple: UserSimple
    ):Call<LoginResponse>

    @POST("/registerUser")
    fun registerUser(@Body user: User): Call<RegisterResponse>
}