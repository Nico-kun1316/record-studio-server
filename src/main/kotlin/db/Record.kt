package db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.`java-time`.CurrentDateTime
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.util.*

object Records: UUIDTable() {
    val name = varchar("name", 128)
    val location = varchar("location", 512)
    val previewLocation = varchar("preview_location", 512)
    val releasedOn = date("released_on").nullable().default(null)
    val addedOn = datetime("added_on").defaultExpression(CurrentDateTime())
    val album = reference("album", Albums, onDelete = CASCADE, onUpdate = CASCADE)
}

class Record(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Record>(Records)

    var name by Records.name
    var location by Records.location
    var previewLocation by Records.previewLocation
    var releasedOn by Records.releasedOn
    var addedOn by Records.addedOn
    var album by Album referencedOn Records.album
}
