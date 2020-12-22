import com.azure.storage.blob.BlobServiceClientBuilder
import db.Albums
import db.Authors
import db.Records
import db.Users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.*
import net.data.ErrorData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


fun main() {
    val dbDriver = "org.postgresql.Driver"
    val env = System.getenv()
    val azureUrl = env["AZURE_STORAGE_CONNECTION_STRING"]!!
    val dbUrl = env["JDBC_DATABASE_URL"]!!
    Database.connect(dbUrl, driver = dbDriver)
    val port = env.getOrDefault("PORT", "8080").toInt()

    val storage = getStorage(azureUrl, "storage")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users, Authors, Albums, Records)
    }

    embeddedServer(Netty, port = port) {
        install(StatusPages) {
            exception<StatusException> {
                it.printStackTrace()
                call.respond(it.status, ErrorData(it.message ?: "Unknown error"))
            }
        }
        install(ContentNegotiation) {
            json()
        }
        authentication {
            registerAuth()
        }
        routing {
            intercept(ApplicationCallPipeline.Call) {
                call.attributes.put(storageKey, storage)
            }
            registerRoutes()
        }

    }.start(wait = true)
}
