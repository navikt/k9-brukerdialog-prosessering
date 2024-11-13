package no.nav.brukerdialog.utils

import com.nimbusds.jwt.SignedJWT
import no.nav.brukerdialog.domenetjenester.innsending.Innsending
import no.nav.brukerdialog.ytelse.Ytelse
import no.nav.brukerdialog.config.JacksonConfiguration
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

object MockMvcUtils {
    fun MockMvc.sendInnSøknad(innsending: Innsending, token: SignedJWT) {
        val ytelse = innsending.ytelse()
        val ytelseUrl: String = when (ytelse) {
            Ytelse.PLEIEPENGER_SYKT_BARN -> "/pleiepenger-sykt-barn"
            Ytelse.OMSORGSPENGER_UTVIDET_RETT -> "/omsorgspenger-utvidet-rett"
            Ytelse.OMSORGSPENGER_MIDLERTIDIG_ALENE -> "/omsorgspenger-midlertidig-alene"
            Ytelse.ETTERSENDING -> "/ettersending"
            Ytelse.OMSORGSDAGER_ALENEOMSORG -> "/omsorgsdager-aleneomsorg"
            Ytelse.OMSORGSPENGER_UTBETALING_ARBEIDSTAKER -> "/omsorgspenger-utbetaling-arbeidstaker"
            Ytelse.OMSORGSPENGER_UTBETALING_SNF -> "/omsorgspenger-utbetaling-snf"
            Ytelse.PLEIEPENGER_LIVETS_SLUTTFASE -> "/pleiepenger-livets-sluttfase"
            Ytelse.ETTERSENDING_PLEIEPENGER_SYKT_BARN -> "/ettersending-pleiepenger-sykt-barn"
            Ytelse.ETTERSENDING_PLEIEPENGER_LIVETS_SLUTTFASE -> "/ettersending-pleiepenger-livets-sluttfase"
            Ytelse.ETTERSENDING_OMP -> "/ettersending-omp"
            Ytelse.ENDRINGSMELDING_PLEIEPENGER_SYKT_BARN -> "/endringsmelding-pleiepenger-sykt-barn"
            Ytelse.DINE_PLEIEPENGER -> "/dine-pleiepenger"
            Ytelse.UNGDOMSYTELSE -> "/ungdomsytelse/soknad"
            Ytelse.OPPLARINGSPENGER -> "/opplaringspenger"
        }

        post("$ytelseUrl/innsending") {
            headers {
                set(NavHeaders.BRUKERDIALOG_YTELSE, ytelse.dialog)
                set(NavHeaders.BRUKERDIALOG_GIT_SHA, UUID.randomUUID().toString())
                setBearerAuth(token.serialize())
            }
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = JacksonConfiguration.configureObjectMapper().writeValueAsString(innsending)
        }.andExpect {
            status {
                isAccepted()
                header { exists(NavHeaders.X_CORRELATION_ID) }
            }
        }
    }
}
