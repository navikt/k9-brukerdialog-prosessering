package no.nav.brukerdialog.ytelse.ungdomsytelse.api.domene.soknad

import io.swagger.v3.oas.annotations.Hidden
import jakarta.validation.constraints.AssertTrue

data class KontonummerInfo(
    val harKontonummer: HarKontonummer,
    val kontonummerFraRegister: String? = null,
    val kontonummerErRiktig: Boolean? = null,
) {
    @get:Hidden
    @get:AssertTrue(message = "Dersom kontonummerFraRegister er satt, må kontonummerErRiktig være satt")
    val kontonummerRiktigSattNårKontonummerFinnes: Boolean
        get() = if (!kontonummerFraRegister.isNullOrBlank()) kontonummerErRiktig != null else true

    @get:Hidden
    @get:AssertTrue(message = "Dersom harKontonummer=JA må kontonummerFraRegister være satt")
    val kontonummerFraRegisterSattNårHarJa: Boolean
        get() = if (harKontonummer == HarKontonummer.JA) !kontonummerFraRegister.isNullOrBlank() else true

    @get:Hidden
    @get:AssertTrue(message = "Dersom harKontonummer=JA må kontonummerErRiktig være satt")
    val kontonummerErRiktigSattNårHarJa: Boolean
        get() = if (harKontonummer == HarKontonummer.JA) kontonummerErRiktig != null else true
}

enum class HarKontonummer {
    JA, NEI, UVISST
}
