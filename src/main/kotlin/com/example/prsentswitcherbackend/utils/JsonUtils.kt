package com.example.prsentswitcherbackend.utils

import com.example.prsentswitcherbackend.model.outcome.OutcomeMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component

@Component
object JsonUtils {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    fun init(builder: Jackson2ObjectMapperBuilder) {
        objectMapper = builder.build()
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return objectMapper.readValue(json, clazz)
    }

    fun <T> convertData(fromValue: Any?, toValueType: Class<T>): T {
        return objectMapper.convertValue(fromValue, toValueType)
    }

    fun toJson(obj: OutcomeMessage<*>): String {
        return objectMapper.writeValueAsString(obj)
    }
}