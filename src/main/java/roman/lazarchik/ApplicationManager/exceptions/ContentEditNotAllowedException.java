package roman.lazarchik.ApplicationManager.exceptions;

public class ContentEditNotAllowedException extends RuntimeException {
    public ContentEditNotAllowedException(String message) {
        super(message);
    }
}
