package org.athos.core.domain.mapper;

import org.mapstruct.Mapping;

@Mapping(target = "createdBy", ignore = true)
@Mapping(target = "createdDate", ignore = true)
@Mapping(target = "updatedBy", ignore = true)
@Mapping(target = "updatedDate", ignore = true)
public @interface EntityMetadataMapping {

}
