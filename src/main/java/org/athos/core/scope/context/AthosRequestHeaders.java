package org.athos.core.scope.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum AthosRequestHeaders {

  X_USER_ID("x-user-id"),
  X_USER_EMAIL("x-user-email"),
  X_USER_PERMISSIONS("x-user-permissions"),
  X_KONG_REQUEST_ID("x-kong-request-id"),
  X_INTERNAL_API_TOKEN("x-athos-api-key");

  private final String value;

  public static boolean isAthosRequestHeader(String header) {
    return Arrays.stream(values()).anyMatch(requestHeader -> requestHeader.getValue().equalsIgnoreCase(header));
  }

}
