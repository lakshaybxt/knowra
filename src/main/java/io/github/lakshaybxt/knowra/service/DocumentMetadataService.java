package io.github.lakshaybxt.knowra.service;

import io.github.lakshaybxt.knowra.dto.DocumentResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentMetadataService {

  DocumentResponseDto uploadAndProcess(MultipartFile file);
}
