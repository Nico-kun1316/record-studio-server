package net.route

import db.Author
import db.asyncTransaction
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import misc.toUUID
import net.NotFoundException
import net.assertParam
import net.data.*
import java.util.*

fun Route.createAuthor() = post("authors") {
    val data: AuthorCreationData = call.receive()

    assertParam(data.name, { !isEmpty() })

    val authorId = asyncTransaction {
        Author.new {
            name = data.name
            genre = data.genre
        }.id.value
    }

    call.respond(HttpStatusCode.Created, IdData(authorId))
}

fun Route.fetchAuthor() = get("authors/{id}") {
    val idString = call.parameters["id"]
    val id = UUID.fromString(idString)
    val author = asyncTransaction { Author.findById(id) ?: throw NotFoundException("Author doesn't exist") }
    call.respond(AuthorData(author.id.value, author.name, author.genre))
}

fun Route.fetchAuthors() = get("authors") {
    val page = call.parameters.page
    val authors = asyncTransaction {
        Author.paged(page).map { AuthorData(it.id.value, it.name, it.genre) }
    }

    call.respond(authors)
}

fun Route.deleteAuthor() = delete("authors/{id}") {
    val id = call.parameters["id"].toUUID()
    asyncTransaction {
        val author = Author.findById(id) ?: throw NotFoundException("Author doesn't exist")
        author.delete()
    }

    call.response.status(HttpStatusCode.NoContent)
}
