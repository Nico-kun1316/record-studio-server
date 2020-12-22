package net

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import net.data.TokenData
import net.route.*


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
                fetchUsers()
                fetchAuthor()
                fetchAuthors()
                fetchRecord()
                fetchRecordData()
                fetchAlbumsForAuthor()
            }
            authenticate("admin") {
                patchUser()
                deleteUser()
                createAlbum()
                createAuthor()
                deleteAuthor()
                createRecord()
            }
            fetchRecordPreview()
            createUser()
        }
    }

    route("/auth/v1") {
        authenticate("basic") {
            post("token") {
                val user = call.user()
                val (token, refreshToken) = generateTokensForUser(user)
                call.respond(TokenData(token, refreshToken))
            }
        }
        authenticate("refresh") {
            post("refresh_token") {
                val user = call.user()
                val (token, refreshToken) = generateTokensForUser(user)
                call.respond(TokenData(token, refreshToken))
            }
        }
    }

    get("/") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }
}
