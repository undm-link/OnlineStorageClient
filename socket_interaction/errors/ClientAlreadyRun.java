package socket_interaction.errors;

public class ClientAlreadyRun extends ClientException {
    public String getMessage() {
        return "Server already run";
    }
}
