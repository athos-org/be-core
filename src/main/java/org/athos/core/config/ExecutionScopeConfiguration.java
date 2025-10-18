package org.athos.core.config;

import org.athos.core.scope.AthosExecutionBeanFactoryPostProcessor;
import org.athos.core.scope.context.AthosExecutionContext;
import org.athos.core.scope.context.DefaultAthosExecutionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.Optional;

import static org.athos.core.scope.AthosExecutionScopeExecutionContextManager.getAthosExecutionContext;

@Configuration
public class ExecutionScopeConfiguration {

  public static final String ATHOS_EXECUTION = "athosExecution";

  @Bean
  public static AthosExecutionBeanFactoryPostProcessor customScopeRegistryBeanFactoryPostProcessor() {
    return new AthosExecutionBeanFactoryPostProcessor();
  }

  @Bean
  @Scope(value = ATHOS_EXECUTION, proxyMode = ScopedProxyMode.INTERFACES)
  public AthosExecutionContext athosExecutionContext() {
    return Optional.ofNullable(getAthosExecutionContext()).orElse(new DefaultAthosExecutionContext());
  }

}
