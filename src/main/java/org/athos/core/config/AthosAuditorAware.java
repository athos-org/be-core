package org.athos.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.athos.core.context.RequestContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
@Log4j2
public class AthosAuditorAware implements AuditorAware<UUID> {

  private final RequestContext requestContext;

  @Override
  public Optional<UUID> getCurrentAuditor() {
    try {
      return Optional.of(UUID.fromString(requestContext.getUserId()));
    } catch (Exception e) {
      log.warn("getCurrentAuditor:: Invalid userId format: {} for request id: {}", requestContext.getUserId(), requestContext.getRequestId());
      return Optional.empty();
    }
  }

}
