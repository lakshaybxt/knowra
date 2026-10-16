package io.github.lakshaybxt.knowra.service;

import io.github.lakshaybxt.knowra.entity.DocumentMetadata;
import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentIngestionService {
  int ingestDocuments(DocumentMetadata savedMetadata, List<Document> parsedDocs);
}
