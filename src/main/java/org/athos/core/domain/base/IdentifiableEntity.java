package org.athos.core.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@MappedSuperclass
public class IdentifiableEntity extends AuditableEntity implements Identifiable<UUID> {

  @Id
  @Column(nullable = false, updatable = false)
  protected UUID id;

  @PrePersist
  public void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }

}
