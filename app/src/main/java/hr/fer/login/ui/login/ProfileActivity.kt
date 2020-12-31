package hr.fer.login.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hr.fer.login.R
import hr.fer.login.api.SharedPrefManager
import hr.fer.login.data.model.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    fun userString(user:User): String {
        return "First Name: "+user.firstName+
                "\nLast Name: "+ user.lastName+
                "\nEmail: "+user.email+
                "\nUsername: "+user.username+
                "\nPassword: "+user.password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        log_out_button.setOnClickListener {
            SharedPrefManager.getInstance(applicationContext).clear()
            recreate()
        }
        val user = SharedPrefManager.getInstance(this).user

        user_profile_info.setText(userString(user))
        user_profile_info.isEnabled=false
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

