package org.athos.core.scope;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.athos.core.scope.context.AthosExecutionContext;
import org.springframework.core.NamedInheritableThreadLocal;

/**
 * AthosExecutionScopeExecutionContextManager is used to store {@link AthosExecutionContext} in thread local.
 * If current thread that uses AthosExecutionContext creates a new thread, the context is not automatically propagated.
 */
@UtilityClass
@Log4j2
public class AthosExecutionScopeExecutionContextManager {

  private static final String CONVERSATION_ID_KEY = "conversationId";
  private static final String CONVERSATION_ID_GLOBAL = "00000000-0000-0000-0000-000000000000";

  private static final Map<String, Object> fallBackAthosExecutionScope = new ConcurrentHashMap<>();
  private static final AthosExecutionThreadLocal<AthosExecutionContext> athosExecutionContextHolder =
      new AthosExecutionThreadLocal<>("AthosExecutionContext", ctx -> (AthosExecutionContext) ctx.getInstance());
  private static final AthosExecutionThreadLocal<Map<String, Object>> athosExecutionScopeHolder =
      new AthosExecutionThreadLocal<>("AthosExecutionScope", ConcurrentHashMap::new);

  /**
   * Store athosExecutionContext as {@link ThreadLocal} variable.
   *
   * <p>The visibility of this method is package-private to enforce using {@link AthosExecutionContextSetter}.
   */
  static void beginAthosExecutionContext(AthosExecutionContext athosExecutionContext) {
    athosExecutionScopeHolder.get().push(new ConcurrentHashMap<>(Map.of(CONVERSATION_ID_KEY, UUID.randomUUID().toString())));
    athosExecutionContextHolder.get().push(athosExecutionContext);

    log.debug("AthosExecutionContext created: {};\nCurrent thread: {}", athosExecutionContext, Thread.currentThread().getName());
  }

  /**
   * Remove AthosExecutionContext from the {@link ThreadLocal} variable.
   *
   * <p>The visibility of this method is package-private to enforce using {@link AthosExecutionContextSetter}.
   */
  static void endAthosExecutionContext() {
    athosExecutionContextHolder.get().pop();
    athosExecutionScopeHolder.get().pop();
    log.debug("AthosExecutionContext removed;\nCurrent thread: {}", Thread.currentThread().getName());
  }

  public static AthosExecutionContext getAthosExecutionContext() {
    return athosExecutionContextHolder.get().peek();
  }

  public static String getConversationIdForScope() {
    return (String) getAthosExecutionScope().getOrDefault(CONVERSATION_ID_KEY, CONVERSATION_ID_GLOBAL);
  }

  public static Map<String, Object> getAthosExecutionScope() {
    var athosExecutionScope = athosExecutionScopeHolder.get().peek();
    return athosExecutionScope == null ? fallBackAthosExecutionScope : athosExecutionScope;
  }

  public static class AthosExecutionThreadLocal<T> extends NamedInheritableThreadLocal<Deque<T>> {

    private final Function<T, T> converter;

    public AthosExecutionThreadLocal(String name, Function<T, T> converter) {
      super(name);
      this.converter = converter;
    }

    @Override
    protected Deque<T> initialValue() {
      return new ArrayDeque<>();
    }

    @Override
    protected Deque<T> childValue(Deque<T> parentValue) {
      var result = initialValue();
      if (parentValue != null && !parentValue.isEmpty()) {
        result.push(converter.apply(parentValue.peek()));
      }
      return result;
    }
  }


}
