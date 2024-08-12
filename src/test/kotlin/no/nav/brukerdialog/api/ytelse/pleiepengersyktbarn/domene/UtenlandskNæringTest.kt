package no.nav.brukerdialog.api.ytelse.pleiepengersyktbarn.domene

import no.nav.brukerdialog.api.ytelse.fellesdomene.Land
import no.nav.brukerdialog.api.ytelse.fellesdomene.Næringstype
import no.nav.brukerdialog.api.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandskNæring
import no.nav.brukerdialog.api.ytelse.pleiepengersyktbarn.soknad.domene.UtenlandskNæring.Companion.valider
import no.nav.brukerdialog.utils.TestUtils.verifiserIngenValideringsFeil
import no.nav.brukerdialog.utils.TestUtils.verifiserValideringsFeil
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UtenlandskNæringTest {

    @Test
    fun `Gyldig utenlandskNæring gir ingen valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("utenlandskNæring[0]").verifiserIngenValideringsFeil()
    }

    @Test
    fun `UtenlandskNæring med ugyldig land gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("ABC", " "),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-01"),
            tilOgMed = LocalDate.parse("2022-03-01")
        ).valider("utenlandskNæring[0]").verifiserValideringsFeil(2,
            listOf(
                "utenlandskNæring[0].land.landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring[0].land.landnavn kan ikke være tomt eller blankt."
            )
        )
    }

    @Test
    fun `UtenlandskNæring med tilOgMed før fraOgMed gir valideringsfeil`() {
        UtenlandskNæring(
            næringstype = Næringstype.JORDBRUK_SKOGBRUK,
            navnPåVirksomheten = "Flush AS",
            land = Land("NLD", "Nederland"),
            organisasjonsnummer = "123ABC",
            fraOgMed = LocalDate.parse("2022-01-05"),
            tilOgMed = LocalDate.parse("2022-01-01")
        ).valider("utenlandskNæring[0]").verifiserValideringsFeil(1,
            listOf("utenlandskNæring[0].tilOgMed må være lik eller etter fraOgMed")
        )
    }

    @Test
    fun `Liste med UtenlandskNæring som har feil i land gir valideringsfeil`(){
        listOf(
            UtenlandskNæring(
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "Flush AS",
                land = Land("CBA", "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-01"),
                tilOgMed = LocalDate.parse("2022-01-03")
            ),
            UtenlandskNæring(
                næringstype = Næringstype.JORDBRUK_SKOGBRUK,
                navnPåVirksomheten = "Flush AS",
                land = Land("ABC", "Nederland"),
                organisasjonsnummer = "123ABC",
                fraOgMed = LocalDate.parse("2022-01-05")
            )
        ).valider("utenlandskNæring").verifiserValideringsFeil(2,
            listOf(
                "utenlandskNæring[0].land.landkode 'CBA' er ikke en gyldig ISO 3166-1 alpha-3 kode.",
                "utenlandskNæring[1].land.landkode 'ABC' er ikke en gyldig ISO 3166-1 alpha-3 kode."
            )
        )
    }
}
