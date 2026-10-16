package io.github.lakshaybxt.knowra.repository;

import io.github.lakshaybxt.knowra.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentMetadataRepo extends JpaRepository<DocumentMetadata, UUID> {
}
