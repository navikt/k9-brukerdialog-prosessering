package no.nav.brukerdialog.ytelse.aktivitetspenger.api.domene.oppgavebekreftelse

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.k9.oppgave.bekreftelse.Bekreftelse

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = KomplettKontrollerRegisterinntektOppgaveDTO::class, name = Bekreftelse.UNG_AVVIK_REGISTERINNTEKT),
    JsonSubTypes.Type(value = KomplettBekreftBostedOppgaveDTO::class, name = Bekreftelse.AVP_BOSTED_AVKLARING),
    JsonSubTypes.Type(value = KomplettBekreftBistandOppgaveDTO::class, name = Bekreftelse.AVP_BISTANDSBEHOV_AVKLARING),
    JsonSubTypes.Type(value = KomplettBekreftAndreLivsoppholdsytelserOppgaveDTO::class, name = Bekreftelse.AVP_ANDRE_LIVSOPPHOLDSYTELSER_AVKLARING),
    JsonSubTypes.Type(value = KomplettBekreftAktivitetOppgaveDTO::class, name = Bekreftelse.AVP_AKTIVITET_AVKLARING)
)
sealed class KomplettAktivitetspengerOppgaveDTO(
    open val oppgaveReferanse: String,
    open val uttalelse: AktivitetspengerOppgaveUttalelseDTO,
) {
    abstract fun somK9Format(): Bekreftelse
    abstract fun dokumentTittelSuffix(): String
}