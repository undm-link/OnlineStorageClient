package clients.errors;

import clients.CommandClient;

public class RequestError extends Exception {
    public final CommandClient.ResponseStatus type;
    public final String message;

    public RequestError(CommandClient.ResponseStatus type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
