package io.github.lakshaybxt.knowra.service.impl;

import io.github.lakshaybxt.knowra.dto.DocumentResponseDto;
import io.github.lakshaybxt.knowra.entity.DocumentMetadata;
import io.github.lakshaybxt.knowra.entity.DocumentStatus;
import io.github.lakshaybxt.knowra.repository.DocumentMetadataRepo;
import io.github.lakshaybxt.knowra.service.DocumentMetadataService;
import io.github.lakshaybxt.knowra.service.DocumentIngestionService;
import io.github.lakshaybxt.knowra.service.DocumentParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentMetadataServiceImpl implements DocumentMetadataService {

  private final DocumentMetadataRepo documentMetadataRepo;
  private final DocumentParserService parserService;
  private final DocumentIngestionService ingestionService;
  private final JdbcTemplate jdbcTemplate;

  /**
   * Uploads a document and processes it for vector embeddings.
   *
   * @param file the document file to be uploaded
   * @return a response DTO containing information about the uploaded document
   */
  @Override
  public DocumentResponseDto uploadAndProcess(MultipartFile file) {

    String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
    String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

    /* Document Metadata Creation */
    DocumentMetadata metadata = DocumentMetadata.builder()
        .fileName(fileName)
        .contentType(contentType)
        .status(DocumentStatus.UPLOADING)
        .fileSize(file.getSize())
        .build();

    /* Save the metadata to the database */
    //DocumentMetadata savedMetadata = documentMetadataRepo.save(metadata);
    metadata = documentMetadataRepo.save(metadata);

    /* Parse the document and create vector embeddings */
    List<Document> parsedDocs = parserService.parseDocument(file);

    /* Ingest the parsed documents into the database */
    int chunksCreated = ingestionService.ingestDocuments(metadata, parsedDocs);

    // metadata.setTotalChunks(chunksCreated);


    return DocumentResponseDto.builder()
        .id(metadata.getId())
        .filename(metadata.getFileName())
        .fileSize(metadata.getFileSize())
        .documentStatus(metadata.getStatus())
        .chunksCreated(chunksCreated)
        .message("Document successfully processed and indexed")
        .build();
  }
}
