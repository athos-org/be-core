package org.athos.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StoreUtilsTest {

  @Test
  void validateKeyThrowsExceptionForBlankKey() {
    assertThrows(IllegalArgumentException.class, () -> StoreUtils.validateKey(" "));
  }

  @Test
  void validateKeyDoesNotThrowForNonBlankKey() {
    assertDoesNotThrow(() -> StoreUtils.validateKey("validKey"));
  }

  @Test
  void validateValueThrowsExceptionForBlankValue() {
    assertThrows(IllegalArgumentException.class, () -> StoreUtils.validateValue(" "));
  }

  @Test
  void validateValueDoesNotThrowForNonBlankValue() {
    assertDoesNotThrow(() -> StoreUtils.validateValue("validValue"));
  }


}