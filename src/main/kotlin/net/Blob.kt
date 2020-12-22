package net

import com.azure.storage.blob.BlobClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import loop
import misc.kB

val BlobClient.readFlow: Flow<ByteArray>
    get() = flow {
        val stream = this@readFlow.openInputStream().buffered()
        loop {
            val array = stream.readNBytes(512.kB.toInt())
            if (array.isEmpty())
                return@loop
            emit(array)
        }
    }
