package org.athos.core.scope;

import static org.athos.core.config.ExecutionScopeConfiguration.ATHOS_EXECUTION;
import static org.athos.core.scope.AthosExecutionScopeExecutionContextManager.getAthosExecutionScope;
import static org.athos.core.scope.AthosExecutionScopeExecutionContextManager.getConversationIdForScope;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;

public class AthosExecutionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    beanFactory.registerScope(ATHOS_EXECUTION, new AthosExecutionScopeConfigurer());
  }

  public static class AthosExecutionScopeConfigurer implements Scope {

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
      return getAthosExecutionScope().computeIfAbsent(name, k -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
      return getAthosExecutionScope().remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
      // No-op: Destruction callbacks are ignored in this scope
    }

    @Override
    public Object resolveContextualObject(String key) {
      return null;
    }

    @Override
    public String getConversationId() {
      return getConversationIdForScope();
    }

  }

}
