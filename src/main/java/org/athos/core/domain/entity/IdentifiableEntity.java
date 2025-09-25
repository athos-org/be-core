package org.athos.core.domain.entity;

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
public class IdentifiableEntity<R extends IdentifiableEntity<R>> extends AuditableEntity implements Identifiable<UUID, R> {

  @Id
  @Column(nullable = false, updatable = false)
  protected UUID id;

  public R setId(UUID id) {
    this.id = id;
    return (R) this;
  }

  @PrePersist
  public void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
  }

}
