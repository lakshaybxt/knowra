package io.github.lakshaybxt.knowra.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat Management", description = "Endpoint for managing chat interactions, including document uploads and conversational queries.")
@RestController
@Slf4j
@RequestMapping("/api/v1/chat")
public class ChatController {

  @Operation(summary = "Test Chat Endpoint", description = "Uploads a document to the system and processes it for vector embeddings.")
  @ApiResponses
  @PostMapping
  public ResponseEntity<String> chat() {
    return ResponseEntity.ok("This is just testing configuration.");
  }
}
