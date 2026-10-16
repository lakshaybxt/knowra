package io.github.lakshaybxt.knowra.exception;

public class DocumentProcessingException extends RuntimeException {

  public DocumentProcessingException() {
    super("Error occurred while processing the document!");
  }

  public DocumentProcessingException(String message) {
    super(message);
  }

  public DocumentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
