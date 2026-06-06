package org.marllon.caip.domains.image.exeptions;

public class ImageUploadException extends FileStorageException {
    public ImageUploadException(String message) {
        super(message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }

}
