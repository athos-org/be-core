package org.athos.core.domain.mapper;

import org.athos.core.domain.entity.IdentifiableEntity;

public interface BaseEntityMapper<T extends IdentifiableEntity<T>, D> {

  T toEntity(D dto);

  void updateEntity(D dto, T entity);

  D toDto(T entity);

}
