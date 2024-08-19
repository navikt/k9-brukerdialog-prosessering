package no.nav.brukerdialog.utils

import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class IgnoredPathUtilsTest {
    @ParameterizedTest
    @ValueSource(strings = ["liveness", "readiness", "metrics"])
    fun name(path: String) {
        Assertions.assertThat(IgnoredPathUtils.isIgnoredPath(path)).isTrue
    }
}
