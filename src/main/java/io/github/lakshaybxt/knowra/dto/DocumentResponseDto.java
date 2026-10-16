package io.github.lakshaybxt.knowra.dto;

import io.github.lakshaybxt.knowra.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponseDto {

  private UUID id;
  private String filename;
  private Long fileSize;
  private DocumentStatus documentStatus;
  private Integer chunksCreated;
  private String message;
}
