package org.athos.core.support;

import org.athos.core.context.RequestHeaders;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.vault.VaultContainer;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.testcontainers.utility.MountableFile.forClasspathResource;

public abstract class CoreTest {

  public static final String TEST_API_KEY = "internal-api-key";

  private static final PostgreSQLContainer<?> pgContainer = new PostgreSQLContainer<>("postgres:16-alpine");
  protected static final VaultContainer<?> vaultContainer = new VaultContainer<>("hashicorp/vault:1.15");

  @BeforeAll
  static void beforeAll() {
    pgContainer.start();
    vaultContainer.withVaultToken(UUID.randomUUID().toString())
        .withCopyFileToContainer(forClasspathResource("vault/internal-policy.hcl"), "/vault/internal-policy.hcl")
        .withInitCommand(
            "secrets enable -path=secrets kv-v2",
            "auth enable approle",
            "write sys/policies/acl/internal-policy policy=@/vault/internal-policy.hcl",
            "write auth/approle/role/my-role token_policies=internal-policy secret_id_ttl=0 token_ttl=20m token_max_ttl=30m",
            "kv put secrets/internal-api-key value=" + TEST_API_KEY
        ).start();
  }

  @AfterAll
  static void afterAll() {
    pgContainer.stop();
    vaultContainer.stop();
  }

  @DynamicPropertySource
  static void setDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", pgContainer::getJdbcUrl);
    registry.add("spring.datasource.username", pgContainer::getUsername);
    registry.add("spring.datasource.password", pgContainer::getPassword);
    registry.add("spring.liquibase.default-schema", () -> "public");
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

  public static HttpHeaders getDefaultHeaders() {
    var headers = new HttpHeaders();
    headers.put(RequestHeaders.X_INTERNAL_API_TOKEN.getValue(), List.of(TEST_API_KEY));
    headers.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));
    return headers;
  }

}