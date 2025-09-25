package org.athos.core.domain.base;

import jakarta.annotation.Nullable;

public interface Identifiable<T> {

  @Nullable
  T getId();

  void setId(@Nullable T id);

}
