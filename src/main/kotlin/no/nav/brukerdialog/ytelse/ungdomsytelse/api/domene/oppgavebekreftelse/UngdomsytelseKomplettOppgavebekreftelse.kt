package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.oppgavebekreftelse

import no.nav.brukerdialog.domenetjenester.innsending.KomplettInnsending
import no.nav.brukerdialog.oppslag.soker.Søker
import no.nav.k9.søknad.Søknad
import java.time.ZonedDateTime
import java.util.UUID

data class UngdomsytelseKomplettOppgavebekreftelse(
    private val deltakelseId: UUID,
    private val oppgaveId: UUID,
    private val søknadId: UUID,
    private val søker: Søker,
    private val mottatt: ZonedDateTime,
    private val k9Format: Søknad,
) : KomplettInnsending
