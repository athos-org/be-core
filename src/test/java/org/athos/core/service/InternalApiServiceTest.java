package org.athos.core.service;

import org.athos.core.support.CoreTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class InternalApiServiceTest extends CoreTest {

  @MockitoSpyBean
  private InternalApiService internalApiService;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("athos.caches.api-key.current", () -> 1); // 1 second
  }

  @Test
  void testGetInternalApiKeys() throws IOException, InterruptedException {
    // Initial fetch, should get the first key from Vault
    var internalApiKeys = internalApiService.getInternalApiKeys();

    assertEquals(1, internalApiKeys.size());
    assertTrue(internalApiKeys.contains(TEST_API_KEY));

    // Add a new key to Vault
    vaultContainer.execInContainer("vault", "kv", "patch", "secrets/internal-api-key", "value=" + TEST_API_KEY + "2" );
    // Wait for 1 second to ensure the cache expires
    Thread.sleep(1000);

    // Fetch the keys again, should include the new key
    internalApiKeys = internalApiService.getInternalApiKeys();

    assertEquals(2, internalApiKeys.size());
    assertTrue(internalApiKeys.contains(TEST_API_KEY));
    assertTrue(internalApiKeys.contains(TEST_API_KEY + "2"));
  }

}
