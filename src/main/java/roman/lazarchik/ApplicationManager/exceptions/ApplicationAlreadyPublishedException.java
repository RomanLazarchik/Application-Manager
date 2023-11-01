package roman.lazarchik.ApplicationManager.exceptions;

public class ApplicationAlreadyPublishedException extends RuntimeException {
    public ApplicationAlreadyPublishedException(String message) {
        super(message);
    }
}
