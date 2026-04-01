package socket_interaction;

import socket_interaction.errors.ClientAlreadyRun;
import socket_interaction.errors.ClientIsStopped;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SocketManager {
    // Constants
    private static final int BUFFER_SIZE = 1024;

    // Private variables
    private final String address;
    private final int port;

    private boolean isRun = false;
    private Socket socket;
    DataOutputStream outputStream;
    DataInputStream inputStream;

    // Public Functions
    public SocketManager(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void run() throws ClientAlreadyRun, IOException {
        if (isRun) throw new ClientAlreadyRun();

        socket = new Socket(address, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());

        isRun = true;
    }

    public void stop() throws IOException {
        if (isRun) {
            socket.close();
            isRun = false;
        }
    }

    public void send(String message) throws ClientIsStopped, IOException {
        if (isRun) {
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] message_bytes = message.getBytes();
            for (int i = 0; i < message.length(); i += BUFFER_SIZE) {
                Arrays.fill(buffer, (byte) 0);
                System.arraycopy(
                        message_bytes,
                        i,
                        buffer,
                        0,
                        Math.min(BUFFER_SIZE, message_bytes.length - i));

                outputStream.write(buffer);
            }
        } else {
            throw new ClientIsStopped();
        }
    }

    public void send(byte[] message) throws ClientIsStopped, IOException {
        if (isRun) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i = 0; i < message.length; i += BUFFER_SIZE) {
                Arrays.fill(buffer, (byte) 0);
                System.arraycopy(message, i, buffer, 0, Math.min(BUFFER_SIZE, message.length - i));

                outputStream.write(buffer);
            }
        } else {
            throw new ClientIsStopped();
        }
    }

    public byte readByte() throws ClientIsStopped, IOException {
        if (isRun) {
            return inputStream.readByte();
        } else {
            throw new ClientIsStopped();
        }
    }

    public byte[] readAllBytes() throws ClientIsStopped, IOException {
        if (isRun) {
            return inputStream.readAllBytes();
        } else {
            throw new ClientIsStopped();
        }
    }

    public String read() throws ClientIsStopped, IOException {
        if (isRun) {
            String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            long end = response.indexOf('\000');
            if (end != -1) {
                response = response.substring(0, response.indexOf('\000'));
            }
            return response;
        } else {
            throw new ClientIsStopped();
        }
    }
}
