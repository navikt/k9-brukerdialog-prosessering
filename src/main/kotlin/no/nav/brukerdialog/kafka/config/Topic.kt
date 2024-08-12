package no.nav.brukerdialog.kafka.config

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced

data class Topic<V>(
    val name: String,
    val serDes: SerDes<V>
) {
    private val keySerde = Serdes.String()
    private val valueSerde = Serdes.serdeFrom(serDes, serDes)
    val consumedWith = Consumed.with(keySerde, valueSerde)
    val producedWith = Produced.with(keySerde, valueSerde)
}
