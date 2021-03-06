package net

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import io.ktor.application.*
import io.ktor.util.*

val storageKey = AttributeKey<BlobContainerClient>("storage client")
val cacheKey = AttributeKey<FileCache>("file cache")

fun getStorage(url: String, container: String): BlobContainerClient {
    val azureClient = BlobServiceClientBuilder().connectionString(url).buildClient()
    return azureClient.getBlobContainerClient(container)
}

val ApplicationCall.storage
    get() = attributes[storageKey]

val ApplicationCall.files
    get() = attributes[cacheKey]
