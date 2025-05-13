package io.availe.mappers

import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import io.availe.jooq.tables.records.NlipRequestRecord
import io.availe.jooq.tables.records.NlipSubmessageRecord
import org.mapstruct.*

/**
 * Mapper for converting between NLIPRequest DTOs and NlipRequestRecord entities.
 * Handles the parent-child relationship between requests and submessages.
 */
@Mapper(
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    unmappedSourcePolicy = ReportingPolicy.ERROR,
    uses = [EnumHelpers::class, JsonHelpers::class, NlipSubMessageMapper::class],
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
interface NlipRequestMapper {

    /**
     * Converts a NLIPRequest DTO to a NlipRequestRecord entity.
     * Ignores submessages as they are handled separately.
     *
     * @param dto The NLIPRequest DTO to convert
     * @return The corresponding NlipRequestRecord entity
     */
    @BeanMapping(ignoreUnmappedSourceProperties = ["submessages"])
    @Mappings(
        Mapping(target = "messagetype", source = "messagetype", qualifiedByName = ["dtoToDbMessageType"]),
        Mapping(target = "format", source = "format", qualifiedByName = ["dtoToDbAllowedFormat"]),
        Mapping(target = "content", source = "content", qualifiedByName = ["stringToJsonb"]),
        Mapping(target = "id", ignore = true),
        Mapping(target = "createdAt", ignore = true),
        Mapping(target = "conversationId", ignore = true),
        Mapping(target = "ordinal", ignore = true)
    )
    fun toEntity(dto: NLIPRequest): NlipRequestRecord

    /**
     * Converts a list of NLIPSubMessage DTOs to a list of NlipSubmessageRecord entities.
     *
     * @param list The list of NLIPSubMessage DTOs to convert
     * @return The corresponding list of NlipSubmessageRecord entities
     */
    fun toSubEntities(list: List<NLIPSubMessage>): List<NlipSubmessageRecord>

    /**
     * Converts a NlipRequestRecord entity and its child NlipSubmessageRecord entities to a NLIPRequest DTO.
     *
     * @param parent The parent NlipRequestRecord entity
     * @param children The list of child NlipSubmessageRecord entities
     * @return The corresponding NLIPRequest DTO with submessages populated
     */
    @BeanMapping(
        ignoreByDefault = true,
        ignoreUnmappedSourceProperties = ["qualifier", "SQLTypeName", "table", "id", "createdAt", "conversationId", "ordinal"]
    )
    @Mappings(
        Mapping(target = "messagetype", source = "parent.messagetype", qualifiedByName = ["dbToDtoMessageType"]),
        Mapping(target = "format", source = "parent.format", qualifiedByName = ["dbToDtoAllowedFormat"]),
        Mapping(target = "subformat", source = "parent.subformat"),
        Mapping(target = "content", source = "parent.content", qualifiedByName = ["jsonbToString"]),
        Mapping(target = "submessages", source = "children")
    )
    fun fromEntity(parent: NlipRequestRecord, children: List<NlipSubmessageRecord>): NLIPRequest
}
