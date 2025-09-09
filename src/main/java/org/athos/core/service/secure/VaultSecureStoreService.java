package org.athos.core.service.secure;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.athos.core.config.VaultConfiguration;
import org.athos.core.domain.exception.SecureStoreServiceException;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.athos.core.utils.StoreUtils.validateKey;
import static org.athos.core.utils.StoreUtils.validateValue;

/**
 * Service for storing and retrieving secrets from HashiCorp Vault.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class VaultSecureStoreService implements SecureStoreService {

  private static final String VALUE_KEY = "value";

  private final VaultConfiguration vaultConfiguration;
  private Vault vault;

  @PostConstruct
  public void init() throws VaultException {
    var appRoleToken = getVault(null).auth()
        .loginByAppRole(vaultConfiguration.getRoleId(), vaultConfiguration.getSecretId())
        .getAuthClientToken();
    vault = getVault(appRoleToken);
  }

  public String get(String key) {
    try {
      return vault.logical().read(addRootPath(key)).getData().get(VALUE_KEY);
    } catch (VaultException e) {
      throw new SecureStoreServiceException("Failed to get secret: key = " + key + ", error = " + e.getMessage(), e);
    }
  }

  public void set(String key, String value) {
    validateKey(key);
    validateValue(value);
    try {
      vault.logical().write(addRootPath(key), Map.of(VALUE_KEY, value));
    } catch (VaultException e) {
      throw new SecureStoreServiceException("Failed to save secret: key = " + key + ", error = " + e.getMessage(), e);
    }
  }

  private String addRootPath(String path) {
    return StringUtils.isNotEmpty(vaultConfiguration.getSecretRoot())
        ? vaultConfiguration.getSecretRoot() + "/" + path
        : path;
  }

  private Vault getVault(String token) throws VaultException {
    return new Vault(new VaultConfig().address(vaultConfiguration.getUrl()).token(token).build());
  }

}
