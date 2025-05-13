package io.availe.mappers

import io.availe.openapi.model.NLIPSubMessage
import io.availe.jooq.tables.records.NlipSubmessageRecord
import org.mapstruct.*

/**
 * Mapper for converting between NLIPSubMessage DTOs and NlipSubmessageRecord entities.
 */
@Mapper(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    unmappedSourcePolicy = ReportingPolicy.ERROR,
    uses = [EnumHelpers::class, JsonHelpers::class],
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
interface NlipSubMessageMapper {

    /**
     * Converts a NLIPSubMessage DTO to a NlipSubmessageRecord entity.
     *
     * @param dto The NLIPSubMessage DTO to convert
     * @return The corresponding NlipSubmessageRecord entity
     */
    @Mappings(
        Mapping(target = "format", source = "format", qualifiedByName = ["dtoToDbAllowedFormat"]),
        Mapping(target = "content", source = "content", qualifiedByName = ["stringToJsonb"]),
        Mapping(target = "id", ignore = true),
        Mapping(target = "requestId", ignore = true)
    )
    fun toEntity(dto: NLIPSubMessage): NlipSubmessageRecord

    /**
     * Converts a NlipSubmessageRecord entity to a NLIPSubMessage DTO.
     *
     * @param rec The NlipSubmessageRecord entity to convert
     * @return The corresponding NLIPSubMessage DTO
     */
    @BeanMapping(
        ignoreByDefault = true,
        ignoreUnmappedSourceProperties = ["qualifier", "SQLTypeName", "table", "id", "requestId"]
    )
    @Mappings(
        Mapping(target = "format", source = "format", qualifiedByName = ["dbToDtoAllowedFormat"]),
        Mapping(target = "content", source = "content", qualifiedByName = ["jsonbToString"]),
        Mapping(target = "subformat", source = "subformat"),
        Mapping(target = "label", source = "label")
    )
    fun fromEntity(rec: NlipSubmessageRecord): NLIPSubMessage
}
