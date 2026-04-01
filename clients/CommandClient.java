package clients;

import java.nio.charset.StandardCharsets;

public class CommandClient extends Client{
    // Public functions
    public CommandClient(String address, int port) {
        super(address, port);
    }

    // Private functions
    private String get_response() throws Exception{
        ResponseStatus status = ResponseStatus.getByInt(socket_manager.readByte());

        String response = socket_manager.read();

        if(status != ResponseStatus.OK) throw new clients.errors.RequestError(status, response);

        return response;
    }

    private byte[] get_response_bytes() throws Exception{
        ResponseStatus status = ResponseStatus.getByInt(socket_manager.readByte());

        byte[] response = socket_manager.readAllBytes();

        if(status != ResponseStatus.OK) throw new clients.errors.RequestError(status, new String(response, StandardCharsets.UTF_8));

        return response;
    }

    // Commands
    public DirectoryEntry[] show_files(String path) throws Exception {
        try {
            run();
            socket_manager.send("show_files " + path);

            String[] arr = get_response().split(" ");
            DirectoryEntry[] files = new DirectoryEntry[arr.length/2];
            stop();

            for(int i = 0; i < arr.length/2; ++i) {
                DirectoryEntryType type = switch (arr[2*i]) {
                    case "file" -> DirectoryEntryType.FILE;
                    case "directory" -> DirectoryEntryType.DIRECTORY;
                    case "" -> null;
                    default -> throw new Exception("Server send incorrect response");
                };
                if(type != null) files[i] = new DirectoryEntry(type, arr[2*i + 1]);
            }

            return files;
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public String read(String path) throws Exception {
        try {
            run();
            socket_manager.send("read " + path);
            String text = get_response();
            stop();
            return text;
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public byte[] read_bytes(String path) throws Exception {
        try {
            run();
            socket_manager.send("read " + path);
            byte[] text = get_response_bytes();
            stop();
            return text;
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void delete_all(String path)  throws Exception {
        try {
            run();
            socket_manager.send("delete_all " + path);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void delete(String path)  throws Exception {
        try {
            run();
            socket_manager.send("delete " + path);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void change_data(String path, String new_name, String new_path) throws Exception{
        try {
            String message = "change_data " + path;
            if(new_name != null)
            {
                message += " --name " + new_name;
            }
            if(new_path != null)
            {
                message += " --dir " + new_path;
            }

            run();
            socket_manager.send(message);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void change_data(String path, String new_name) throws Exception {
        change_data(path, new_name, null);
    }

    public void create_file(String file_path, String text)  throws Exception {
        try {
            run();
            socket_manager.send("create_file " + file_path + " " + text.length());
            socket_manager.send(text);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void create_file(String file_path, byte[] text)  throws Exception {
        try {
            run();
            socket_manager.send("create_file " + file_path + " " + text.length);
            socket_manager.send(text);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void rewrite_file(String file_path, String text)  throws Exception {
        try {
            run();
            socket_manager.send("rewrite_file " + file_path + " " + text.length());
            socket_manager.send(text);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void rewrite_file(String file_path, byte[] text)  throws Exception {
        try {
            run();
            socket_manager.send("rewrite_file " + file_path + " " + text.length);
            socket_manager.send(text);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void create_or_rewrite_file(String file_path, String text)  throws Exception {
        try{
        run();
            socket_manager.send("create_or_rewrite_file " + file_path + " " + text.length());
            socket_manager.send(text);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void create_or_rewrite_file(String file_path, byte[] text)  throws Exception {
        try{
            run();
            socket_manager.send("create_or_rewrite_file " + file_path + " " + text.length);
            socket_manager.send(text);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void change_file_data(String path, String new_name)  throws Exception {
        try {
            String message = "change_file_data " + path;
            if(new_name != null)
            {
                message += " --name " + new_name;
            }

            run();
            socket_manager.send(message);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void replace(String old_path, String new_path) throws Exception {
        change_data(old_path, null, new_path);
    }

    public void create_directory(String path)  throws Exception {
        try {
            run();
            socket_manager.send("create_directory " + path);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    public void change_directory_data(String path, String new_name)  throws Exception {
        try {
            String message = "change_directory_data " + path;
            if(new_name != null)
            {
                message += " --name " + new_name;
            }

            run();
            socket_manager.send(message);
            get_response();
            stop();
        } catch(Exception ex) {
            stop();
            throw ex;
        }
    }

    // Outdated commands
    public void replace_file(String old_path, String new_path)  throws Exception {
        try {
            run();
            socket_manager.send("replace_file " + old_path + " " + new_path);
            get_response();
            stop();
        } catch(Exception ex) {
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
