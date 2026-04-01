package socket_interaction.errors;

public class ClientAlreadyRun extends Exception {
    public String getMessage() {
        return "Server already run";
    }
}
