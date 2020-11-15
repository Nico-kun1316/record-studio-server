package net.route

import db.Album
import db.Author
import db.asyncTransaction
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import misc.toUUID
import net.NotFoundException
import net.data.*

fun Route.createAlbum() = post("authors/{id}/albums") {
    val id = call.parameters["id"].toUUID()
    val data = call.receive<AlbumCreationData>()
    val albumId = asyncTransaction {
        val author = Author.findById(id) ?: throw NotFoundException("Author doesn't exist")
        Album.new {
            this.author = author
            this.name = data.name
            this.releaseDate = data.releasedOn
        }.id.value
    }

    call.respond(HttpStatusCode.Created, IdData(albumId))
}

fun Route.fetchAlbumsForAuthor() = get("authors/{id}/albums") {
    val id = call.parameters["id"].toUUID()
    val page = call.parameters.page
    val albums = asyncTransaction {
        val author = Author.findById(id) ?: throw NotFoundException("Author doesn't exist")
        author.albums.paged(page).map {
            AlbumData(it.name, it.releaseDate, it.addedOn, author.id.value)
        }
    }

    call.respond(HttpStatusCode.OK, albums)
}
