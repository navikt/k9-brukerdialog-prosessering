package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengerutbetalingsnf.domene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import no.nav.k9brukerdialogprosessering.utils.krever
import no.nav.k9brukerdialogprosessering.utils.kreverIkkeNull
import java.time.LocalDate

class Frilans(
    @JsonFormat(pattern = "yyyy-MM-dd") private val startdato: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val sluttdato: LocalDate? = null,
    private val jobberFortsattSomFrilans: Boolean? = null,
) {
    internal fun valider(felt: String) = mutableListOf<String>().apply {
        kreverIkkeNull(jobberFortsattSomFrilans, "$felt.jobberFortsattSomFrilans kan ikke være null.")
        sluttdato?.let {
            krever(
                sluttdato.erLikEllerEtter(startdato),
                "$felt.sluttdato må være lik eller etter startdato."
            )
        }
        jobberFortsattSomFrilans?.let {
            if (!jobberFortsattSomFrilans) kreverIkkeNull(
                sluttdato,
                "$felt.sluttdato kan ikke være null dersom jobberFortsattSomFrilans=false."
            )
        }
    }

    fun somK9Frilanser() = Frilanser().apply {
        medStartdato(this@Frilans.startdato)
        this@Frilans.sluttdato?.let { medSluttdato(this@Frilans.sluttdato) }
    }
}
