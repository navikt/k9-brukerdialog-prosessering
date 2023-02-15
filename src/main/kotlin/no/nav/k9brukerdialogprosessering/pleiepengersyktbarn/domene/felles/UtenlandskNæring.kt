package no.nav.k9brukerdialogprosessering.pleiepengersyktbarn.domene.felles

import java.time.LocalDate

class UtenlandskNæring(
    val næringstype: Næringstyper,
    val navnPåVirksomheten: String,
    val land: Land,
    val organisasjonsnummer: String? = null,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate? = null
)
