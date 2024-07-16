package no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.domene

import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Enkeltdag
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.Omsorgstilbud
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFortid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.OmsorgstilbudSvarFremtid
import no.nav.k9brukerdialogprosessering.api.ytelse.pleiepengersyktbarn.soknad.domene.PlanUkedager
import no.nav.k9brukerdialogprosessering.utils.TestUtils.assertFeilPå
import no.nav.k9brukerdialogprosessering.utils.TestUtils.verifiserIngenFeil
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate


class OmsorgstilbudValideringTest {
    private companion object {
        val felt = "omsorgstilbud"
        val gyldigOmsorgstilbud = Omsorgstilbud(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = OmsorgstilbudSvarFremtid.JA,
            erLiktHverUke = true,
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(3)
            ),
            enkeltdager = null
        )
    }

    @Test
    fun `Gyldig omsorgstilbud gir ingen feil`() {
        gyldigOmsorgstilbud.valider(felt).verifiserIngenFeil()
    }

    @Test
    fun `Skal gi feil dersom svarFortid=JA, svarFremtid=JA og både ukedager og enkeldager er null`() {
        gyldigOmsorgstilbud.copy(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = OmsorgstilbudSvarFremtid.JA,
            ukedager = null,
            enkeltdager = null
        ).valider(felt).assertFeilPå(
            listOf(
                "Ved omsorgstilbud.svarFortid=JA kan ikke både omsorgstilbud.enkeltdager og omsorgstilbud.ukedager være null.",
                "Ved omsorgstilbud.svarFremtid=JA kan ikke både omsorgstilbud.enkeltdager og omsorgstilbud.ukedager være null.",
                "Hvis erLiktHverUke er true må ukedager være satt."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom både ukedager og enkeldager er satt`() {
        gyldigOmsorgstilbud.copy(
            ukedager = PlanUkedager(
                mandag = Duration.ofHours(3)
            ),
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).valider(felt).assertFeilPå(
            listOf(
                "Kan ikke ha både enkeltdager og ukedager satt, må velge en av de.",
                "Hvis erLiktHverUke er true må enkeldager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er true og ukedager er null`() {
        gyldigOmsorgstilbud.copy(
            svarFortid = OmsorgstilbudSvarFortid.JA,
            svarFremtid = OmsorgstilbudSvarFremtid.NEI,
            erLiktHverUke = true,
            ukedager = null,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).valider(felt).assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er true må ukedager være satt.",
                "Hvis erLiktHverUke er true må enkeldager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er true og enkeldager er satt`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverUke = true,
            enkeltdager = listOf(Enkeltdag(LocalDate.now(), Duration.ofHours(3)))
        ).valider(felt).assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er true må enkeldager være null.",
                "Kan ikke ha både enkeltdager og ukedager satt, må velge en av de."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er false og ukedager er satt`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverUke = false,
            ukedager = PlanUkedager(mandag = Duration.ofHours(2))
        ).valider(felt).assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er false kan ikke ukedager være satt.",
                "Hvis erLiktHverUke er false kan ikke enkeltdager være null."
            )
        )
    }

    @Test
    fun `Skal gi feil dersom erLiktHverUke er false og enkeltdager er null`() {
        gyldigOmsorgstilbud.copy(
            erLiktHverUke = false,
            enkeltdager = null
        ).valider(felt).assertFeilPå(
            listOf(
                "Hvis erLiktHverUke er false kan ikke enkeltdager være null.",
                "Hvis erLiktHverUke er false kan ikke ukedager være satt."
            )
        )
    }

}
