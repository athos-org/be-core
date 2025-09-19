package org.athos.core.support;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.vault.VaultContainer;

import java.io.IOException;
import java.util.UUID;

public abstract class CoreTest {

  private static final VaultContainer<?> vaultContainer = new VaultContainer<>("hashicorp/vault:1.15");

  @BeforeAll
  static void beforeAll() {
    vaultContainer.withVaultToken(UUID.randomUUID().toString()).withInitCommand(
        "secrets enable -path=secrets kv-v2",
        "auth enable approle",
        "write auth/approle/role/my-role token_policies=default secret_id_ttl=0 token_ttl=20m token_max_ttl=30m",
        "kv put secrets/internal-api-key value=internal-api-key"
    ).start();
  }

  @AfterAll
  static void afterAll() {
    vaultContainer.stop();
  }

  @DynamicPropertySource
  static void setVaultProperties(DynamicPropertyRegistry registry) throws IOException, InterruptedException {
    var roleId = vaultContainer.execInContainer("vault", "read", "-field=role_id", "auth/approle/role/my-role/role-id").getStdout().trim();
    var secretId = vaultContainer.execInContainer("vault", "write", "-f", "-field=secret_id", "auth/approle/role/my-role/secret-id").getStdout().trim();
    registry.add("integrations.vault.url", vaultContainer::getHttpHostAddress);
    registry.add("integrations.vault.role-id", () -> roleId);
    registry.add("integrations.vault.secret-id", () -> secretId);
    registry.add("integrations.vault.secret-root", () -> "secrets");
  }

}