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
    var isAuthenticated = false;
    var headers = StreamEx.of(request.getHeaderNames())
        .toMap(Function.identity(), header -> StreamEx.of(request.getHeaders(header)).toList());

    requestContext.populateContext(headers);
    log.info("Incoming request: [{}] {}", request.getMethod(), request.getRequestURI());

    if (internalApiService.getInternalServiceAddresses().contains(request.getRemoteAddr())) {
      isAuthenticated = true;
    } else {
      var internalApiKey = headers.get(RequestHeaders.X_INTERNAL_API_TOKEN.getValue()).getFirst();
      isAuthenticated = internalApiService.getInternalApiKeys().contains(internalApiKey);
    }

    if (isAuthenticated) {
      var grants = requestContext.getUserPermissions().stream().map(SimpleGrantedAuthority::new).toList();
      var authentication = new UsernamePasswordAuthenticationToken(requestContext.getUserEmail(), null, grants);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
      log.info("Completed request: [{}] {}, result: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
    }
  }

}
