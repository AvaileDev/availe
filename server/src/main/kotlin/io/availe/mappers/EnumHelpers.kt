package io.availe.mappers

import org.mapstruct.Named
import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.availe.jooq.enums.AllowedFormat as DbAllowedFormat
import io.availe.jooq.enums.MessageType as DbMessageType

/**
 * Helper object for converting between DTO and DB enum types.
 * Used by MapStruct mappers for type conversions.
 */
object EnumHelpers {

    /**
     * Converts a DTO AllowedFormat to a DB AllowedFormat.
     *
     * @param dto The DTO AllowedFormat to convert
     * @return The corresponding DB AllowedFormat
     */
    @JvmStatic
    @Named("dtoToDbAllowedFormat")
    fun dtoToDbAllowedFormat(dto: AllowedFormat): DbAllowedFormat =
        DbAllowedFormat.valueOf(dto.name)

    /**
     * Converts a DB AllowedFormat to a DTO AllowedFormat.
     *
     * @param db The DB AllowedFormat to convert
     * @return The corresponding DTO AllowedFormat
     */
    @JvmStatic
    @Named("dbToDtoAllowedFormat")
    fun dbToDtoAllowedFormat(db: DbAllowedFormat): AllowedFormat =
        AllowedFormat.valueOf(db.name)

    /**
     * Converts a DTO MessageType to a DB MessageType.
     * Handles null values safely.
     *
     * @param dto The DTO MessageType to convert (can be null)
     * @return The corresponding DB MessageType or null if input is null
     */
    @JvmStatic
    @Named("dtoToDbMessageType")
    fun dtoToDbMessageType(dto: NLIPRequest.Messagetype?): DbMessageType? =
        dto?.let { DbMessageType.valueOf(it.name) }

    /**
     * Converts a DB MessageType to a DTO MessageType.
     * Handles null values safely.
     *
     * @param db The DB MessageType to convert (can be null)
     * @return The corresponding DTO MessageType or null if input is null
     */
    @JvmStatic
    @Named("dbToDtoMessageType")
    fun dbToDtoMessageType(db: DbMessageType?): NLIPRequest.Messagetype? =
        db?.let { NLIPRequest.Messagetype.valueOf(it.name) }
}
