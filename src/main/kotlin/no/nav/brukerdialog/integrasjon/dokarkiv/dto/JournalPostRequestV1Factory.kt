package no.nav.brukerdialog.integrasjon.dokarkiv.dto

import no.nav.brukerdialog.mellomlagring.dokument.Dokument
import org.springframework.http.MediaType
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object JournalPostRequestV1Factory {

    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    internal fun instance(
        kanal: Kanal = Kanal.NAV_NO,
        journalposttype: JournalPostType = JournalPostType.INNGAAENDENDE,
        mottaker: String,
        dokumenter: List<List<Dokument>>,
        datoMottatt: ZonedDateTime,
        ytelseType: YtelseType,
        avsenderMottakerIdType: AvsenderMottakerIdType,
        avsenderMottakerNavn: String,
        eksternReferanseId: String
    ) : DokarkivJournalpostRequest {

        if (dokumenter.isEmpty()) {
            throw IllegalStateException("Det må sendes minst ett dokument")
        }

        val vedlegg = mutableListOf<JoarkDokument>()

        dokumenter.forEach { dokumentBolk ->
                vedlegg.add(mapDokument(dokumentBolk, ytelseType.brevkode))
        }

        return DokarkivJournalpostRequest(
            journalposttype = journalposttype.kode,
            avsenderMottaker = AvsenderMottaker(mottaker, avsenderMottakerIdType.kode, avsenderMottakerNavn), // I Versjon 1 er det kun innlogget bruker som laster opp vedlegg og fyller ut søknad, så bruker == avsender
            bruker = Bruker(mottaker, avsenderMottakerIdType.kode),
            tema = ytelseType.tema.kode,
            tittel = ytelseType.tittel,
            kanal = kanal.kode,
            journalfoerendeEnhet = "9999", //  NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen. Ved automatisk journalføring uten mennesker involvert skal enhet settes til "9999".
            datoMottatt = formatDate(datoMottatt),
            dokumenter = vedlegg,
            innsendingstype = ytelseType.innsendingstype,
            eksternReferanseId = eksternReferanseId
        )
    }

    private fun formatDate(dateTime: ZonedDateTime) : String {
        val utc = ZonedDateTime.ofInstant(dateTime.toInstant(), ZoneOffset.UTC)
        return DATE_TIME_FORMATTER.format(utc)
    }

    private fun mapDokument(dokumentBolk : List<Dokument>, typeReferanse: TypeReferanse) : JoarkDokument {
        val title = dokumentBolk.first().title
        val dokumenterVarianter = mutableListOf<DokumentVariant>()

        dokumentBolk.forEach {
            val arkivFilType = getArkivFilType(it)
            dokumenterVarianter.add(
                DokumentVariant(
                    filtype = arkivFilType,
                    variantformat = getVariantFormat(arkivFilType),
                    fysiskDokument = it.content
                )
            )
        }

        when (typeReferanse) {
            is DokumentType -> {
                return JoarkDokument(
                    tittel = title,
                    dokumentVarianter = dokumenterVarianter.toList()
                )
            }
            is BrevKode -> {
                return JoarkDokument(
                    tittel = title,
                    brevkode = typeReferanse.brevKode,
                    dokumentkategori = typeReferanse.dokumentKategori,
                    dokumentVarianter = dokumenterVarianter.toList()
                )
            }
            else -> error("Ikke støtttet type referense ${typeReferanse.javaClass.simpleName}")
        }
    }

    private fun getArkivFilType(dokument: Dokument) : ArkivFilType {
        if (MediaType.APPLICATION_PDF_VALUE == dokument.contentType) return ArkivFilType.PDFA
        if (MediaType.APPLICATION_JSON_VALUE == dokument.contentType) return ArkivFilType.JSON
        if (MediaType.APPLICATION_XML_VALUE == dokument.contentType) return ArkivFilType.XML
        throw IllegalStateException("Ikke støttet Content-Type '${dokument.contentType}'")
    }

    private fun getVariantFormat(arkivFilType: ArkivFilType) : VariantFormat {
        return if (arkivFilType == ArkivFilType.PDFA) VariantFormat.ARKIV else VariantFormat.ORIGINAL
    }
}
