package org.athos.core.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.athos.core.service.secure.SecureStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InternalApiService {

  private static final List<String> INTERNAL_SERVICE_NAMES = List.of("kong", "athos-bootstrap"); // TODO: Fetch from configuration
  private static final String INTERNAL_API_KEY = "internal-api-key";

  private final SecureStoreService vaultStoreService;
  private Cache<String, String> currentKeyCache;
  private Cache<String, String> previousKeyCache;
  private Cache<String, String> gatewayAddressCache;

  @Value("${athos.caches.api-key.current:3600}")
  private int currentKeyExpiration;
  @Value("${athos.caches.api-key.previous:900}")
  private int previousKeyExpiration;
  @Value("${athos.caches.gateway-address:3600}")
  private int gatewayAddressExpiration;

  @PostConstruct
  public void init() {
    currentKeyCache = Caffeine.newBuilder().maximumSize(1)
        .expireAfterWrite(currentKeyExpiration, TimeUnit.SECONDS)
        .evictionListener(this::keyExpirationCallback)
        .build();
    previousKeyCache = Caffeine.newBuilder().maximumSize(1)
        .expireAfterWrite(previousKeyExpiration, TimeUnit.SECONDS)
        .build();
    gatewayAddressCache = Caffeine.newBuilder().maximumSize(1)
        .expireAfterWrite(gatewayAddressExpiration, TimeUnit.SECONDS)
        .build();
  }

  private void keyExpirationCallback(String key, String value, RemovalCause cause) {
    previousKeyCache.put(key, value);
  }

  public String getInternalApiKey() {
    return currentKeyCache.get(INTERNAL_API_KEY, k -> vaultStoreService.get(INTERNAL_API_KEY));
  }

  public Set<String> getInternalApiKeys() {
    return StreamEx.of(getInternalApiKey())
        .append(previousKeyCache.getIfPresent(INTERNAL_API_KEY))
        .nonNull()
        .toSet();
  }

  public List<String> getInternalServiceAddresses() {
    return INTERNAL_SERVICE_NAMES.stream()
        .map(serviceName -> gatewayAddressCache.get(serviceName, this::getInternalServiceAddress))
        .toList();
  }

  public String getInternalServiceAddress(String serviceName) {
    try {
      return InetAddress.getByName(serviceName).getHostAddress();
    } catch (UnknownHostException e) {
      return null;
    }
  }

}
