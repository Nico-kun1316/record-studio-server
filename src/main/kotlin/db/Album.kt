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

object Albums: UUIDTable() {
    val name = varchar("name", 128)
    val releaseDate = date("release_date")
    val addedOn = datetime("added_on").defaultExpression(CurrentDateTime())
    val author = reference("author", Authors, onDelete = CASCADE, onUpdate = CASCADE)
}

class Album(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<Album>(Albums)

    var name by Albums.name
    var releaseDate by Albums.releaseDate
    var addedOn by Albums.addedOn
    var author by Author referencedOn Albums.author
    val records by Record referrersOn Records.album
}
