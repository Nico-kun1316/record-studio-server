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
import net.assertParam
import net.data.*

fun Route.createAlbum() = post("authors/{id}/albums") {
    val id = call.parameters["id"].toUUID()
    val data: AlbumCreationData = call.receive()
    assertParam(data.name, { trim().isNotEmpty() })

    val albumId = asyncTransaction {
        val albumAuthor = Author.findById(id) ?: throw NotFoundException("Author doesn't exist")
        Album.new {
            author = albumAuthor
            name = data.name
            releaseDate = data.releasedOn
            genre = data.genre
        }.id.value
    }

    call.respond(HttpStatusCode.Created, IdData(albumId))
}

fun Route.fetchAlbums() = get("albums") {
    val page = call.parameters.page
    val albums = asyncTransaction {
        Album.paged(page).map { AlbumData(
            it.name,
            setOfNotNull(it.genre, it.author.genre),
            it.releaseDate,
            it.addedOn,
            it.id.value,
            it.author.id.value
        ) }
    }
    call.respond(albums)
}

fun Route.fetchAlbumsForAuthor() = get("authors/{id}/albums") {
    val id = call.parameters["id"].toUUID()
    val page = call.parameters.page
    val albums = asyncTransaction {
        val author = Author.findById(id) ?: throw NotFoundException("Author doesn't exist")
        author.albums.paged(page).map {
            AlbumData(
                it.name,
                setOfNotNull(it.genre, author.genre),
                it.releaseDate,
                it.addedOn,
                it.id.value,
                author.id.value
            )
        }
    }

    call.respond(HttpStatusCode.OK, albums)
}
