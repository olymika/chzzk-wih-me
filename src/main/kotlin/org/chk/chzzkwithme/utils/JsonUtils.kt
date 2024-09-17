package org.chk.chzzkwithme.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jsonMapper

internal object JsonUtils {
    val OBJ_MAPPER =
        jsonMapper {
            addModules(KotlinModule.Builder().build())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
}

internal fun <T> toJson(obj: T): String = JsonUtils.OBJ_MAPPER.writeValueAsString(obj)

internal inline fun <reified T> fromJson(json: String): T = JsonUtils.OBJ_MAPPER.readValue(json, T::class.java)

internal fun readTree(json: String) = JsonUtils.OBJ_MAPPER.readTree(json)
