package db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object Albums: UUIDTable() {
    val name = varchar("name", 128)
    val author = reference("author", Authors)
}

class Album(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Album>(Albums)

    var name by Albums.name
    var author by Author referencedOn Albums.author
    val records by Record referrersOn Records.album
}
