package hr.fer.login.api

import android.content.Context
import hr.fer.login.data.model.User


class SharedPrefManager private constructor(private val mCtx: Context) {

    val isLoggedIn: Boolean
        get() {
            val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getLong("id", -1) != -1L
        }

    val user: User
        get() {
            val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return User(
                    sharedPreferences.getLong("id", -1),
                sharedPreferences.getString("firstName", "")!!,
                sharedPreferences.getString("lastName", "")!!,
                sharedPreferences.getString("email", "")!!,
                sharedPreferences.getString("username", "")!!,
                sharedPreferences.getString("password", "")!!
            )
        }


    fun saveUser(user: User) {

        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        user.id.let { editor.putLong("id", it) }
        editor.putString("firstName", user.firstName)
        editor.putString("lastName", user.lastName)
        editor.putString("email", user.email)
        editor.putString("username", user.username)
        editor.putString("password", user.password)

        editor.apply()

    }

    fun clear() {
        val sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private val SHARED_PREF_NAME = "my_shared_preff"
        private var mInstance: SharedPrefManager? = null
        @Synchronized
        fun getInstance(mCtx: Context): SharedPrefManager {
            if (mInstance == null) {
                mInstance = SharedPrefManager(mCtx)
            }
            return mInstance as SharedPrefManager
        }
    }

}