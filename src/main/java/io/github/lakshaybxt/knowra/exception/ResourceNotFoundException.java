package io.github.lakshaybxt.knowra.exception;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException() {
    super("Resource you are looking for is not found!");
  }


  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
