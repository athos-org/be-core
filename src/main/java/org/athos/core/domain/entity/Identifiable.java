package org.athos.core.domain.entity;

import jakarta.annotation.Nullable;

public interface Identifiable<T, R extends Identifiable<T, R>> {

  @Nullable
  T getId();

  R setId(@Nullable T id);

}
