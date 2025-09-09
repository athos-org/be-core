package org.athos.core.service.secure;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import org.athos.core.TestUtils;
import org.athos.core.config.VaultConfiguration;
import org.athos.core.domain.exception.SecureStoreServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VaultSecureStoreServiceTest {

  @Mock
  private Vault vault;
  @Mock
  private VaultConfiguration config;
  @InjectMocks
  private VaultSecureStoreService vaultSecureStoreService;

  @BeforeEach
  void setUp() {
    when(vault.logical()).thenReturn(mock(Logical.class));
    when(config.getSecretRoot()).thenReturn("root");
    TestUtils.setInternalState(vaultSecureStoreService, "vault", vault);
  }

  @Test
  void getThrowsExceptionWhenVaultFails() throws VaultException {
    when(vault.logical().read("root/key")).thenThrow(new VaultException("Vault error"));
    assertThrows(SecureStoreServiceException.class, () -> vaultSecureStoreService.get("key"));
  }

  @Test
  void getReturnsValueWhenKeyExists() throws VaultException {
    var logicalResponse = mock(LogicalResponse.class);
    when(logicalResponse.getData()).thenReturn(Map.of("value", "secretValue"));
    when(vault.logical().read("root/key")).thenReturn(logicalResponse);
    String result = vaultSecureStoreService.get("key");
    assertEquals("secretValue", result);
  }

  @Test
  void setThrowsExceptionWhenVaultFails() throws VaultException {
    when(vault.logical().write("root/key", Map.of("value", "secretValue"))).thenThrow(new VaultException("Vault error"));
    assertThrows(SecureStoreServiceException.class, () -> vaultSecureStoreService.set("key", "secretValue"));
  }

  @Test
  void setStoresValueSuccessfully() throws VaultException {
    when(vault.logical().write("root/key", Map.of("value", "secretValue"))).thenReturn(mock(LogicalResponse.class));
    assertDoesNotThrow(() -> vaultSecureStoreService.set("key", "secretValue"));
  }
  
}
