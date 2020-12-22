package db

import net.data.Genres
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object Authors: UUIDTable() {
    val name = varchar("name", 128).uniqueIndex()
    val genre = enumerationByName<Genres>("genre", 64)
}

class Author(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Author>(Authors)

    var name by Authors.name
    var genre by Authors.genre
    val albums by Album referrersOn Albums.author
}
