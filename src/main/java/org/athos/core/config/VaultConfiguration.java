package org.athos.core.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "integrations.vault")
@Validated
@Data
public class VaultConfiguration {

  @URL
  private String url;
  @NotNull
  private String roleId;
  @NotNull
  private String secretId;
  @NotNull
  private String secretRoot;

}
