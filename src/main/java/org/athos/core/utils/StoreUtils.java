package org.athos.core.utils;

import io.micrometer.common.util.StringUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StoreUtils {

  public static void validateKey(String key) {
    if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("Key cannot be blank");
    }
  }

  public static void validateValue(String value) {
    if (StringUtils.isBlank(value)) {
      throw new IllegalArgumentException("Value cannot be blank");
    }
  }

}