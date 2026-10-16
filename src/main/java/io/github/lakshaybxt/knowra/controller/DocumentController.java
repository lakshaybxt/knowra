package io.github.lakshaybxt.knowra.controller;

import io.github.lakshaybxt.knowra.dto.ApiEnvelope;
import io.github.lakshaybxt.knowra.dto.DocumentResponseDto;
import io.github.lakshaybxt.knowra.service.DocumentMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Tag(
    name = "Document Management",
    description = "Endpoint for uploading, listing, and managing documents and their vector embeddings."
)
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
public class DocumentController {

  private final DocumentMetadataService documentService;

  @Operation(
      summary = "Upload and index a document(PDF, DOCX, TEXT, MD, CSV",
      description = "Uploads a document to the system and index document files and processes it for vector embeddings."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Document uploaded successfully."),
      @ApiResponse(responseCode = "400", description = "Invalid document format."),
      @ApiResponse(responseCode = "500", description = "Internal server error.")
  })
  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiEnvelope<DocumentResponseDto>> uploadDocument(
      @RequestParam("file") MultipartFile file
      ) {
    log.info("Received request to upload document.");

    DocumentResponseDto response = documentService.uploadAndProcess(file);

    ApiEnvelope<DocumentResponseDto> envelope = ApiEnvelope.<DocumentResponseDto>builder()
        .success(true)
        .data(response)
        .message("Document uploaded and processed successfully.")
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(envelope);

  }
}
