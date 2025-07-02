package no.nav.brukerdialog.mellomlagring.dokument

import kotlinx.coroutines.runBlocking
import no.nav.brukerdialog.K9brukerdialogprosesseringApplication
import no.nav.brukerdialog.utils.FilUtils.hentFil
import no.nav.brukerdialog.utils.FødselsnummerGenerator
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.apache.commons.lang3.stream.Streams
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.file.Files

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [K9brukerdialogprosesseringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWireMock
class DokumentServiceTest {

    private companion object {
        private val logger = LoggerFactory.getLogger(DokumentServiceTest::class.java)
    }

    @Autowired
    lateinit var dokumentService: DokumentService

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Bilde_3_MB.jpg",
            "test.pdf",
            "nav-logo.png"
        ]
    )
    fun `Lagring, henting og sletting av enklelt dokumenter fungerer`(filnavn: String) {
        // Gitt dokument...
        val dokumentEier = DokumentEier(FødselsnummerGenerator.neste())
        val fil = hentFil(filnavn)
        val dokument = Dokument(
            title = fil.name,
            content = fil.readBytes(),
            contentType = Files.probeContentType(fil.toPath()),
            eier = DokumentEier("12345678910")
        )

        runBlocking {
            // når den lagres...
            val dokumentId = dokumentService.lagreDokument(
                dokument = dokument,
                dokumentEier = dokumentEier,
                skannForVirus = false
            )

            // så skal dokumentet kunne hentes ut igjen...
            val hentetDokument = dokumentService.hentDokument(
                dokumentId = dokumentId,
                dokumentEier = dokumentEier
            )
            assertThat(hentetDokument).isEqualTo(dokument)

            // og slettes uten problemer
            val slettet = dokumentService.slettDokument(
                dokumentId = dokumentId,
                dokumentEier = dokumentEier
            )
            assertThat(slettet).isTrue()
        }
    }

    @Test
    fun `Lagring av, henting, og sletting av flere dokumenter fungerer`() {
        // Gitt flere dokumenter...
        val dokumentEier = DokumentEier(FødselsnummerGenerator.neste())
        val dokumentIder = Streams.of(
            hentFil("Bilde_3_MB.jpg"),
            hentFil("test.pdf"),
            hentFil("nav-logo.png")
        ).map {
            Dokument(
                title = it.name,
                content = it.readBytes(),
                contentType = Files.probeContentType(it.toPath()),
                eier = dokumentEier
            )
        }.map { dokument ->
            runBlocking {
                // når de lagres...
                dokumentService.lagreDokument(
                    dokument = dokument,
                    dokumentEier = dokumentEier,
                    skannForVirus = false
                )
            }
        }.toList()

        runBlocking {
            // så skal de kunne hentes ut igjen...
            val dokumenter = dokumentService.hentDokumenter(dokumentIder, dokumentEier)
            assertThat(dokumenter).hasSize(3)

            // og slettes uten problemer
            dokumentService.slettDokumenter(dokumentIder, dokumentEier)
            assertThat(dokumentService.hentDokumenter(dokumentIder, dokumentEier)).isEmpty()
        }
    }

    @Test
    fun `Persistering av ikke-eksisterende dokument feiler`() {
        // Gitt et dokument som ikke er lagret
        val dokumentEier = DokumentEier(FødselsnummerGenerator.neste())

        // Når vi prøver å persistere det
        assertThrows<IllegalStateException> {
            dokumentService.persister(
                dokumentId = "ikke-eksisterende-dokument",
                dokumentEier = dokumentEier
            )
        }
    }

    @Test
    fun `Persistering av dokument returnerer true`() {
        // Gitt et dokument som er lagret
        val dokumentEier = DokumentEier(FødselsnummerGenerator.neste())
        val fil = hentFil("test.pdf")
        val dokument = Dokument(
            title = fil.name,
            content = fil.readBytes(),
            contentType = Files.probeContentType(fil.toPath()),
            eier = dokumentEier
        )

        runBlocking {
            val dokumentId = dokumentService.lagreDokument(
                dokument = dokument,
                dokumentEier = dokumentEier,
                skannForVirus = false,
            )

            // Når vi persisterer det
            val result = dokumentService.persister(
                dokumentId = dokumentId,
                dokumentEier = dokumentEier
            )

            // Så skal det returnere true
            assertThat(result).isTrue()
        }
    }
}
