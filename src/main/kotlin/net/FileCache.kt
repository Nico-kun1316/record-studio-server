package net

import com.azure.storage.blob.BlobContainerClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.withLock

class FileCache(private val storage: BlobContainerClient) {
    private val tmpDir by lazy { File(System.getProperty("java.io.tmpdir")) }
    private val lock = Mutex()
    private val cache = mutableMapOf<String, File>()

    suspend fun get(key: String): File =
        lock.withLock {
            if (key in cache)
                cache[key]!!
            else withContext(Dispatchers.IO) {
                val file = File(tmpDir, key)
                val blob = storage.getBlobClient(key)
                FileOutputStream(file).use { blob.download(it) }
                cache[key] = file
                file
            }
        }

}
