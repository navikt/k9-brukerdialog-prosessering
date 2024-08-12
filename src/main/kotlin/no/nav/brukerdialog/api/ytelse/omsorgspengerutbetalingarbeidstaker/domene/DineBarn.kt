package no.nav.brukerdialog.api.ytelse.omsorgspengerutbetalingarbeidstaker.domene

import jakarta.validation.Valid

class DineBarn(
    @field:Valid var barn: List<Barn>,
    val harDeltBosted: Boolean,
)
