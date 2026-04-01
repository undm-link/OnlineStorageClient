package socket_interaction.errors;

public class ClientIsStoped extends Exception {
    public String getMessage() {
        return "Client stoped. To correct this restart server";
    }
}
