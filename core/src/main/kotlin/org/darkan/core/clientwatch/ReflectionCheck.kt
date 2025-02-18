package org.darkan.core.clientwatch

import org.darkan.core.net.prot.ClientProt
import org.darkan.core.net.prot.ServerProt

enum class ReflectionResponseCode(val id: Int) {
    SUCCESS(0),
    NUMBER(1),
    STRING(2),
    OTHER(4),

    CHECK_EXCEPTION_CLASS_NOT_FOUND(-1),
    CHECK_EXCEPTION_SECURITY(-2),
    CHECK_EXCEPTION_NULLPOINTER(-3),
    CHECK_EXCEPTION(-4),
    CHECK_THROWABLE(-5),

    RESP_EXCEPTION_CLASS_NOT_FOUND(-10),
    RESP_EXCEPTION_INVALID_CLASS(-11),
    RESP_EXCEPTION_STREAM_CORRUPTED(-12),
    RESP_EXCEPTION_OPTIONAL_DATA(-13),
    RESP_EXCEPTION_ILLEGAL_ACCESS(-14),
    RESP_EXCEPTION_ILLEGAL_ARGUMENT(-15),
    RESP_EXCEPTION_INVOCATION_TARGET(-16),
    RESP_EXCEPTION_SECURITY(-17),
    RESP_EXCEPTION_IO(-18),
    RESP_EXCEPTION_NULLPOINTER(-19),
    RESP_EXCEPTION(-20),
    RESP_THROWABLE(-21);

    companion object {
        private val map = entries.associateBy { it.id }
        fun fromId(id: Int) = map[id]
    }
}

enum class ReflectionCheckType {
    GET_INT,
    SET_INT,
    GET_FIELD_MODIFIERS,
    GET_METHOD_RETURN_VALUE,
    GET_METHOD_MODIFIERS
}

data class ReflectionData(
    val numericalData: Long? = null,
    val stringData: String? = null
)
