package db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object Records: UUIDTable() {
    val name = varchar("name", 128)
    val location = varchar("location", 512)
    val album = reference("album", Albums)
}

class Record(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Record>(Records)

    var name by Records.name
    var location by Records.location
    var album by Album referencedOn Records.album
}
