package no.nav.brukerdialog.integrasjon.dokarkiv

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import no.nav.brukerdialog.integrasjon.dokarkiv.dto.YtelseType
import org.json.JSONObject

internal class DokarkivResponseTransformer : ResponseTransformer() {
    companion object {
        val BREVKODE_MED_FORVENTET_JOURNALPOST_ID = mapOf(
            YtelseType.PLEIEPENGESØKNAD to "1",
            YtelseType.PLEIEPENGESØKNAD_ENDRINGSMELDING to "1",
            YtelseType.OMSORGSPENGESØKNAD to "2",
            YtelseType.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG to "3",
            YtelseType.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER to "4",
            YtelseType.OMSORGSPENGESØKNAD_OVERFØRING_AV_DAGER to "5",
            YtelseType.OMSORGSPENGEMELDING_DELING_AV_DAGER to "5",
            YtelseType.OPPLÆRINGSPENGERSØKNAD to "6",
            YtelseType.FRISINNSØKNAD to "7",
            YtelseType.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE to "8",
            YtelseType.PLEIEPENGESØKNAD_ETTERSENDING to "9",
            YtelseType.OMSORGSPENGESØKNAD_ETTERSENDING to "10",
            YtelseType.OMSORGSPENGESØKNAD_UTBETALING_FRILANSER_SELVSTENDIG_ETTERSENDING to "11",
            YtelseType.OMSORGSPENGESØKNAD_UTBETALING_ARBEIDSTAKER_ETTERSENDING to "12",
            YtelseType.OMSORGSPENGESØKNAD_MIDLERTIDIG_ALENE_ETTERSENDING to "13",
            YtelseType.OMSORGSPENGEMELDING_DELING_AV_DAGER_ETTERSENDING to "14",
            YtelseType.OMSORGSDAGER_ALENEOMSORG to "15",
            YtelseType.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE to "16",
            YtelseType.PLEIEPENGESØKNAD_LIVETS_SLUTTFASE_ETTERSENDING to "17",
            YtelseType.OMSORGSDAGER_ALENEOMSORG_ETTERSENDING to "18",
            YtelseType.UNGDOMSYTELSE_SØKNAD to "19", // TODO Bruk Tema.UNGDOMSYTELSE før lansering
            YtelseType.UNGDOMSYTELSE_INNTEKTRAPPORTERING to "20", // TODO Bruk Tema.UNGDOMSYTELSE før lansering
            YtelseType.OPPLÆRINGSPENGERSØKNAD_ETTERSENDING to "21",
        )
    }

    override fun getName(): String {
        return "dokarkiv"
    }

    override fun transform(
        request: Request,
        response: Response,
        files: FileSource?,
        parameters: Parameters?,
    ): Response {
        val requestEntity = request.bodyAsString
        val tema = JSONObject(requestEntity).getString("tema")

        val journalpostId = BREVKODE_MED_FORVENTET_JOURNALPOST_ID.entries.firstOrNull {
            val søknadstype = it.key
            requestEntity.contains(søknadstype.brevkode.brevKode) && (søknadstype.tema.kode == tema)
        }?.value ?: throw IllegalArgumentException("Ikke støttet brevkode.")

        return Response.Builder.like(response)
            .body(getResponse(journalpostId))
            .build()
    }

    override fun applyGlobally(): Boolean {
        return false
    }

    //language=JSON
    private fun getResponse(journalpostId: String) =
        """       
        {
          "journalpostId": "$journalpostId",
          "journalstatus": "M",
          "melding": null,
          "journalpostferdigstilt": false,
          "dokumenter": [
            {
              "dokumentInfoId": "485201432"
            },
            {
              "dokumentInfoId": "485201433"
            }
          ]
        }
    """.trimIndent()
}
