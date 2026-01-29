package no.nav.brukerdialog.mellomlagring.soknad

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.config.Issuers
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/mellomlagring/{ytelse}")
@RequiredIssuers(
    ProtectedWithClaims(issuer = Issuers.TOKEN_X, claimMap = ["acr=Level4"])
)
class MellomlagringController(
    private val mellomlagringService: MellomlagringService,
    private val objectMapper: ObjectMapper,
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

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMellomlagring(@PathVariable ytelse: String): ResponseEntity<JsonNode> = runBlocking {
        val mellomlagring = mellomlagringService.hentMellomlagring(Ytelse.valueOf(ytelse))
        val body: JsonNode? = objectMapper.readTree(mellomlagring ?: "{}")
        ResponseEntity(
            body,
            HttpStatus.OK
        )
    }

    @DeleteMapping
    fun deleteMellomlagring(@PathVariable ytelse: String): ResponseEntity<Void> = runBlocking {
        mellomlagringService.slettMellomlagring(Ytelse.valueOf(ytelse))
        ResponseEntity(HttpStatus.ACCEPTED)
    }
}
