package org.athos.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.athos.core.context.RequestContext;
import org.athos.core.context.RequestHeaders;
import org.athos.core.service.InternalApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AthosAuthenticationFilterTest {

  @Mock
  private RequestContext requestContext;
  @Mock
  private InternalApiService internalApiService;
  @InjectMocks
  private AthosAuthenticationFilter athosAuthenticationFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternalAuthenticatesRequestFromInternalServiceAddress() throws Exception {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);

    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(internalApiService.getInternalServiceAddresses()).thenReturn(List.of("127.0.0.1"));

    athosAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(requestContext).getUserPermissions();
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalAuthenticatesRequestWithValidApiKey() throws Exception {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);

    var headers = new HashMap<String, List<String>>();
    headers.put(RequestHeaders.X_INTERNAL_API_TOKEN.getValue(), List.of("validApiKey"));

    when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));
    when(request.getHeaders(RequestHeaders.X_INTERNAL_API_TOKEN.getValue()))
        .thenReturn(Collections.enumeration(headers.get(RequestHeaders.X_INTERNAL_API_TOKEN.getValue())));
    when(internalApiService.getInternalApiKeys()).thenReturn(Set.of("validApiKey"));

    athosAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalRejectsRequestWithInvalidApiKey() throws Exception {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);

    var headers = new HashMap<String, List<String>>();
    headers.put(RequestHeaders.X_INTERNAL_API_TOKEN.getValue(), List.of("invalidApiKey"));

    when(request.getHeaderNames()).thenReturn(Collections.enumeration(headers.keySet()));
    when(request.getHeaders(RequestHeaders.X_INTERNAL_API_TOKEN.getValue()))
        .thenReturn(Collections.enumeration(headers.get(RequestHeaders.X_INTERNAL_API_TOKEN.getValue())));
    when(internalApiService.getInternalApiKeys()).thenReturn(Set.of("validApiKey"));

    athosAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalRejectsRequestWithoutApiKeyOrInternalAddress() throws Exception {
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var filterChain = mock(FilterChain.class);

    when(request.getRemoteAddr()).thenReturn("192.168.0.1");
    when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when(internalApiService.getInternalServiceAddresses()).thenReturn(List.of("127.0.0.1"));
    when(internalApiService.getInternalApiKeys()).thenReturn(Set.of("validApiKey"));

    athosAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, never()).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

}
