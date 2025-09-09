package org.athos.core;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public class TestUtils {

  public static void setInternalState(Object target, String field, Object value) {
    try {
      var f = getDeclaredFieldRecursive(field, target.getClass());
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Unable to set internal state on a private field", e);
    }
  }

  private static Field getDeclaredFieldRecursive(String field, Class<?> cls) {
    try {
      return cls.getDeclaredField(field);
    } catch (NoSuchFieldException e) {
      if (cls.getSuperclass() != null) {
        return getDeclaredFieldRecursive(field, cls.getSuperclass());
      }
      throw new RuntimeException("Unable to find field: %s for class: %s".formatted(field, cls.getName()), e);
    }
  }

}
