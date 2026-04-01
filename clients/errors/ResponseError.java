package clients.errors;

public class ResponseError extends Exception {
    public String getMessage() {
        return "Server send incorrect response";
    }
}
