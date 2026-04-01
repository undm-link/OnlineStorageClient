import clients.CommandClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

class CommandLineMain {
    private final static String DEFAULT_ADDRESS = "127.0.0.1";
    private final static int DEFAULT_PORT = 8000;

    public static void main(String[] args) {
        String address = null;
        int port = -1;
        boolean is_argument_valid = true;
        for (int i = 1; i < args.length; ++i) {
            if (Objects.equals(args[i], "--address") || Objects.equals(args[i], "--port")) {
                if (i == args.length - 1) {
                    System.out.println("It must be at least one argument after \"--address\" and \"--port\"");
                    is_argument_valid = false;
                    break;
                }
                else if (Objects.equals(args[i], "--address")) {
                    address = args[i + 1];
                }
                else if (Objects.equals(args[i], "--port")) {
                    try {
                        port = Integer.parseInt(args[i + 1]);
                    }
                    catch (java.lang.NumberFormatException ex) {
                        is_argument_valid = false;
                        System.out.println(ex.getMessage());
                    }
                }

                ++i;
            }
        }

        if(address == null) {
            address = DEFAULT_ADDRESS;
        }
        if(port == -1) {
            port = DEFAULT_PORT;
        }

        if(is_argument_valid) {
            try {
                var client = new CommandClient(address, port);

                var scanner = new Scanner(System.in);

                boolean is_program_run = true;
                while(is_program_run) {
                    try {
                        System.out.println("Enter command:");
                        String command = scanner.nextLine();

                        switch (command) {
                            case "help" -> System.out.println("""
                                    exit - exit program
                                    show_files, ls - show all files and directories in current directory
                                    read - show file value
                                    delete - delete file or directory
                                    create_file - create new file
                                    rewrite_file - rewrite file
                                    copy_file - copy file
                                    create_or_rewrite_file - create file or rewrite if it's already exists
                                    copy_or_rewrite_file - copy file or rewrite if it's already exists
                                    rename - rename file or directory
                                    replace - change path of file or directory
                                    create_directory - create new directory
                                    rename_directory - change directory name
                                    download_file - download file
                                    """);
                            case "exit" -> is_program_run = false;
                            case "show_files", "ls" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();
                                var entries = client.show_files(path);

                                System.out.println("Directories:");
                                for(var el : entries) {
                                    if(el.type() == CommandClient.DirectoryEntryType.DIRECTORY) {
                                        System.out.println(el.name());
                                    }
                                }

                                System.out.println();
                                System.out.println("Files:");
                                for(var el : entries) {
                                    if(el.type() == CommandClient.DirectoryEntryType.FILE) {
                                        System.out.println(el.name());
                                    }
                                }
                            }
                            case "read" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();

                                System.out.println("Text:\n" + client.read(path));
                            }
                            case "delete" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();

                                try
                                {
                                    client.delete(path);
                                } catch (clients.errors.RequestError ex) {
                                    if(ex.type == CommandClient.ResponseStatus.DIRECTORY_NOT_EMPTY) {
                                        System.out.println("Directory isn't empty. If you still want to delete it write 'YES'");
                                        if(scanner.nextLine().equals("YES")) {
                                            client.delete_all(path);
                                        }
                                    }
                                    else {
                                        throw ex;
                                    }
                                }
                            }
                            case "create_file" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();
                                System.out.println("Enter text:");
                                String text = scanner.nextLine();

                                client.create_file(path, text);
                            }
                            case "rewrite_file" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();
                                System.out.println("Enter text:");
                                String text = scanner.nextLine();

                                client.rewrite_file(path, text);
                            }
                            case "copy_file" -> {
                                System.out.println("Enter to path of copied file:");
                                String file_path = scanner.nextLine();
                                System.out.println("Enter to path to new place for file:");
                                String path = scanner.nextLine();

                                File file = new File(file_path);
                                if(file.exists()) {
                                    var input_stream = new FileInputStream(file);
                                    client.create_file(path, new String(input_stream.readAllBytes(), StandardCharsets.UTF_8));
                                }
                                else {
                                    System.out.println("File doesn't exists");
                                }
                            }
                            case "create_or_rewrite_file" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();
                                System.out.println("Enter text:");
                                String text = scanner.nextLine();

                                client.create_or_rewrite_file(path, text);
                            }
                            case "copy_or_rewrite_file" -> {
                                System.out.println("Enter to path of copied file:");
                                String file_path = scanner.nextLine();
                                System.out.println("Enter to path to new place for file:");
                                String path = scanner.nextLine();

                                File file = new File(file_path);
                                if(file.exists()) {
                                    var input_stream = new FileInputStream(file);
                                    client.create_or_rewrite_file(path, new String(input_stream.readAllBytes(), StandardCharsets.UTF_8));
                                }
                                else {
                                    System.out.println("File doesn't exists");
                                }
                            }
                            case "rename" -> {
                                System.out.println("Enter path:");
                                String path = scanner.nextLine();
                                System.out.println("Enter new name:");
                                String name = scanner.nextLine();

                                client.change_data(path, name);
                            }
                            case "rename_file" -> {
                                System.out.println("Enter path to file:");
                                String path = scanner.nextLine();
                                System.out.println("Enter new name:");
                                String name = scanner.nextLine();

                                client.change_file_data(path, name);
                            }
                            case "replace" -> {
                                System.out.println("Enter old path:");
                                String old_path = scanner.nextLine();
                                System.out.println("Enter new path:");
                                String new_path = scanner.nextLine();

                                client.replace(old_path, new_path);
                            }
                            case "create_directory" -> {
                                System.out.println("Enter path to new directory:");
                                String path = scanner.nextLine();
                                client.create_directory(path);
                            }
                            case "rename_directory" -> {
                                System.out.println("Enter path to directory:");
                                String path = scanner.nextLine();
                                System.out.println("Enter new name");
                                String name = scanner.nextLine();

                                client.change_directory_data(path, name);
                            }
                            case "download_file" -> {
                                System.out.println("Enter path to file to download:");
                                String path = scanner.nextLine();
                                String file_content = client.read(path);
                                System.out.println("Enter path to directory of downloading:");
                                var file = new FileOutputStream(scanner.nextLine());
                                file.write(file_content.getBytes());
                            }
                            // Outdated commands
                            case "replace_file" -> {
                                System.out.println("Enter old path:");
                                String old_path = scanner.nextLine();
                                System.out.println("Enter new path:");
                                String new_path = scanner.nextLine();

                                client.replace_file(old_path, new_path);
                            }
                            case null, default -> System.out.println("Command is unknown");
                        }
                    }
                    catch(clients.errors.RequestError ex) {
                        System.out.println("Request Error:");
                        System.out.println(ex.type);
                        System.out.println(ex.message);
                    }
                    catch(Exception ex) {
                        System.out.println("Error occurred");
                        System.out.println("Error message: " + ex.getMessage());
                    }

                    System.out.println();
                }
            }
            catch(Exception ex) {
                System.out.println("Error occurred");
                System.out.println("Error message: " + ex.getMessage());
            }

            System.out.println("Program stoped");
        }
    }
}
