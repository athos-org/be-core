package org.athos.core.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  protected LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", updatable = false)
  protected UUID createdBy;

  @LastModifiedDate
  @Column(name = "updated_date", nullable = false)
  protected LocalDateTime updatedDate;

  @LastModifiedBy
  @Column(name = "updated_by")
  protected UUID updatedBy;

}