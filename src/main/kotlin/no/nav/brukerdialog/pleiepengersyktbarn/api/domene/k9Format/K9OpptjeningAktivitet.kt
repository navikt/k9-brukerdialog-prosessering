package no.nav.brukerdialog.pleiepengersyktbarn.api.domene.k9Format

import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.Frilans
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.FrilansType
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.PleiepengerSyktBarnSøknad
import no.nav.brukerdialog.pleiepengersyktbarn.api.domene.arbeid.DAGER_PER_UKE
import no.nav.k9.søknad.felles.opptjening.Frilanser
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet

fun Double.tilTimerPerDag() = this.div(DAGER_PER_UKE)

internal fun PleiepengerSyktBarnSøknad.byggK9OpptjeningAktivitet(): OpptjeningAktivitet {
    val opptjeningAktivitet = OpptjeningAktivitet()
    if (selvstendigNæringsdrivende.harInntektSomSelvstendig) {
        opptjeningAktivitet.medSelvstendigNæringsdrivende(selvstendigNæringsdrivende.tilK9SelvstendigNæringsdrivende())
    }

    val erFrilanserMedInntenkt = frilans.harInntektSomFrilanser && frilans.type == FrilansType.FRILANS
    if (erFrilanserMedInntenkt) {
        opptjeningAktivitet.medFrilanser(frilans.tilK9Frilanser())
    }
    return opptjeningAktivitet
}

internal fun Frilans.tilK9Frilanser(): Frilanser {
    val frilanser = Frilanser()
    frilanser.medStartdato(startdato)
    sluttdato?.let { frilanser.medSluttdato(it) }
    return frilanser
}
