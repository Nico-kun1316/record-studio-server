package net

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import net.data.TokenData
import net.route.createUser
import net.route.fetchUser


fun HTML.index() {
    head {
        title("Record Server")
    }
    body {
        div {
            h1 {
                +"This website is cursed"
            }
        }
    }
}

fun Route.registerRoutes() {
    route("/api") {
        route("v1") {
            authenticate {
                fetchUser()
            }
            createUser()
        }
    }

    route("/auth/v1") {
        authenticate("basic") {
            get("token") {
                val id = call.UUID()
                val (token, refreshToken) = generateTokensForId(id)
                call.respond(TokenData(token, refreshToken))
            }
        }
        authenticate("refresh") {
            get("refresh_token") {
                val id = call.UUID()
                val (token, refreshToken) = generateTokensForId(id)
                call.respond(TokenData(token, refreshToken))
            }
        }
    }

    get("/") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }
}
