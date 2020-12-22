package net.route

import db.Album
import db.Record
import db.asyncTransaction
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import misc.toUUID
import net.*
import net.data.RecordCreationData
import net.data.RecordData
import java.util.*

fun Route.createRecord() = post("albums/{id}/records") {
    val id = UUID.randomUUID()
    val store = call.storage
    try {
        val albumId = call.parameters["id"].toUUID()
        val multipart = call.receiveMultipart()

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> {
                    Json.decodeFromString<RecordCreationData>(partData.value).let {
                        asyncTransaction {
                            val albumObject = Album.findById(albumId) ?: throw NotFoundException("Album doesn't exist")
                            Record.new(id) {
                                album = albumObject
                                location = "$id"
                                previewLocation = "${id}_preview"
                                name = it.name
                                price = it.price
                                releasedOn = it.releasedOn
                            }
                        }
                    }
                }
                is PartData.FileItem -> withContext(Dispatchers.IO) {
                    val name = if (partData.name == "preview") "${id}_preview" else "$id"
                    val length = partData.headers[HttpHeaders.ContentLength]?.toLong()
                    if (length == null) {
                        asyncTransaction { Record.findById(id)?.delete() }
                        throw InvalidParameterException("File length is missing")
                    }
                    if (partData.contentType != ContentType.Audio.OGG) {
                        asyncTransaction { Record.findById(id)?.delete() }
                        throw IllegalMediaTypeException("File type must be audio/ogg, but is ${partData.contentType}")
                    }
                    val client = store.getBlobClient(name).blockBlobClient
                    partData.streamProvider().use {
                        client.getBlobOutputStream(true).use { os ->
                            it.transferTo(os)
                        }
                    }
                }
                is PartData.BinaryItem -> throw InvalidParameterException("Binary data is not supported")
            }
        }
        call.response.status(HttpStatusCode.Created)
    } catch (e: Exception) {
        e.printStackTrace()
        asyncTransaction { Record.findById(id)?.delete() }
        withContext(Dispatchers.IO) {
            val blob = store.getBlobClient("$id").blockBlobClient
            val previewBlob = store.getBlobClient("${id}_preview")
            blob.delete()
            previewBlob.delete()
        }
        throw e
    }
}

fun Route.fetchRecordData() = get("records/{id}") {
    val id = call.parameters["id"].toUUID()
    val record = asyncTransaction {
        Record.findById(id)?.let { RecordData(it.id.value, it.album.id.value, it.name, it.price, it.releasedOn) }
            ?: throw NotFoundException("Track cannot be found")
    }
    call.respond(record)
}

@OptIn(KtorExperimentalAPI::class)
suspend fun PipelineContext<Unit, ApplicationCall>.streamRecord(preview: Boolean) {
    try {
        val id = call.parameters["id"].toUUID()
        val location = asyncTransaction {
            val record = Record.findById(id) ?: throw NotFoundException("Track doesn't exist")
            if (preview) record.previewLocation else record.location
        }
        val store = call.storage
        val blob = store.getBlobClient(location)
        call.respondBytesWriter(ContentType.Audio.OGG) {
            val flow = blob.readFlow
            withContext(Dispatchers.IO) {
                flow.collect {
                    writeFully(it, 0, it.size)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
}

fun Route.fetchRecordPreview() = get("records/{id}/preview") {
    streamRecord(preview = true)
}

fun Route.fetchRecord() = get("records/{id}/full") {
    streamRecord(preview = false)
}
