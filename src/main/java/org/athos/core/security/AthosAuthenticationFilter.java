package org.athos.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;
import org.athos.core.context.RequestContext;
import org.athos.core.context.RequestHeaders;
import org.athos.core.service.InternalApiService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Log4j2
@RequiredArgsConstructor
public class AthosAuthenticationFilter extends OncePerRequestFilter {

  private final InternalApiService internalApiService;
  private final RequestContext requestContext;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    var headers = StreamEx.of(request.getHeaderNames())
        .toMap(Function.identity(), header -> StreamEx.of(request.getHeaders(header)).toList());

    requestContext.populateContext(headers);
    log.info("Incoming request: [{}] {}", request.getMethod(), request.getRequestURI());

    boolean isAuthenticated = authenticateRequest(request, headers);
    if (isAuthenticated) {
      var grants = requestContext.getUserPermissions().stream().map(SimpleGrantedAuthority::new).toList();
      var authentication = new UsernamePasswordAuthenticationToken(requestContext.getUserEmail(), null, grants);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      log.info("Request authenticated successfully: [{}] {}", request.getMethod(), request.getRequestURI());
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private boolean authenticateRequest(HttpServletRequest request, Map<String, List<String>> headers) {
    if (internalApiService.getInternalServiceAddresses().contains(request.getRemoteAddr())) {
      return true;
    }
    var internalApiKey = headers
        .getOrDefault(RequestHeaders.X_INTERNAL_API_TOKEN.getValue(), Collections.singletonList(null))
        .getFirst();
    return Optional.ofNullable(internalApiKey)
        .map(internalApiService.getInternalApiKeys()::contains)
        .orElse(false);
  }

}
