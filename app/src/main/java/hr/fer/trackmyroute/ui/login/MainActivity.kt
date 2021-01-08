package hr.fer.trackmyroute.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hr.fer.trackmyroute.R
import hr.fer.trackmyroute.api.RetrofitClient
import hr.fer.trackmyroute.api.SharedPrefManager
import hr.fer.trackmyroute.data.model.LoginResponse
import hr.fer.trackmyroute.data.model.UserSimple
import hr.fer.trackmyroute.ui.registration.RegistrationActivity
import hr.fer.trackmyroute.ui.routes.RouteListActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//         remove title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_main)


        button_register.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        login_button.setOnClickListener {

            val username_text = username.text.toString().trim()
            val password_text = password.text.toString().trim()

            if(username_text.isEmpty()){
                username.error = "Username required"
                username.requestFocus()
                return@setOnClickListener
            }


            if(password_text.isEmpty()){
                password.error = "Password required"
                password.requestFocus()
                return@setOnClickListener
            }

            RetrofitClient.instance.userLogin(UserSimple(username_text,password_text))
                .enqueue(object: retrofit2.Callback<LoginResponse>{
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onResponse(call: Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {

                        if(response.body()==null){
                            if(response.code()==404){
                                username.error = "Username or password invalid"
                                username.requestFocus()
                            }else {
                                Toast.makeText(
                                    applicationContext,
                                    "code: ${response.code()}", Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        else if(!response.body()?.error!!){
                            Toast.makeText(applicationContext, response.body().toString(), Toast.LENGTH_LONG).show()

                            SharedPrefManager.getInstance(applicationContext).saveUser(response.body()?.user!!)
//                            val intent = Intent(applicationContext, ProfileActivity::class.java)
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            val intent = Intent(applicationContext, RouteListActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)


                        }else{
                            Toast.makeText(applicationContext, response.body()?.message, Toast.LENGTH_LONG).show()
                        }

                    }
                })

        }
    }

    override fun onStart() {
        super.onStart()

        if(SharedPrefManager.getInstance(this).isLoggedIn){
//            val intent = Intent(applicationContext, ProfileActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val intent = Intent(applicationContext, RouteListActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}