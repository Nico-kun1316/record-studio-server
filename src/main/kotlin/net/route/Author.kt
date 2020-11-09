package net.route

import db.Author
import db.asyncTransaction
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import net.NotFoundException
import net.data.AuthorCreationData
import net.data.AuthorData
import net.data.page
import net.data.paged
import java.util.*

fun Route.createAuthor() = post("authors") {
    val data: AuthorCreationData = call.receive()
    val author = asyncTransaction {
        Author.new {
            name = data.name
        }
    }

    call.respond(HttpStatusCode.Created, AuthorData(author.id.value, author.name))
}

fun Route.fetchAuthor() = get("authors/{id}") {
    val idString = call.parameters["id"]
    val id = UUID.fromString(idString)
    val author = asyncTransaction { Author.findById(id) ?: throw NotFoundException("Author doesn't exist") }
    call.respond(AuthorData(author.id.value, author.name))
}

fun Route.fetchAuthors() = get("authors") {
    val page = call.parameters.page
    val authors = asyncTransaction {
        Author.paged(page).map { AuthorData(it.id.value, it.name) }
    }

    call.respond(authors)
}

fun Route.deleteAuthor() = delete("authors/{id}") {
    val id = UUID.fromString(call.parameters["id"])
    asyncTransaction {
        val author = Author.findById(id) ?: throw NotFoundException("Author doesn't exist")
        author.delete()
    }

    call.response.status(HttpStatusCode.NoContent)
}
