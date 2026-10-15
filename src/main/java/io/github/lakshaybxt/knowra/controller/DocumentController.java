package io.github.lakshaybxt.knowra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Document Management",
    description = "Endpoint for uploading, listing, and managing documents and their vector embeddings."
)
@RestController
@Slf4j
@RequestMapping("/api/v1/documents")
public class DocumentController {

  @Operation(
      summary = "Upload and index a document(PDF, DOCX, TEXT, MD, CSV",
      description = "Uploads a document to the system and index document files and processes it for vector embeddings."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Document uploaded successfully."),
      @ApiResponse(responseCode = "400", description = "Invalid document format."),
      @ApiResponse(responseCode = "500", description = "Internal server error.")
  })
  @PostMapping
  public ResponseEntity<String> uploadDocument() {
    log.info("Received request to upload document.");
    // Placeholder for document upload logic
    return ResponseEntity.ok("Document uploaded successfully.");
  }
}
