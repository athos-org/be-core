package org.athos.core.scope.context;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public interface AthosExecutionContext {

  default String getRequestId() {
    return null;
  }

  default Map<String, Collection<String>> getHeaders() {
    return Collections.emptyMap();
  }

  default String getUserId() {
    return null;
  }

  default String getUserEmail() {
    return null;
  }

  default Collection<String> getUserPermissions() {
    return Collections.emptyList();
  }

  /**
   * A useful method to get an actual instance of the FolioExecutionContext when the one is injected through
   * a wrapper/proxy. Pay attention, that the result type must be Object, otherwise a proxy will return itself
   */
  default Object getInstance() {
    return this;
  }

}
