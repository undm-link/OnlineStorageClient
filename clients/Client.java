package clients;

import socket_interaction.SocketManager;
import socket_interaction.errors.ClientAlreadyRun;

import java.io.IOException;

public abstract class Client {
    protected SocketManager socketManager;

    public Client(String address, int port) {
        socketManager = new SocketManager(address, port);
    }

    protected void run() throws ClientAlreadyRun, IOException {
        socketManager.run();
    }

    protected void stop() throws IOException {
        socketManager.stop();
    }

    protected void refresh() throws ClientAlreadyRun, IOException {
        stop();
        run();
    }
}
