package no.nav.k9brukerdialogprosessering.common

import com.fasterxml.jackson.core.type.TypeReference

inline fun <reified T> TypeReferanseHelper(): TypeReference<TopicEntry<T>> = object : TypeReference<TopicEntry<T>>() {}
data class TopicEntry<V>(val metadata: Metadata, val data: V)
data class Metadata(val version: Int, val correlationId: String)
