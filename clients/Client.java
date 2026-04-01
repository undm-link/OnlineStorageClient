package clients;

import socket_interaction.SocketManager;

public abstract class Client {
    protected SocketManager socket_manager;

    public Client(String address, int port) {
        socket_manager = new SocketManager(address, port);
    }

    protected void run() throws Exception {
        socket_manager.run();
    }

    protected void stop() throws Exception {
        socket_manager.stop();
    }

    protected void refresh() throws Exception {
        stop();
        run();
    }
}
