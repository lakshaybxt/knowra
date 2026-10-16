package io.github.lakshaybxt.knowra.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ApiErrorEnvelope {

  private final boolean success;
  private final String message;
  private final int status;
  private final String path;
  private final LocalDateTime timestamp;
  private final Map<String, String> errors;
}
