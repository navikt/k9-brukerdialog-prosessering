package no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.brukerdialog.common.Constants.DATE_FORMATTER
import no.nav.brukerdialog.common.Constants.DATE_TIME_FORMATTER
import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.PSBMottattSøknad
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Barn
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.BarnRelasjon
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Beredskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.FerieuttakIPerioden
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Frilans
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Medlemskap
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.Nattevåk
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.SelvstendigNæringsdrivende
import no.nav.brukerdialog.meldinger.pleiepengersyktbarn.domene.felles.UtenlandsoppholdIPerioden
import no.nav.brukerdialog.utils.DateUtils.somNorskDag
import no.nav.brukerdialog.utils.K9FormatUtils
import no.nav.brukerdialog.ytelse.fellesdomene.Søker
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PSBSøknadPdfDataMapper.mapInnsendingsdetaljer
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PSBSøknadPdfDataMapper.mapPSBSøknadPdfData
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PSBSøknadPdfDataMapper.mapPerioder
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PSBSøknadPdfDataMapper.mapRelasjonTilBarnet
import no.nav.brukerdialog.ytelse.pleiepengersyktbarn.søknad.pdf.PSBSøknadPdfDataMapper.mapSøker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZonedDateTime

class PSBSøknadPdfDataMapperTest {
    @Test
    fun `mapInnsendingsdetaljer skal returnere riktig objekt`() {
        // Arrange
        val mottatt: ZonedDateTime = ZonedDateTime.now()
        val mottatTekst = "${mottatt.withZoneSameInstant(OSLO_ZONE_ID).somNorskDag()} ${DATE_TIME_FORMATTER.format(mottatt)}"

        // Act
        val resultat = mapInnsendingsdetaljer(mottatt)

        // Assert
        assertThat(resultat.verdiliste?.first()?.verdi).isEqualTo(mottatTekst)
    }

    @Test
    fun `mapSøker returnerer en Verdiliste med søker og barn`() {
        // Arrange
        val søker =
            Søker(
                aktørId = "1234",
                fornavn = "Ola",
                etternavn = "Nordmann",
                fødselsnummer = "29099012345",
                fødselsdato = LocalDate.parse("1990-09-29"),
            )
        val barn = Barn(fødselsnummer = "02119970078", navn = "Ola JR", aktørId = "4321")

        // Act
        val resultat = mapSøker(søker, barn)

        // Assert
        assertThat(resultat.verdiliste?.size).isEqualTo(4)
        assertThat(resultat.verdiliste?.get(0)?.verdi).contains("Ola")
        assertThat(resultat.verdiliste?.get(1)?.verdi).isEqualTo("29099012345")
        assertThat(resultat.verdiliste?.get(2)?.verdi).contains("JR")
        assertThat(resultat.verdiliste?.get(3)?.verdi).isEqualTo("02119970078")
    }

    @Test
    fun `mapRelasjonTilBarnet skal returnere objekt med relasjon som utskriftstreng og begrunnelse`() {
        // Arrange
        val relasjonTilBarnet = BarnRelasjon.FAR
        val barnRelasjonsBeskrivelse = "Blabla"

        // Act
        val resultat = mapRelasjonTilBarnet(relasjonTilBarnet, barnRelasjonsBeskrivelse)

        // Assert
        assertThat(resultat?.verdiliste?.get(0)?.verdi).isEqualTo("Du er far til barnet")
        assertThat(resultat?.verdiliste?.get(1)?.verdi).isEqualTo("Blabla")
    }

    @Test
    fun `mapRelasjonTilBarnet skal returnere null dersom relasjon er null`() {
        // Arrange
        val relasjonTilBarnet = null
        val barnRelasjonsBeskrivelse = "Blabla"

        // Act
        val resultat = mapRelasjonTilBarnet(relasjonTilBarnet, barnRelasjonsBeskrivelse)

        // Assert
        assertThat(resultat).isNull()
    }

    @Test
    fun `mapPerioder skal returnere objekt med riktig periode`() {
        // Arrange
        val fraOgMed = LocalDate.now().minusYears(1)
        val tilOgMed = LocalDate.now()
        val periodeTekst = "${DATE_FORMATTER.format(fraOgMed)} - ${DATE_FORMATTER.format(tilOgMed)}"

        // Act
        val resultat = mapPerioder(fraOgMed, tilOgMed)

        // Assert
        assertThat(resultat.verdiliste?.get(0)?.verdi).isEqualTo(periodeTekst)
    }

    @Test
    fun `mapArbeidsgivere returnerer riktig dersom ingen arbeidsgivere`() {
        // Arrange
//        val arbeidsgivere = listOf()

        // Act
        // Assert
    }

    @Test
    fun `PSBSøknad skal returnere riktig feltmap dersom visse verdier ikke er tilstede`() {
        // Arrange
        val soknadsId: String = "test-soknad-id"
        val mottatt: ZonedDateTime = ZonedDateTime.now()
        val søknad =
            PSBMottattSøknad(
                søknadId = "test-søknad-id",
                mottatt = ZonedDateTime.now(),
                språk = "nb",
                fraOgMed = LocalDate.now().plusDays(6),
                tilOgMed = LocalDate.now().plusDays(35),
                søker =
                    Søker(
                        aktørId = "1234",
                        fornavn = "Ola",
                        etternavn = "Nordmann",
                        fødselsnummer = "29099012345",
                        fødselsdato = LocalDate.parse("1990-09-29"),
                    ),
                barn = Barn(fødselsnummer = "02119970078", navn = "Ola JR", aktørId = "4321"),
                medlemskap = Medlemskap(harBoddIUtlandetSiste12Mnd = false, skalBoIUtlandetNeste12Mnd = false),
                utenlandsoppholdIPerioden = UtenlandsoppholdIPerioden(skalOppholdeSegIUtlandetIPerioden = false),
                ferieuttakIPerioden = FerieuttakIPerioden(skalTaUtFerieIPerioden = false, ferieuttak = listOf()),
                opptjeningIUtlandet = listOf(),
                utenlandskNæring = listOf(),
                harForståttRettigheterOgPlikter = true,
                harBekreftetOpplysninger = true,
                nattevåk = Nattevåk(harNattevåk = false, tilleggsinformasjon = null),
                frilans = Frilans(harInntektSomFrilanser = false),
                selvstendigNæringsdrivende = SelvstendigNæringsdrivende(harInntektSomSelvstendig = false),
                arbeidsgivere = listOf(),
                k9FormatSøknad = K9FormatUtils.defaultK9FormatPSB(soknadsId, mottatt),
                beredskap = Beredskap(false, null),
            )

        // Act

        val mappetSøknad = mapPSBSøknadPdfData("Test søknad", søknad)

        // Assert
        println(jacksonObjectMapper().writeValueAsString(mappetSøknad))
    }
}
