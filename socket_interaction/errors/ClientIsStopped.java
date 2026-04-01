package socket_interaction.errors;

public class ClientIsStopped extends ClientException {
    public String getMessage() {
        return "Client stopped. To correct this restart server";
    }
}
