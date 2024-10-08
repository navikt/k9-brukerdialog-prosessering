package no.nav.brukerdialog.utils

import no.nav.k9.ettersendelse.Ettersendelse
import no.nav.k9.søknad.JsonUtils
import no.nav.k9.søknad.Søknad
import no.nav.brukerdialog.common.MetaInfo
import no.nav.brukerdialog.config.JacksonConfiguration
import no.nav.brukerdialog.oppslag.soker.Søker
import java.time.LocalDate
import java.util.*

class SøknadUtils {
    companion object{
        val objectMapper = JacksonConfiguration.configureObjectMapper()
        fun Søknad.somJson(): String = JsonUtils.toString(this)
        fun Ettersendelse.somJson(): String = JsonUtils.toString(this)


        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("1999-11-02"),
            fornavn = "MOR",
            etternavn = "MORSEN",
            fødselsnummer = "02119970078"
        )

        val metadata = MetaInfo(
            version = 1,
            correlationId = UUID.randomUUID().toString(),
            soknadDialogCommitSha = "abc-123"
        )
    }
}
