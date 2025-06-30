package no.nav.brukerdialog.mellomlagring.dokument

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import no.nav.brukerdialog.integrasjon.clamav.VirusskannerService
import no.nav.brukerdialog.integrasjon.gcpstorage.Storage
import no.nav.brukerdialog.integrasjon.gcpstorage.StorageKey
import no.nav.brukerdialog.integrasjon.gcpstorage.StorageValue
import no.nav.brukerdialog.mellomlagring.dokument.kryptering.KrypteringService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
data class DokumentService(
    private val krypteringService: KrypteringService,
    private val storage: Storage,
    private val virusScanner: VirusskannerService,
    private val objectMapper: ObjectMapper,
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(DokumentService::class.java)
    }

    fun hentDokument(
        dokumentId: String,
        dokumentEier: DokumentEier,
    ): Dokument? {
        logger.trace("Henter dokument {}.", dokumentId)
        val value = storage.hent(
            genererLagringsnøkkel(
                dokumentId = dokumentId,
                eier = dokumentEier
            )
        ) ?: return null

        logger.trace("Fant dokument, dekrypterer.")

        val decrypted = krypteringService.dekrypter(
            id = dokumentId,
            encrypted = value.value,
            dokumentEier = dokumentEier
        )

        logger.trace("Dekryptert, mapper til dokument.")

        return objectMapper.readValue(decrypted, Dokument::class.java)
    }

    suspend fun hentDokumenter(dokumentIder: List<String>, dokumentEier: DokumentEier): List<Dokument> {
        return coroutineScope {
            val deferred: List<Deferred<Dokument?>> = dokumentIder.map { dokumentId: String ->
                async {
                    hentDokument(dokumentId, dokumentEier)
                }
            }
            deferred.awaitAll().filterNotNull()
        }
    }

    fun slettDokument(
        dokumentId: String,
        dokumentEier: DokumentEier,
    ): Boolean {
        logger.trace("Sletter dokument {}", dokumentId)
        val result = storage.slett(
            genererLagringsnøkkel(
                dokumentId = dokumentId,
                eier = dokumentEier
            )
        )
        if (!result) logger.warn("Fant ikke noe dokument å slette.")
        return result
    }

    internal suspend fun slettDokumenter(
        dokumentIder: List<String>,
        dokumentEier: DokumentEier,
    ) {
        coroutineScope {
            val deferred = dokumentIder.map { dokumentId ->
                async {
                    slettDokument(dokumentId, dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }

    fun persister(
        dokumentId: String,
        dokumentEier: DokumentEier,
    ): Boolean {
        logger.info("Setter metadata på dokument med id: {}", dokumentId)
        val key = genererLagringsnøkkel(dokumentId, dokumentEier)
        return storage.persister(key)
    }

    suspend fun persisterDokumenter(
        dokumentIder: List<String>,
        dokumentEier: DokumentEier,
    ) {
        coroutineScope {
            val deferred = dokumentIder.map { dokumentId ->
                async {
                    persister(dokumentId, dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }

    suspend fun lagreDokument(
        dokument: Dokument,
        dokumentEier: DokumentEier,
        medHold: Boolean = false,
    ): String {
        virusScanner.skann(dokument.content)

        logger.trace("Generer DokumentID")
        val dokumentId = genererDokumentId()
        logger.trace("DokumentID={}. Krypterer.", dokumentId)

        val encrypted = krypteringService.krypter(
            id = dokumentId,
            plainText = objectMapper.writeValueAsString(dokument),
            dokumentEier = dokumentEier
        )

        logger.trace("Larer dokument.")

        storage.lagre(
            key = genererLagringsnøkkel(dokumentId = dokumentId, eier = dokumentEier),
            value = StorageValue(value = encrypted),
            hold = medHold
        )

        logger.trace("Lagring OK.")

        return dokumentId
    }

    fun fjerneHoldPåPersistertDokument(
        dokumentId: String,
        dokumentEier: DokumentEier,
    ): Boolean {
        return storage.fjerneHold(
            genererLagringsnøkkel(
                dokumentId = dokumentId,
                eier = dokumentEier
            )
        )
    }

    suspend fun fjernHoldPåPersisterteDokumenter(
        dokumentIder: List<String>,
        dokumentEier: DokumentEier,
    ) {
        coroutineScope {
            val deferred = dokumentIder.map { dokumentId ->
                async {
                    fjerneHoldPåPersistertDokument(dokumentId, dokumentEier)
                }
            }
            deferred.awaitAll()
        }
    }

    private fun genererLagringsnøkkel(
        dokumentId: String,
        eier: DokumentEier,
    ): StorageKey {
        logger.trace("Genrerer Storage Key for {}. Krypterer.", dokumentId)
        val plainText = "${eier.eiersFødselsnummer}-${dokumentId}"
        val encrypted = krypteringService.krypter(
            id = dokumentId,
            plainText = plainText,
            dokumentEier = eier
        )
        logger.trace("Storage Key kryptert.")
        val storageKey = StorageKey(
            value = encrypted
        )
        logger.info("$storageKey")
        return storageKey
    }

    private fun genererDokumentId(): String = krypteringService.id()
}
