package no.nav.brukerdialog.ytelse.omsorgpengerutbetalingat.api.domene

import jakarta.validation.Valid

class DineBarn(
    var barn: List<@Valid Barn>,
    val harDeltBosted: Boolean,
)
