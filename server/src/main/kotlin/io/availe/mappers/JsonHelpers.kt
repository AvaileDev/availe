package io.availe.mappers

import org.jooq.JSONB
import org.mapstruct.Named

/**
 * Helper object for converting between String and JSONB types.
 * Used by MapStruct mappers for JSON data conversions.
 */
object JsonHelpers {

    /**
     * Converts a String to a JSONB object.
     * For simple strings, stores the string directly without JSON serialization.
     *
     * @param str The String to convert to JSONB
     * @return The corresponding JSONB object
     */
    @JvmStatic
    @Named("stringToJsonb")
    fun stringToJsonb(str: String): JSONB = JSONB.valueOf(str)

    /**
     * Converts a JSONB object to a String.
     * For simple strings, returns the raw data without JSON deserialization.
     *
     * @param js The JSONB object to convert
     * @return The corresponding String representation
     */
    @JvmStatic
    @Named("jsonbToString")
    fun jsonbToString(js: JSONB): String = js.data()
}
