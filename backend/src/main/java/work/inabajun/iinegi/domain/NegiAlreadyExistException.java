package work.inabajun.iinegi.domain;

public class NegiAlreadyExistException extends RuntimeException{
    public NegiAlreadyExistException(String message, Exception cause) {
        super(message, cause);
    }
}
