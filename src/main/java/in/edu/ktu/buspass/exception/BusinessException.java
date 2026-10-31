package in.edu.ktu.buspass.exception;

/** A rule violation that should be shown to the user, not a server error. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
