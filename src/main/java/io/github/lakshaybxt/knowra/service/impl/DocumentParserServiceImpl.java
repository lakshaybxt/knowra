package io.github.lakshaybxt.knowra.service.impl;

import io.github.lakshaybxt.knowra.exception.DocumentProcessingException;
import io.github.lakshaybxt.knowra.service.DocumentParserService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class DocumentParserServiceImpl implements DocumentParserService {

  @Override
  public List<Document> parseDocument(MultipartFile file) {

    String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
    String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

    log.info("Parsing document: {}, size: {} bytes with content type: {}", fileName, file.getSize(), contentType);

    try {

      Resource resource = new ByteArrayResource(file.getBytes()){
        @Override
        public @Nullable String getFilename() {
          return fileName;
        }
      };

      if (fileName.toLowerCase().endsWith(".pdf") || contentType.contains("pdf")) {
        return parsePdf(resource);
      } else {
        return parseGenericFile(resource);
      }

    } catch (IOException ex) {
      log.error("Error parsing document: {}", fileName, ex);

      throw new DocumentProcessingException("Could not read uploaded file: " + fileName, ex);

    } catch (Exception ex) {
      log.error("Error parsing document: {}", fileName, ex);

      throw new DocumentProcessingException("Error occurred while parsing the document: " + fileName, ex);
    }
  }

  private List<Document> parseGenericFile(Resource resource) {
    log.info("Parsing PDF document: {}", resource.getFilename());

    TikaDocumentReader documentReader = new TikaDocumentReader(resource);
    return documentReader.read();
  }

  private List<Document> parsePdf(Resource resource) {

    log.info("Parsing generic document: {}", resource.getFilename());

    PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
        .withPageTopMargin(0)
        .withPageBottomMargin(0)
        .build();

    PagePdfDocumentReader documentReader = new PagePdfDocumentReader(resource, config);
    return documentReader.read();
  }
}
