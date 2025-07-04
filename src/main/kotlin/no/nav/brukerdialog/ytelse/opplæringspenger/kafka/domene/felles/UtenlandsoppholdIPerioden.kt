package no.nav.brukerdialog.ytelse.opplæringspenger.kafka.domene.felles

data class UtenlandsoppholdIPerioden(
    val skalOppholdeSegIUtlandetIPerioden: Boolean,
    val opphold: List<Utenlandsopphold> = listOf(),
)
