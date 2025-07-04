package no.nav.brukerdialog.domenetjenester.innsending

import io.mockk.every
import io.mockk.mockk
import no.nav.brukerdialog.utils.TokenTestUtils.mockContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.ErrorResponseException

class InnsendingServiceTest {

    @Test
    fun `Gitt søker på innsending ikke samme som innlogget bruker, forvent feil`() {
        val tokenValidationContextHolderMock = mockk<SpringTokenValidationContextHolder>()
        tokenValidationContextHolderMock.mockContext() // Har bruker 123456789 i context

        val innsending = mockk<Innsending>()
        every { innsending.søkerNorskIdent() } returns "987654321"

        val innsendingService = InnsendingService(
            søkerService = mockk(),
            kafkaProdusent = mockk(),
            dokumentService = mockk(),
            springTokenValidationContextHolder = tokenValidationContextHolderMock,
        )

        assertThrows<ErrorResponseException> {
            innsendingService.forsikreInnloggetBrukerErSøker(innsending)
        }
    }
}
