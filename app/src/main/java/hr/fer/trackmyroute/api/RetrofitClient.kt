package hr.fer.trackmyroute.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

//    private val AUTH = "Basic "+ Base64.encodeToString("dan:qwer1234".toByteArray(), Base64.NO_WRAP)

    private const val BASE_URL = "http://192.168.100.111:8080/"
    // Create a new object from HttpLoggingInterceptor


//    private val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val original = chain.request()
//
//                val requestBuilder = original.newBuilder()
//                        .addHeader("Authorization", AUTH)
//                        .method(original.method(), original.body())
//
//                val request = requestBuilder.build()
//                chain.proceed(request)
//            }.build()

    val instance: Api by lazy{
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS).addInterceptor(interceptor).build()
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        retrofit.create(Api::class.java)
    }

}