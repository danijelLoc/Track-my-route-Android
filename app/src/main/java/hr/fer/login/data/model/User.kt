package hr.fer.login.data.model

import java.io.Serializable

data class User(
        var id: Long = -1,
        var firstName: String = "",
        var lastName: String = "",
        var email: String = "",
        var username: String = "",
        var password: String = ""
) : Serializable
