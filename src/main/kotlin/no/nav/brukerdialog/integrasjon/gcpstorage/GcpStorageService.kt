package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import com.google.cloud.storage.Storage as GcpStorage

@Service
class GcpStorageService(
    private val storage: GcpStorage,
    @Value("\${no.nav.mellomlagring.gcp_storage_bucket_navn}") private val bucket: String,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GcpStorageService::class.java)
    }

    init {
        ensureBucketExists()
    }

    fun hent(key: StorageKey): StorageValue? {
        return try {
            val blob = storage.get(BlobId.of(bucket, key.value)) ?: return null
            val outputStream = ByteArrayOutputStream()
            blob.downloadTo(outputStream)

            StorageValue(String(outputStream.toByteArray()))
        } catch (ex: StorageException) {
            logger.error("Henting av dokument med id ${key.value} feilet.", ex)
            null
        }
    }

    fun slett(storageKey: StorageKey): Boolean {

        if (harHold(storageKey)) toggleHold(storageKey, false)

        val value = hent(storageKey)
        return if (value == null) false else {
            return try {
                storage.delete(bucket, storageKey.value)
                true
            } catch (cause: StorageException) {
                logger.warn("Sletting av dokument med id ${storageKey.value} feilet.", cause)
                false
            }
        }
    }

    fun lagre(key: StorageKey, value: StorageValue, hold: Boolean) {
        val blobId = BlobId.of(bucket, key.value)
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build()
        lagre(blobInfo, value)

        if (hold) toggleHold(key, hold)
    }

    fun fjerneHold(storageKey: StorageKey): Boolean {
        hent(storageKey) ?: return false
        if (harHold(storageKey)) toggleHold(storageKey, false)
        return true
    }

    private fun toggleHold(key: StorageKey, hold: Boolean) {
        val blobInfo = hentBlobInfoBuilder(key).setTemporaryHold(hold).build()
        storage.update(blobInfo)
        if (hold) logger.info("Midlertidig hold er plassert på dokument med id: ${key.value}")
        else logger.info("Midlertidig hold er fjernet på dokument med id: ${key.value}")
    }

    fun harHold(key: StorageKey): Boolean {
        val blobId = BlobId.of(bucket, key.value)
        val blob: Blob? = storage.get(blobId, GcpStorage.BlobGetOption.fields(GcpStorage.BlobField.TEMPORARY_HOLD))

        return when (blob?.temporaryHold) {
            true -> true
            else -> false
        }
    }

    private fun hentBlobInfoBuilder(key: StorageKey): BlobInfo.Builder {
        val blobId = BlobId.of(bucket, key.value)
        return BlobInfo.newBuilder(blobId)
    }

    /**
     * Setter metadata 'customDateTime' for eksisterende objekt.
     * Dette kallet setter customDateTime for objektet til året 999_999_999, eller inntil den er slettet.
     *
     * @param key Den unike identifikatoren til objektet.
     * @return Returnerer true, dersom objektet blir funnet og oppdatert med ny metadata. False dersom den ikke blir funnet, eller feiler.
     */
    fun persister(key: StorageKey): Boolean {
        if (hent(key) == null) return false

        return try {
            toggleHold(key, true)
            true
        } catch (ex: StorageException) {
            logger.error("Feilet med å persistere objekt med id: ${key.value}", ex)
            false
        }
    }

    private fun lagre(blobInfo: BlobInfo, value: StorageValue) {
        val content: ByteArray = value.value.toByteArray()
        try {
            storage.writer(blobInfo).use { writer -> writer.write(ByteBuffer.wrap(content, 0, content.size)) }
        } catch (ex: StorageException) {
            logger.error("Feilet med å lagre dokument med id: ${blobInfo.blobId.name}", ex)
            throw Exception("Feilet med å lagre dokument med id: ${blobInfo.blobId.name}. Exception: $ex")
        }
    }

    fun ready() {
        storage[bucket].location
    }

    private fun ensureBucketExists() {
        val bucket = storage.get(bucket)
        if (bucket !== null) {
            logger.info("Bucket ${this.bucket} funnet.")
        } else {
            throw IllegalStateException("Fant ikke bucket ved navn ${this.bucket}. Provisjoner en via naiserator.")
        }
    }
}
