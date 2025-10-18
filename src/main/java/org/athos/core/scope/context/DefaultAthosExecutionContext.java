package org.athos.core.scope.context;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Log4j2
@NoArgsConstructor
@Data
public class DefaultAthosExecutionContext implements AthosExecutionContext {

  private String userId;
  private String userEmail;
  private Collection<String> userPermissions;

  private String requestId;
  private Map<String, Collection<String>> headers;

  public DefaultAthosExecutionContext(Map<String, Collection<String>> requestHeaders) {
    log.debug("populateContext:: Creating execution context with headers: {}", headers);
    this.headers = StreamEx.of(requestHeaders.entrySet())
        .filter(entry -> AthosRequestHeaders.isAthosRequestHeader(entry.getKey()))
        .toMap(Map.Entry::getKey, Map.Entry::getValue);
    this.requestId = getHeaderValue(headers, AthosRequestHeaders.X_KONG_REQUEST_ID);
    this.userId = getHeaderValue(headers, AthosRequestHeaders.X_USER_ID);
    this.userEmail = getHeaderValue(headers, AthosRequestHeaders.X_USER_EMAIL);
    this.userPermissions = getHeaderValues(headers, AthosRequestHeaders.X_USER_PERMISSIONS);
  }

  private static String getHeaderValue(Map<String, Collection<String>> headers, AthosRequestHeaders header) {
    return headers.getOrDefault(header.getValue(), Collections.singletonList(null)).iterator().next();
  }

  private static Collection<String> getHeaderValues(Map<String, Collection<String>> headers, AthosRequestHeaders header) {
    return headers.getOrDefault(header.getValue(), Collections.emptyList());
  }

}
