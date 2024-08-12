package no.nav.k9brukerdialogprosessering.api.ytelse.fellesdomene

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.k9.søknad.felles.opptjening.SelvstendigNæringsdrivende
import no.nav.k9brukerdialogprosessering.utils.StringUtils
import java.math.BigDecimal
import java.time.LocalDate

data class VarigEndring(
    @JsonFormat(pattern = "yyyy-MM-dd")
    private val dato: LocalDate,
    private val inntektEtterEndring: Int,
    private val forklaring: String,
) {
    companion object {
        internal fun SelvstendigNæringsdrivende.SelvstendigNæringsdrivendePeriodeInfo.leggTilVarigEndring(varigEndring: VarigEndring) {
            medErVarigEndring(true)
            medEndringDato(varigEndring.dato)
            medEndringBegrunnelse(StringUtils.saniter(varigEndring.forklaring))
            medBruttoInntekt(BigDecimal.valueOf(varigEndring.inntektEtterEndring.toLong()))
        }
    }
}
