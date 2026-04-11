# UI client

To start ui app run DesktopMain.java with javafx

# Command-line client
## Run client
```
java ./CommandLineMain.java --address server_address --port server_port
```

* server_address(optional) - web address of server
* server_port(optional) - port of server

## Command

* help - print list of all commands
* exit - exit program
* show_files - show all files and directories in current directory
* read - show file value
* delete - delete file or directory
* create_file - create new file
* rewrite_file - rewrite file
* copy_file - copy file
* create_or_rewrite_file - create file or rewrite if it's already exists
* copy_or_rewrite_file - copy file or rewrite if it's already exists
* rename - rename file or directory
* rename_file - change file name
* replace - change file or dir path
* create_directory - create new directory
* rename_directory - change directory name
* download_file - download file

Outdated commands:
* replace_file - change file path
