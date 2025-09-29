package org.athos.core.domain.mapper;

import org.mapstruct.Mapping;

@Mapping(target = "metadata.createdBy", source = "createdBy")
@Mapping(target = "metadata.createdDate", source = "createdDate")
@Mapping(target = "metadata.updatedBy", source = "updatedBy")
@Mapping(target = "metadata.updatedDate", source = "updatedDate")
@Mapping(target = "metadata.version", source = "version")
public @interface DtoMetadataMapping {

}
