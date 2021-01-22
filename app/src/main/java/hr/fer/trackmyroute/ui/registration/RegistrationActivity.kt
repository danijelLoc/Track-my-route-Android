package hr.fer.trackmyroute.ui.registration

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import hr.fer.trackmyroute.R
import hr.fer.trackmyroute.api.RetrofitClient
import hr.fer.trackmyroute.data.model.RegisterResponse
import hr.fer.trackmyroute.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)


        registerButton.setOnClickListener {

            var user = User()

            val firstNameText = editTextFirstName.text.toString()
            val lastNameText = editTextLastName.text.toString()
            val emailText = editTextEmail.text.toString()
            val usernameText = editTextUsername.text.toString()
            val passwordText = editTextPassword.text.toString()

            if(firstNameText.isEmpty()) {
                editTextFirstName.error = "First name required"
                editTextFirstName.requestFocus()
                return@setOnClickListener
            }

            if(lastNameText.isEmpty()) {
                editTextLastName.error = "Last name required"
                editTextLastName.requestFocus()
                return@setOnClickListener
            }

            if(emailText.isEmpty()) {
                editTextEmail.error = "Email required"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }

            if(usernameText.isEmpty()) {
                editTextUsername.error = "Username required"
                editTextUsername.requestFocus()
                return@setOnClickListener
            }

            if(passwordText.isEmpty()) {
                editTextPassword.error = "Password required"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }

            user.firstName = firstNameText
            user.lastName = lastNameText
            user.email = emailText
            user.username = usernameText
            user.password = passwordText


            val rest = RetrofitClient.instance
            rest.registerUser(user)
                .enqueue(object: Callback<RegisterResponse> {

                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        if(response.body()==null) {
                            if(response.code()==406) {
                                registerTextView.error = "Registration invalid"
                                val toast = Toast.makeText(applicationContext, "Registration invalid", Toast.LENGTH_LONG).show()
                                editTextFirstName.requestFocus()
                            } else if(response.code()==409) {
                                editTextUsername.error = "Username already taken"
                                editTextUsername.requestFocus()
                            }
                            else {
                                val toast0 = Toast.makeText(applicationContext, "code: &{response.code()}", Toast.LENGTH_LONG).show()
                            }
                        }
                        else if(!(response.body()?.error!!)) {
                            val toast1 = Toast.makeText(applicationContext,"Registracija uspjesna", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        else {
                            val toast2 = Toast.makeText(applicationContext, response.body()!!.message, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        val toast3 = Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }

                })

        }



    }
}