package work.inabajun.iinegi.domain;

public class NegiNotFoundException extends RuntimeException{
    public NegiNotFoundException(String message) {
        super(message);
    }
    public NegiNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
