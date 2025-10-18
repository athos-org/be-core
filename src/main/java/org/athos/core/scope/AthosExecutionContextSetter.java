package org.athos.core.scope;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import one.util.streamex.StreamEx;
import org.athos.core.scope.context.AthosExecutionContext;
import org.athos.core.scope.context.DefaultAthosExecutionContext;

/**
 * Context setter that stores the {@link AthosExecutionContext} in a {@link ThreadLocal} variable.
 *
 * <p>Call {@link #close()} after use to remove it from the {@link ThreadLocal} variable so
 * that any later usage of the thread cannot accidentally use a wrong {@link AthosExecutionContext}
 * and unit tests fail if storing {@link AthosExecutionContext} is forgotten.
 *
 * <p>Best practice is try-with-resources that automatically calls {@link #close()} in all cases, even
 * on thrown exception:
 *
 * <pre>
 * try (var x = new AthosExecutionContextSetter(athosModuleMetadata, httpHeaders)) {
 *   // some stuff
 * }
 * </pre>
 */
public class AthosExecutionContextSetter implements AutoCloseable {

  /**
   * Stores the {@link AthosExecutionContext} in a {@link ThreadLocal} variable.
   */
  public AthosExecutionContextSetter(AthosExecutionContext athosExecutionContext) {
    AthosExecutionScopeExecutionContextManager.beginAthosExecutionContext(athosExecutionContext);
  }

  /**
   * Create a {@link AthosExecutionContext} from the {@code httpHeaders} and store it in a {@link ThreadLocal} variable.
   *
   * @param httpHeaders where to take the tenant id from
   */
  public AthosExecutionContextSetter(Map<String, Collection<String>> httpHeaders) {
    this(new DefaultAthosExecutionContext(httpHeaders));
  }

  /**
   * Create a {@link AthosExecutionContext} from the {@code httpServletRequest}, and store it in a {@link ThreadLocal} variable.
   *
   * @param httpServletRequest where to take the tenant id from
   */
  public AthosExecutionContextSetter(HttpServletRequest httpServletRequest) {
    this(getRequestHeaders(httpServletRequest));
  }

  /**
   * Remove {@link AthosExecutionContext} from the {@link ThreadLocal} variable.
   */
  @Override
  public void close() {
    AthosExecutionScopeExecutionContextManager.endAthosExecutionContext();
  }

  private static Map<String, Collection<String>> getRequestHeaders(HttpServletRequest httpServletRequest) {
    return StreamEx.of(httpServletRequest.getHeaderNames())
        .toMap(Function.identity(), header -> StreamEx.of(httpServletRequest.getHeaders(header)).toList());
  }

}
