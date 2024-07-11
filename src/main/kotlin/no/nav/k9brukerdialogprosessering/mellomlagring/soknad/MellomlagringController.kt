package no.nav.k9brukerdialogprosessering.mellomlagring.soknad

import kotlinx.coroutines.runBlocking
import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse
import no.nav.k9brukerdialogprosessering.config.Issuers
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mellomlagring")
@ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
class MellomlagringController(
    private val mellomlagringService: MellomlagringService,
) {

    @PostMapping
    fun createMellomlagring(
        @RequestParam ytelse: String,
        @RequestBody mellomlagring: Map<String, Any>
    ): ResponseEntity<Unit> = runBlocking {
        try {
            mellomlagringService.settMellomlagring(
                Ytelse.valueOf(ytelse),
                JSONObject(mellomlagring).toString()
            )
            ResponseEntity(HttpStatus.CREATED)
        } catch (e: CacheConflictException) {
            ResponseEntity(HttpStatus.CONFLICT)
        }
    }

    @PutMapping
    fun updateMellomlagring(
        @RequestParam ytelse: String,
        @RequestBody mellomlagring: Map<String, Any>
    ): ResponseEntity<Unit> = runBlocking {
        try {
            mellomlagringService.oppdaterMellomlagring(
                Ytelse.valueOf(ytelse),
                JSONObject(mellomlagring).toString()
            )
            ResponseEntity(HttpStatus.NO_CONTENT)
        } catch (e: CacheNotFoundException) {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping
    fun getMellomlagring(@RequestParam ytelse: String): ResponseEntity<String> = runBlocking {
        val mellomlagring = mellomlagringService.hentMellomlagring(Ytelse.valueOf(ytelse))
        ResponseEntity(
            mellomlagring ?: "{}",
            HttpStatus.OK
        )
    }

    @DeleteMapping
    fun deleteMellomlagring(@RequestParam ytelse: String): ResponseEntity<Void> = runBlocking {
        mellomlagringService.slettMellomlagring(Ytelse.valueOf(ytelse))
        ResponseEntity(HttpStatus.ACCEPTED)
    }
}
