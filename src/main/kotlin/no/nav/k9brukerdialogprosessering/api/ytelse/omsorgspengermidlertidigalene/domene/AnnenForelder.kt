package no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.ANNET
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.FENGSEL
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.INNLAGT_I_HELSEINSTITUSJON
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.SYKDOM
import no.nav.k9brukerdialogprosessering.api.ytelse.omsorgspengermidlertidigalene.domene.Situasjon.UTØVER_VERNEPLIKT
import no.nav.k9brukerdialogprosessering.utils.StringUtils
import no.nav.k9brukerdialogprosessering.utils.erLikEllerEtter
import no.nav.k9brukerdialogprosessering.utils.krever
import no.nav.k9brukerdialogprosessering.utils.kreverIkkeNull
import java.time.LocalDate
import no.nav.k9.søknad.ytelse.omsorgspenger.utvidetrett.v1.AnnenForelder as K9AnnenForelder

class AnnenForelder(
    @Size(max = 11)
    @Pattern(regexp = "^\\d+$", message = "'\${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private val fnr: String,
    private val navn: String,
    private val situasjon: Situasjon,
    private val situasjonBeskrivelse: String? = null,
    private val periodeOver6Måneder: Boolean? = null,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val periodeFraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val periodeTilOgMed: LocalDate? = null,
) {

    internal fun somK9AnnenForelder(): K9AnnenForelder {
        return K9AnnenForelder()
            .medNorskIdentitetsnummer(NorskIdentitetsnummer.of(fnr))
            .medSituasjon(situasjon.somK9SituasjonType(), situasjonBeskrivelse?.let { StringUtils.saniter(it) })
            .apply {
                if (periodeTilOgMed != null) this.medPeriode(Periode(periodeFraOgMed, periodeTilOgMed))
            }
    }

    internal fun valider(felt: String) = mutableListOf<String>().apply {
        krever(navn.isNotBlank(), "$felt.navn kan ikke være tomt eller blank.")
        periodeTilOgMed?.let {
            krever(
                periodeTilOgMed.erLikEllerEtter(periodeFraOgMed),
                "$felt.periodeTilOgMed må være lik eller etter periodeFraOgMed."
            )
        }

        when (situasjon) {
            INNLAGT_I_HELSEINSTITUSJON -> validerGyldigPeriodeSatt(felt, situasjon)
            UTØVER_VERNEPLIKT, FENGSEL -> validerVærnepliktEllerFengsel(felt)
            SYKDOM, ANNET -> {
                validerGyldigPeriodeSatt(felt, situasjon)
                krever(
                    !situasjonBeskrivelse.isNullOrBlank(),
                    "$felt.situasjonBeskrivelse kan ikke være null eller tom dersom situasjon er $situasjon"
                )
            }
        }
    }

    private fun MutableList<String>.validerVærnepliktEllerFengsel(felt: String) =
        kreverIkkeNull(
            periodeTilOgMed,
            "$felt.periodeTilOgMed kan ikke være null dersom situasjonen er $FENGSEL eller $UTØVER_VERNEPLIKT"
        )

    private fun MutableList<String>.validerGyldigPeriodeSatt(felt: String, situasjon: Situasjon) {
        krever(
            periodeTilOgMed != null || periodeOver6Måneder != null,
            "$felt.periodeTilOgMed eller periodeOver6Måneder må være satt dersom situasjonen er $situasjon"
        )
    }
}
