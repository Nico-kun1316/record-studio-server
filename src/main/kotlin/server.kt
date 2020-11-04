import db.Users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import net.registerAuth
import net.registerRoutes
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


fun main() {
    val dbDriver = "org.postgresql.Driver"
    val devDbUrl = "jdbc:postgresql://localhost:5432/record-studio?user=postgres&password=vombisek1"
    val env = System.getenv()
    val dbUrl = env.getOrDefault("JDBC_DATABASE_URL", devDbUrl)
    Database.connect(dbUrl, driver = dbDriver)
    val port = env.getOrDefault("PORT", "8080").toInt()

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
    }

    embeddedServer(Netty, port = port) {
        authentication {
            registerAuth()
        }
        routing {
            registerRoutes()
        }
        install(ContentNegotiation) {
            json()
        }

    }.start(wait = true)
}
