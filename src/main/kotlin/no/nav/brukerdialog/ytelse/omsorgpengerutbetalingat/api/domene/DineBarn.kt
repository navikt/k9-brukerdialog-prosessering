package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene

import jakarta.validation.Valid

class DineBarn(
    @field:Valid var barn: List<Barn>,
    val harDeltBosted: Boolean,
)
