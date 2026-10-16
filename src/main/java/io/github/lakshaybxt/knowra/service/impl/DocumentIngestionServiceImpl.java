package io.github.lakshaybxt.knowra.service.impl;

import io.github.lakshaybxt.knowra.configuration.AppConfigProperties;
import io.github.lakshaybxt.knowra.entity.DocumentMetadata;
import io.github.lakshaybxt.knowra.entity.DocumentStatus;
import io.github.lakshaybxt.knowra.exception.DocumentProcessingException;
import io.github.lakshaybxt.knowra.repository.DocumentMetadataRepo;
import io.github.lakshaybxt.knowra.service.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

  private final VectorStore vectorStore;
  private final DocumentMetadataRepo documentMetadataRepo;
  private final AppConfigProperties appProperties;

  @Override
  public int ingestDocuments(DocumentMetadata metadata, List<Document> parsedDocs) {
    log.info("Ingesting documents[id = {}, name = {}, pages={}]", metadata.getId(), metadata.getFileName(), metadata.getFileSize());

    try {

      metadata.setStatus(DocumentStatus.PROCESSING);
      metadata.setTotalPages(parsedDocs.size());

      /* 1. Chunking using TokenTextSplitter */
      TokenTextSplitter splitter = TokenTextSplitter.builder()
          .withChunkSize(appProperties.getRag().getChunkSize())
          .withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars())
          .withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed())
          .withMaxNumChunks(appProperties.getRag().getMaxNumChunks())
          .withKeepSeparator(true)
          .build();

      List<Document> chunks = splitter.apply(parsedDocs);
      if(chunks.isEmpty()) {
        metadata.setStatus(DocumentStatus.FAILED);
        metadata.setErrorMessage("Document appears to be empty or unscannable. No content could be extracted.");
        documentMetadataRepo.save(metadata);
        return 0;
      }

      /* 2, Metadata enrichment for each chunk */
      List<Document> enrichedChunks = new ArrayList<>();
      int chunkIndex = 0;
      for (Document chunk : chunks) {
        chunk.getMetadata().put("documentId", metadata.getId().toString());
        chunk.getMetadata().put("fileName", metadata.getFileName());
        chunk.getMetadata().put("contentType", metadata.getContentType());
        chunk.getMetadata().put("chunkIndex", chunkIndex++);

        // Preserve or calculate page number if available
        Object pageNumber = chunk.getMetadata().get("page_number");
        if (pageNumber == null) {
          pageNumber = chunk.getMetadata().get("pageNumber");
        }

        if (pageNumber != null) {
          chunk.getMetadata().put("pageNumber", pageNumber);
        }

        Document enrichDoc = new Document(chunk.getText(), chunk.getMetadata());
        enrichedChunks.add(enrichDoc);
      }

      // 3. Write chunks and embeddings to the vector store
      log.info("Writing {} enriched chunks to the vector store for document: {}.", enrichedChunks.size(), metadata.getFileName());
      vectorStore.write(enrichedChunks);

      // 4. Update metadata with total chunks and status
      metadata.setTotalChunks(enrichedChunks.size());
      metadata.setStatus(DocumentStatus.INDEXED);
      metadata.setErrorMessage(null);
      documentMetadataRepo.save(metadata);

      log.info("Successfully Indexed document[id = {}, name = {}, pages={}, chunks={}]", metadata.getId(), metadata.getFileName(), metadata.getTotalPages(), enrichedChunks.size());

      return enrichedChunks.size();
    } catch (Exception ex) {
      log.error("Error ingesting documents[id = {}, name = {}]: {}", metadata.getId(), metadata.getFileName(), ex.getMessage(), ex);
      metadata.setStatus(DocumentStatus.FAILED);
      metadata.setErrorMessage("Error occurred while ingesting the document: " + ex.getMessage());
      documentMetadataRepo.save(metadata);

      throw new DocumentProcessingException("Failed to ingest document[id = " + metadata.getId() + ", name = " + metadata.getFileName() + "]: " + ex.getMessage(), ex);
    }
  }
}
