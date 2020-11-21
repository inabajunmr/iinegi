package work.inabajun.iinegi.domain;

public class ImageEncodeFailedException extends RuntimeException {
    public ImageEncodeFailedException(String message) {
        super(message);
    }
    public ImageEncodeFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
