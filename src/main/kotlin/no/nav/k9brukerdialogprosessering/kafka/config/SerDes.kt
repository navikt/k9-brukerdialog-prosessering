package no.nav.k9brukerdialogprosessering.kafka.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

abstract class SerDes<V>(private val objectMapper: ObjectMapper) : Serializer<V>, Deserializer<V> {
    override fun serialize(topic: String, data: V): ByteArray {
        return data.let {
            objectMapper.writeValueAsBytes(it)
        }
    }

    override fun configure(configs: MutableMap<String, *>, isKey: Boolean) {}
    override fun close() {}
}
