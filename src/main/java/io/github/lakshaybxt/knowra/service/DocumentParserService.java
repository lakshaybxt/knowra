package io.github.lakshaybxt.knowra.service;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentParserService {
  List<Document> parseDocument(MultipartFile file);
}
