package hr.fer.login.api


import hr.fer.login.data.model.LoginResponse
import hr.fer.login.data.model.RegisterResponse
import hr.fer.login.data.model.User
import hr.fer.login.data.model.UserSimple
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface Api {

    @POST("/loginUser")
    fun userLogin(
            @Body userSimple: UserSimple
    ):Call<LoginResponse>

    @POST("/registerUser")
    fun registerUser(@Body user: User): Call<RegisterResponse>
}