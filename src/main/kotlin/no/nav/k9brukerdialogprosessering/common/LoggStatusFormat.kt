package no.nav.k9brukerdialogprosessering.common

import no.nav.k9brukerdialogprosessering.api.ytelse.Ytelse

//Brukes når man logger status i flyten. Formaterer slik at loggen er mer lesbar
internal fun formaterStatuslogging(ytelse: Ytelse, id: String, melding: String): String {
    return String.format("Søknad for %s med søknadID: %2$36s %3$1s",ytelse, id, melding)
}
