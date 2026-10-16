package io.github.lakshaybxt.knowra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEnvelope<T> {

  private Boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp;
}
