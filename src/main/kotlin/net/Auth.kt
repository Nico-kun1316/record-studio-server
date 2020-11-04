package net

import db.User
import db.validateCredentials
import io.ktor.auth.*

fun Authentication.Configuration.registerAuth() {
    basic {
        realm = "Record Studio"
        validate { credentials ->
            if (User.validateCredentials(credentials.name, credentials.password))
                UserIdPrincipal(credentials.name)
            else null
        }
    }
}
