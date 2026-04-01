package clients;

import clients.errors.RequestError;
import clients.errors.ResponseError;

import socket_interaction.errors.ClientException;
import socket_interaction.errors.ClientIsStopped;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CommandClient extends Client {
    // Public functions
    public CommandClient(String address, int port) {
        super(address, port);
    }

    // Private functions
    private String getResponse() throws ClientIsStopped, IOException, RequestError {
        ResponseStatus status = ResponseStatus.getByInt(socketManager.readByte());

        String response = socketManager.read();

        if (status != ResponseStatus.OK) throw new clients.errors.RequestError(status, response);

        return response;
    }

    private byte[] getResponseBytes() throws ClientIsStopped, IOException, RequestError {
        ResponseStatus status = ResponseStatus.getByInt(socketManager.readByte());

        byte[] response = socketManager.readAllBytes();

        if (status != ResponseStatus.OK)
            throw new clients.errors.RequestError(
                    status, new String(response, StandardCharsets.UTF_8));

        return response;
    }

    // Commands
    public DirectoryEntry[] showFiles(String path)
            throws ClientException, IOException, RequestError, ResponseError {
        try {
            run();
            socketManager.send("show_files " + path);

            String[] arr = getResponse().split(" ");
            DirectoryEntry[] files = new DirectoryEntry[arr.length / 2];
            stop();

            for (int i = 0; i < arr.length / 2; ++i) {
                DirectoryEntryType type =
                        switch (arr[2 * i]) {
                            case "file" -> DirectoryEntryType.FILE;
                            case "directory" -> DirectoryEntryType.DIRECTORY;
                            case "" -> null;
                            default -> throw new ResponseError();
                        };
                if (type != null) files[i] = new DirectoryEntry(type, arr[2 * i + 1]);
            }

            return files;
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public String read(String path) throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("read " + path);
            String text = getResponse();
            stop();
            return text;
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public byte[] readBytes(String path) throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("read " + path);
            byte[] text = getResponseBytes();
            stop();
            return text;
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void deleteAll(String path) throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("delete_all " + path);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void delete(String path) throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("delete " + path);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void changeData(String path, String newName, String newPath)
            throws ClientException, IOException, RequestError {
        try {
            String message = "change_data " + path;
            if (newName != null) {
                message += " --name " + newName;
            }
            if (newPath != null) {
                message += " --dir " + newPath;
            }

            run();
            socketManager.send(message);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void changeData(String path, String newName)
            throws ClientException, IOException, RequestError {
        changeData(path, newName, null);
    }

    public void createFile(String filePath, String text)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("create_file " + filePath + " " + text.length());
            socketManager.send(text);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void createFile(String filePath, byte[] text)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("create_file " + filePath + " " + text.length);
            socketManager.send(text);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void rewriteFile(String filePath, String text)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("rewrite_file " + filePath + " " + text.length());
            socketManager.send(text);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void rewriteFile(String filePath, byte[] text)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("rewrite_file " + filePath + " " + text.length);
            socketManager.send(text);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void createOrRewriteFile(String filePath, String text)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("create_or_rewrite_file " + filePath + " " + text.length());
            socketManager.send(text);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void createOrRewriteFile(String filePath, byte[] text)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("create_or_rewrite_file " + filePath + " " + text.length);
            socketManager.send(text);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void changeFileData(String path, String newName)
            throws ClientException, IOException, RequestError {
        try {
            String message = "change_file_data " + path;
            if (newName != null) {
                message += " --name " + newName;
            }

            run();
            socketManager.send(message);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void replace(String oldPath, String newPath)
            throws ClientException, IOException, RequestError {
        changeData(oldPath, null, newPath);
    }

    public void createDirectory(String path) throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("create_directory " + path);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    public void changeDirectoryData(String path, String newName)
            throws ClientException, IOException, RequestError {
        try {
            String message = "change_directory_data " + path;
            if (newName != null) {
                message += " --name " + newName;
            }

            run();
            socketManager.send(message);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    // Outdated commands
    public void replaceFile(String oldPath, String newPath)
            throws ClientException, IOException, RequestError {
        try {
            run();
            socketManager.send("replace_file " + oldPath + " " + newPath);
            getResponse();
            stop();
        } catch (Exception ex) {
            stop();
            throw ex;
        }
    }

    // Inner classes
    public enum ResponseStatus {
        OK(1),
        UNKNOWN_ERROR(-1),

        // Client Errors
        BAD_REQUEST(2),
        INCORRECT_ARGUMENTS(3),
        INCORRECT_COMMAND(4),
        COMMAND_CANT_BE_EXECUTED(5),
        DIRECTORY_NOT_EMPTY(6),

        // Server Errors
        SERVER_ERROR(129);

        public final int code;

        ResponseStatus(int code) {
            this.code = code;
        }

        static ResponseStatus getByInt(int num) {
            for (var type : values()) {
                if (type.code == num) {
                    return type;
                }
            }

            return null;
        }
    }

    public enum DirectoryEntryType {
        FILE,
        DIRECTORY,
    }

    public record DirectoryEntry(DirectoryEntryType type, String name) {}
}
