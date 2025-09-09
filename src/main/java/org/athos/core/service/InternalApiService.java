package org.athos.core.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.athos.core.service.secure.VaultSecureStoreService;
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

  private final VaultSecureStoreService vaultStoreService;
  private Cache<String, String> currentKeyCache;
  private Cache<String, String> previousKeyCache;
  private Cache<String, String> gatewayAddressCache;

  @PostConstruct
  public void init() {
    currentKeyCache = Caffeine.newBuilder().maximumSize(1)
        .expireAfterWrite(60, TimeUnit.MINUTES)
        .evictionListener(this::keyExpirationCallback)
        .build();
    previousKeyCache = Caffeine.newBuilder().maximumSize(1)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .build();
    gatewayAddressCache = Caffeine.newBuilder().maximumSize(1)
        .expireAfterWrite(60, TimeUnit.MINUTES)
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
