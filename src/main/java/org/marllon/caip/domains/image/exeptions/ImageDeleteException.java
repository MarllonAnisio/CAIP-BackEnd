package org.marllon.caip.domains.image.exeptions;

public class ImageDeleteException extends FileStorageException {
    public ImageDeleteException(String message) {
        super(message);
    }

    public ImageDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}
