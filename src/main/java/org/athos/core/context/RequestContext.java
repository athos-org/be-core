package org.athos.core.context;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class RequestContext {

  private String userId;
  private String userEmail;
  private List<String> userPermissions;

  private String requestId;
  private Map<String, List<String>> headers;

  public void populateContext(Map<String, List<String>> headers) {
    log.debug("populateContext:: Populating request context with headers: {}", headers);
    this.headers = headers;
    this.requestId = getHeaderValue(headers, RequestHeaders.X_KONG_REQUEST_ID);
    this.userId = getHeaderValue(headers, RequestHeaders.X_USER_ID);
    this.userEmail = getHeaderValue(headers, RequestHeaders.X_USER_EMAIL);
    this.userPermissions = getHeaderValues(headers, RequestHeaders.X_USER_PERMISSIONS);
  }

  private static String getHeaderValue(Map<String, List<String>> headers, RequestHeaders header) {
    return headers.getOrDefault(header.getValue(), Collections.singletonList(null)).getFirst();
  }

  private static List<String> getHeaderValues(Map<String, List<String>> headers, RequestHeaders header) {
    return headers.getOrDefault(header.getValue(), Collections.emptyList());
  }

}
