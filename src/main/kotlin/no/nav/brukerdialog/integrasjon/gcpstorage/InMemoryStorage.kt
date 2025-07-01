package no.nav.brukerdialog.integrasjon.gcpstorage

import com.google.cloud.storage.Storage.BlobField
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(value = ["test", "vtp"])
class InMemoryStorage : Storage {
    override fun ready() {}

    private val storage = mutableMapOf<StorageKey, StorageValue>()
    private val metadata = mutableMapOf<StorageKey, Map<String, Any>>()

    override fun fjerneHold(storageKey: StorageKey): Boolean {
        this.storage[storageKey] ?: throw IllegalStateException("Dokument med gitt storagekey ikke funnet.")
        metadata[storageKey] = mutableMapOf(BlobField.TEMPORARY_HOLD.name to false)
        return true
    }

    override fun slett(storageKey: StorageKey): Boolean {
        val value = storage.remove(storageKey)
        return value != null
    }

    override fun lagre(key: StorageKey, value: StorageValue, hold: Boolean) {
        storage[key] = value
    }

    override fun persister(key: StorageKey): Boolean {
        this.storage[key] ?: throw IllegalStateException("Fant ikke dokument å persistere.")
        metadata[key] = mutableMapOf(BlobField.TEMPORARY_HOLD.name to true)
        return harHold(key)
    }

    override fun harHold(key: StorageKey): Boolean {
        val metadata = this.metadata[key]!!
        return when(metadata[BlobField.TEMPORARY_HOLD.name] as Boolean?) {
            true -> true
            else -> false
        }
    }

    override fun hent(key: StorageKey): StorageValue? {
        return storage[key]
    }
}
