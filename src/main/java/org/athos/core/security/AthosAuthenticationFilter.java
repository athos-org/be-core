package org.athos.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.athos.core.scope.AthosExecutionContextSetter;
import org.athos.core.scope.AthosExecutionScopeExecutionContextManager;
import org.athos.core.scope.context.AthosRequestHeaders;
import org.athos.core.service.InternalApiService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class AthosAuthenticationFilter extends OncePerRequestFilter {

  private final InternalApiService internalApiService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try (var ignored = new AthosExecutionContextSetter(request)) {
      var athosExecutionContext = AthosExecutionScopeExecutionContextManager.getAthosExecutionContext();
      log.info("Incoming request: [{}] {}", request.getMethod(), request.getRequestURI());

      boolean isAuthenticated = authenticateRequest(request, athosExecutionContext.getHeaders());
      if (isAuthenticated) {
        var grants = athosExecutionContext.getUserPermissions().stream().map(SimpleGrantedAuthority::new).toList();
        var authentication = new UsernamePasswordAuthenticationToken(athosExecutionContext.getUserEmail(), null, grants);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
        log.info("Request completed with status {}: [{}] {}", response.getStatus(), request.getMethod(), request.getRequestURI());
      } else {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
  }

  private boolean authenticateRequest(HttpServletRequest request, Map<String, Collection<String>> headers) {
    if (internalApiService.getInternalServiceAddresses().contains(request.getRemoteAddr())) {
      return true;
    }
    var internalApiKey = headers
        .getOrDefault(AthosRequestHeaders.X_INTERNAL_API_TOKEN.getValue(), Collections.singletonList(null))
        .iterator().next();
    return Optional.ofNullable(internalApiKey)
        .map(internalApiService.getInternalApiKeys()::contains)
        .orElse(false);
  }

}
