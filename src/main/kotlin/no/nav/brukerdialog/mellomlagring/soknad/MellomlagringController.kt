package no.nav.brukerdialog.mellomlagring.soknad

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.api.ytelse.Ytelse
import no.nav.brukerdialog.config.Issuers
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mellomlagring/{ytelse}")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class MellomlagringController(
    private val mellomlagringService: MellomlagringService,
) {

    @PostMapping
    fun createMellomlagring(
        @PathVariable ytelse: String,
        @RequestBody mellomlagring: Map<String, Any>,
    ): ResponseEntity<Unit> = runBlocking {
        mellomlagringService.settMellomlagring(
            Ytelse.valueOf(ytelse),
            JSONObject(mellomlagring).toString()
        )
        ResponseEntity(HttpStatus.CREATED)

    }

    @PutMapping
    fun updateMellomlagring(
        @PathVariable ytelse: String,
        @RequestBody mellomlagring: Map<String, Any>,
    ): ResponseEntity<Unit> = runBlocking {

        mellomlagringService.oppdaterMellomlagring(
            Ytelse.valueOf(ytelse),
            JSONObject(mellomlagring).toString()
        )
        ResponseEntity(HttpStatus.NO_CONTENT)

    }

    @GetMapping
    fun getMellomlagring(@PathVariable ytelse: String): ResponseEntity<String> = runBlocking {
        val mellomlagring = mellomlagringService.hentMellomlagring(Ytelse.valueOf(ytelse))
        ResponseEntity(
            mellomlagring ?: "{}",
            HttpStatus.OK
        )
    }

    @DeleteMapping
    fun deleteMellomlagring(@PathVariable ytelse: String): ResponseEntity<Void> = runBlocking {
        mellomlagringService.slettMellomlagring(Ytelse.valueOf(ytelse))
        ResponseEntity(HttpStatus.ACCEPTED)
    }
}
