package es.upm.miw.devops.domain.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String detail) {
        super("Resource not found. " + detail);
    }
}
