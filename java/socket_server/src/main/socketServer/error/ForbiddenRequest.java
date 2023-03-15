package main.socketServer.error;

public class ForbiddenRequest extends RuntimeException {
    public ForbiddenRequest(String message) {
        super(message);
    }
}
