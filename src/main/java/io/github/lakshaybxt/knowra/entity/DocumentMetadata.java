package io.github.lakshaybxt.knowra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_metadata")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentMetadata {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;
  private Long fileSize;
  private Integer totalPages;
  private Integer totalChunks;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @Column(length = 1000)
  private String errorMessage;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

}
