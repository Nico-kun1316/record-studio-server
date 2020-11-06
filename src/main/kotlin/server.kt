import db.Users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.AuthenticationException
import net.AuthorizationException
import net.registerAuth
import net.registerRoutes
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


fun main() {
    val dbDriver = "org.postgresql.Driver"
    val env = System.getenv()
    // val azureUrl = env["AZURE_STORAGE_CONNECTION_STRING"]!!
    val dbUrl = env["JDBC_DATABASE_URL"]!!
    Database.connect(dbUrl, driver = dbDriver)
    val port = env.getOrDefault("PORT", "8080").toInt()

    /*
    val azureClient = BlobServiceClientBuilder().connectionString(azureUrl).buildClient()
    val storage = azureClient.getBlobContainerClient("storage")
    val blob = storage.getBlobClient("blob.png")
    println("uploading blob")
    blob.uploadFromFile("c:/users/gabe/desktop/unknown.png")
    println("uploaded blob")
    for (blobble in storage.listBlobs()) {
        println(blobble.name)
    }
    println("downloading blob")
    blob.downloadToFile("c:/users/gabe/desktop/${blob.blobName}")
    println("downloaded blob")
     */

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
    }

    embeddedServer(Netty, port = port) {
        install(StatusPages) {
            exception<AuthorizationException> { call.respond(HttpStatusCode.Forbidden) }
            exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
        }
        install(ContentNegotiation) {
            json()
        }
        authentication {
            registerAuth()
        }
        routing {
            registerRoutes()
        }

    }.start(wait = true)
}
