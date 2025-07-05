package socket_interaction;

import socket_interaction.errors.ClientAlreadyRun;
import socket_interaction.errors.ClientIsStoped;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SocketManager {
    // Constants
    private static final int BUFFER_SIZE = 1024;

    // Private variables
    private final String address;
    private final int port;

    private boolean is_run = false;
    private Socket socket;
    DataOutputStream output_stream;
    DataInputStream input_stream;

    // Public Functions
    public SocketManager(String address, int port) {
        this.address = address;
        this.port = port;
    }
    public void run() throws Exception {
        if (is_run) throw new ClientAlreadyRun();

        socket = new Socket(address, port);
        output_stream = new DataOutputStream(socket.getOutputStream());
        input_stream = new DataInputStream(socket.getInputStream());

        is_run = true;
    }
    public void stop() throws Exception{
        if(is_run)
        {
            socket.close();
            is_run = false;
        }
    }
    public void send(String message) throws Exception{
        if(is_run) {
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] message_bytes = message.getBytes();
            for(int i = 0; i < message.length(); i += BUFFER_SIZE) {
                Arrays.fill(buffer, (byte) 0);
                System.arraycopy(message_bytes, i, buffer, 0, Math.min(BUFFER_SIZE, message_bytes.length - i));

                output_stream.write(buffer);
            }
        }
        else {
            throw new ClientIsStoped();
        }
    }
    public void send(byte[] message) throws Exception{
        if(is_run) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for(int i = 0; i < message.length; i += BUFFER_SIZE) {
                Arrays.fill(buffer, (byte) 0);
                System.arraycopy(message, i, buffer, 0, Math.min(BUFFER_SIZE, message.length - i));

                output_stream.write(buffer);
            }
        }
        else {
            throw new ClientIsStoped();
        }
    }
    public byte readByte() throws Exception {
        if(is_run) {
            return input_stream.readByte();
        }
        else {
            throw new ClientIsStoped();
        }
    }
    public byte[] readAllBytes() throws Exception {
        if(is_run) {
            return input_stream.readAllBytes();
        }
        else {
            throw new ClientIsStoped();
        }
    }
    public String read() throws Exception{
        if(is_run) {
            String response = new String(input_stream.readAllBytes(), StandardCharsets.UTF_8);
            long end = response.indexOf('\000');
            if(end != -1)
            {
                response = response.substring(0, response.indexOf('\000'));
            }
            return response;
        }
        else {
            throw new ClientIsStoped();
        }
    }
}


