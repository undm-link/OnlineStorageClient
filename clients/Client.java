package clients;

import socket_interaction.SocketManager;

public abstract class Client {
    protected SocketManager socketManager;

    public Client(String address, int port) {
        socketManager = new SocketManager(address, port);
    }

    protected void run() throws Exception {
        socketManager.run();
    }

    protected void stop() throws Exception {
        socketManager.stop();
    }

    protected void refresh() throws Exception {
        stop();
        run();
    }
}
