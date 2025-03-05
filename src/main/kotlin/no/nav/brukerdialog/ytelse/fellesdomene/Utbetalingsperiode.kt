package no.nav.brukerdialog.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotEmpty
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.type.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.brukerdialog.utils.erLikEllerEtter
import java.time.Duration
import java.time.LocalDate
import no.nav.k9.søknad.felles.fravær.AktivitetFravær as K9AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværÅrsak as K9FraværÅrsak

class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") private val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") private val tilOgMed: LocalDate,

    private val antallTimerBorte: Duration? = null,
    private val antallTimerPlanlagt: Duration? = null,
    private val årsak: FraværÅrsak? = null,

    @field:NotEmpty(message = "Kan ikke være tom")
    private val aktivitetFravær: List<AktivitetFravær>,
) {
    companion object {

        internal fun List<Utbetalingsperiode>.somK9FraværPeriode(
            søknadÅrsak: SøknadÅrsak? = null,
            organisasjonsnummer: String? = null,
        ) = this.map { it.somK9FraværPeriode(søknadÅrsak, organisasjonsnummer) }
    }

    @Hidden
    @AssertTrue(message = "'tilOgMed' må være lik eller etter 'fraOgMed'")
    fun isTilOgMed(): Boolean {
        return tilOgMed.erLikEllerEtter(fraOgMed)
    }

    @Hidden
    @AssertTrue(message = "Dersom antallTimerBorte er satt må antallTimerPlanlagt være satt")
    fun isAntallTimerPlanlagt(): Boolean {
        if (antallTimerBorte != null) {
            return antallTimerPlanlagt != null
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "Dersom antallTimerPlanlagt er satt må antallTimerBorte være satt")
    fun isAntallTimerBorte(): Boolean {
        if (antallTimerPlanlagt != null) {
            return antallTimerBorte != null
        }
        return true
    }

    @Hidden
    @AssertTrue(message = "antallTimerBorte kan ikke være større enn antallTimerPlanlagt")
    fun isAntallTimerPlanlagtStørreEnnAntallTimerBorte(): Boolean {
        if (antallTimerPlanlagt != null && antallTimerBorte != null) {
            return antallTimerPlanlagt >= antallTimerBorte
        }
        return true
    }

    internal fun somK9FraværPeriode(
        søknadÅrsak: SøknadÅrsak? = null,
        organisasjonsnummer: String? = null,
    ) = FraværPeriode()
        .medPeriode(Periode(fraOgMed, tilOgMed))
        .medFraværÅrsak(årsak?.somK9FraværÅrsak() ?: K9FraværÅrsak.ORDINÆRT_FRAVÆR)
        .medSøknadsårsak(søknadÅrsak)
        .medAktivitetFravær(aktivitetFravær.map { it.somK9AktivitetFravær() })
        .medArbeidsgiverOrgNr(Organisasjonsnummer.of(organisasjonsnummer))
        .medNormalarbeidstid(antallTimerPlanlagt)
        .medFravær(antallTimerBorte)

}

enum class FraværÅrsak {
    STENGT_SKOLE_ELLER_BARNEHAGE,
    SMITTEVERNHENSYN,
    ORDINÆRT_FRAVÆR;

    fun somK9FraværÅrsak() = when (this) {
        STENGT_SKOLE_ELLER_BARNEHAGE -> K9FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE
        SMITTEVERNHENSYN -> K9FraværÅrsak.SMITTEVERNHENSYN
        ORDINÆRT_FRAVÆR -> K9FraværÅrsak.ORDINÆRT_FRAVÆR
    }
}

enum class AktivitetFravær {
    ARBEIDSTAKER,
    FRILANSER,
    SELVSTENDIG_VIRKSOMHET;

    fun somK9AktivitetFravær() = when (this) {
        ARBEIDSTAKER -> K9AktivitetFravær.ARBEIDSTAKER
        FRILANSER -> K9AktivitetFravær.FRILANSER
        SELVSTENDIG_VIRKSOMHET -> K9AktivitetFravær.SELVSTENDIG_VIRKSOMHET
    }
}
