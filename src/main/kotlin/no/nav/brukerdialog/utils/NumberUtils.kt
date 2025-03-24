package no.nav.brukerdialog.utils

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

object NumberUtils {
    fun BigDecimal.formaterSomValuta(): String {
        val valutaFormat = NumberFormat.getCurrencyInstance(Locale.of("no", "NO"))
        return valutaFormat.format(this)
    }

    fun Int.formaterSomValuta(): String = BigDecimal.valueOf(this.toLong()).formaterSomValuta()
}
