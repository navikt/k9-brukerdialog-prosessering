package no.nav.brukerdialog.config

import no.nav.brukerdialog.common.Constants.OSLO_ZONE_ID
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class ClockConfiguration {

    /** Norsk tid, slik at «i dag» blir riktig rundt midnatt. Kan erstattes med en fast klokke i tester. */
    @Bean
    fun clock(): Clock = Clock.system(OSLO_ZONE_ID)
}
