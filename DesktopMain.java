import clients.CommandClient;

import javafx.application.Application;

import javafx.application.Platform;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.UnknownHostException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DesktopMain extends Application {
    private CommandClient client = null;
    private String mainPath = "/";
    private Runnable refreshFilesViewEvent;

    // Window open functions
    private void openConnectWindow(Stage stage) {
        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        addressField.setMinSize(300, 50);
        addressField.setMaxSize(300, 50);
        addressField.setFont(new Font(20));
        addressField.setAlignment(Pos.CENTER);
        addressField.isHover();

        TextField portField = new TextField();
        portField.setPromptText("Port");
        portField.setMinSize(300, 50);
        portField.setMaxSize(300, 50);
        portField.setFont(new Font(20));
        portField.setAlignment(Pos.CENTER);
        portField.setTranslateY(10);
        portField.setFocusTraversable(false);

        Button submit = new Button("Connect");
        submit.setMinSize(200, 40);
        submit.setMaxSize(200, 40);
        submit.setFont(new Font(20));
        submit.setAlignment(Pos.CENTER);
        submit.setTranslateY(30);
        submit.setTranslateX(50);
        submit.setStyle("-fx-background-color:white");
        submit.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        submit.setDefaultButton(true);
        submit.setOnAction(
                _ -> {
                    try {
                        client =
                                new CommandClient(
                                        addressField.getText(),
                                        Integer.parseInt(portField.getText()));

                        openDirectoryViewWindow("/");
                        stage.close();
                    } catch (NumberFormatException ex) {
                        openErrorWindow("Port field must contain number");
                    } catch (UnknownHostException ex) {
                        openErrorWindow("Address is incorrect");
                    } catch (Exception ex) {
                        openErrorWindow(ex + " " + ex.getMessage());
                    }
                });

        VBox controlPanel = new VBox(addressField, portField, submit);

        StackPane layout = new StackPane(new Group(controlPanel));
        StackPane.setAlignment(controlPanel, Pos.TOP_CENTER);

        Scene scene = new Scene(layout);
        stage.setHeight(500);
        stage.setWidth(750);
        stage.setMinHeight(300);
        stage.setMinWidth(350);
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> Platform.exit());
        stage.show();
    }

    private void openConnectWindow() {
        openConnectWindow(new Stage());
    }

    private void openErrorWindow(String message) {
        Stage stage = new Stage();

        Label errorLabel = new Label("Error");
        errorLabel.setFont(new Font(40));
        errorLabel.setLayoutX(200);
        errorLabel.setLayoutY(10);

        Label messageLabel = new Label(message);
        messageLabel.setFont(new Font(20));

        FlowPane messageLabelPane = new FlowPane(messageLabel);
        FlowPane.setMargin(messageLabel, new Insets(5, 5, 5, 5));

        ScrollPane scrollPane = new ScrollPane(messageLabelPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMinSize(450, 300);
        scrollPane.setMaxSize(450, 300);
        scrollPane.setLayoutY(75);
        scrollPane.setLayoutX(25);

        Button okButton = new Button("OK");
        okButton.setMinSize(100, 40);
        okButton.setMaxSize(100, 40);
        okButton.setLayoutY(390);
        okButton.setTranslateX(200);
        okButton.setFont(new Font(20));
        okButton.setAlignment(Pos.CENTER);
        okButton.setStyle("-fx-background-color:white");
        okButton.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        okButton.setDefaultButton(true);
        okButton.setOnAction(_ -> stage.close());

        Group group = new Group(errorLabel, scrollPane, okButton);

        Scene scene = new Scene(group);

        stage.setHeight(500);
        stage.setWidth(500);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshFilesView(
            Stage stage,
            String newPath,
            FlowPane filesBox,
            Label pathLabel,
            Button uploadButton,
            Button backButton,
            Button createDirectoryButton)
            throws Exception {
        String path = Paths.get(newPath).normalize().toString();
        var entries = client.showFiles(path);
        pathLabel.setText(path);
        filesBox.getChildren().clear();
        for (var el : entries) {
            AnchorPane pane = new AnchorPane();
            pane.setMinWidth(150);
            pane.setMaxWidth(150);
            pane.setMinHeight(125);
            pane.setMaxHeight(125);

            Label fileName = new Label(el.name());
            fileName.setAlignment(Pos.CENTER);
            fileName.setFont(new Font(15));
            AnchorPane.setTopAnchor(fileName, 90d);
            AnchorPane.setRightAnchor(fileName, 0d);
            AnchorPane.setBottomAnchor(fileName, 15d);
            AnchorPane.setLeftAnchor(fileName, 0d);

            Button newButton = new Button();
            newButton.setFocusTraversable(false);
            AnchorPane.setTopAnchor(newButton, 0d);
            AnchorPane.setLeftAnchor(newButton, 25d);

            ContextMenu menu = new ContextMenu();
            menu.setStyle(
                    """
                    -fx-border-color: black;
                    -fx-border-width: 0.5;\s
                    """);
            MenuItem openMenuItem = new MenuItem("open");
            MenuItem renameMenuItem = new MenuItem("rename");
            renameMenuItem.setOnAction(_ -> openChangeNameWindow(path + "/" + el.name()));
            MenuItem downloadMenuItem = new MenuItem("download");
            downloadMenuItem.setOnAction(
                    _ -> {
                        try {
                            DirectoryChooser downloadFileChooser = new DirectoryChooser();
                            File directory = downloadFileChooser.showDialog(stage);

                            if (directory != null) {
                                if (new File(directory + "/" + el.name()).exists()) {
                                    openErrorWindow("File already exists");
                                } else if (directory.isDirectory() && directory.exists()) {
                                    var file = new FileOutputStream(directory + "/" + el.name());
                                    file.write(client.read(path + "/" + el.name()).getBytes());
                                } else {
                                    openErrorWindow("You must choose directory");
                                }
                            }
                        } catch (Exception ex) {
                            openErrorWindow(ex.getMessage());
                        }
                    });
            MenuItem deleteMenuItem = new MenuItem("delete");
            deleteMenuItem.setOnAction(
                    _ -> {
                        try {
                            client.delete(path + "/" + el.name());
                            refreshFilesViewEvent.run();
                        } catch (clients.errors.RequestError ex) {
                            if (ex.type == CommandClient.ResponseStatus.DIRECTORY_NOT_EMPTY) {
                                try {
                                    openDirectoryNoEmptyWindow(path + "/" + el.name());
                                } catch (Exception e) {
                                    openErrorWindow(ex.getMessage());
                                }
                            } else {
                                openErrorWindow(ex.getMessage());
                            }
                        } catch (Exception ex) {
                            openErrorWindow(ex.getMessage());
                        }
                    });
            menu.getItems().addAll(openMenuItem, renameMenuItem, downloadMenuItem, deleteMenuItem);
            newButton.setContextMenu(menu);

            ImageView newButtonImageView = new ImageView();
            newButton.setStyle("-fx-background-color:rgb(0,0,0,0)");
            if (el.type() == CommandClient.DirectoryEntryType.DIRECTORY) {
                newButtonImageView.setImage(new Image("static_files/images/directory_icon.png"));
                EventHandler<ActionEvent> event =
                        _ -> {
                            try {
                                mainPath = path + "/" + el.name();
                                refreshFilesViewEvent.run();
                            } catch (Exception ex) {
                                openErrorWindow(ex + " " + ex.getMessage());
                            }
                        };
                openMenuItem.setOnAction(event);
                newButton.setOnAction(event);
            } else if (el.type() == CommandClient.DirectoryEntryType.FILE) {
                newButtonImageView.setImage(new Image("static_files/images/file_icon.png"));
                EventHandler<ActionEvent> event =
                        _ -> {
                            try {
                                openFileViewWindow(path + "/" + el.name());
                            } catch (Exception ex) {
                                openErrorWindow(ex + " " + ex.getMessage());
                            }
                        };
                openMenuItem.setOnAction(event);
                newButton.setOnAction(event);
            }
            newButtonImageView.setFitHeight(75);
            newButtonImageView.setFitWidth(75);
            newButton.setGraphic(newButtonImageView);
            newButton.setMinWidth(100);
            newButton.setMaxWidth(100);
            newButton.setMinHeight(100);
            newButton.setMaxHeight(100);

            pane.getChildren().addAll(newButton, fileName);
            filesBox.getChildren().add(pane);
        }

        backButton.setOnAction(
                _ -> {
                    try {
                        Path parentPath = Paths.get(path).getParent();
                        if (parentPath != null) {
                            mainPath = parentPath.toString();
                            refreshFilesViewEvent.run();
                        }
                    } catch (Exception ex) {
                        openErrorWindow(ex + " " + ex.getMessage());
                    }
                });
        FileChooser uploadFileChooser = new FileChooser();
        uploadButton.setOnAction(
                _ -> {
                    File file = uploadFileChooser.showOpenDialog(stage);
                    if (file != null) {
                        try (var inputStream = new FileInputStream(file)) {
                            client.createFile(
                                    path + "/" + file.getName(),
                                    new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                            refreshFilesViewEvent.run();
                        } catch (java.io.FileNotFoundException _) {
                        } catch (Exception ex) {
                            openErrorWindow(ex.getMessage());
                        }
                    }
                });

        createDirectoryButton.setOnAction(_ -> openCreateDirectoryWindow(path));
        mainPath = path;
    }

    private void refreshFilesView(
            Stage stage,
            FlowPane filesBox,
            Label pathLabel,
            Button uploadButton,
            Button backButton,
            Button createDirectoryButton)
            throws Exception {
        refreshFilesView(
                stage,
                mainPath,
                filesBox,
                pathLabel,
                uploadButton,
                backButton,
                createDirectoryButton);
    }

    private void openDirectoryViewWindow(String path) throws Exception {
        Stage stage = new Stage();
        AnchorPane layout = new AnchorPane();
        FlowPane filesBox = new FlowPane(10, 10);

        Button backButton = new Button();
        ImageView backButtonImageView = new ImageView("static_files/images/back_icon.png");
        backButtonImageView.setFitHeight(50);
        backButtonImageView.setFitWidth(50);
        backButton.setGraphic(backButtonImageView);
        backButton.setMinWidth(50);
        backButton.setMaxWidth(50);
        backButton.setMinHeight(50);
        backButton.setMaxHeight(50);
        backButton.setFocusTraversable(false);
        AnchorPane.setTopAnchor(backButton, 15d);
        AnchorPane.setLeftAnchor(backButton, 15d);

        Label pathLabel = new Label(Paths.get(path).normalize().toString());
        AnchorPane.setTopAnchor(pathLabel, 10d);
        AnchorPane.setRightAnchor(pathLabel, 90d);
        AnchorPane.setLeftAnchor(pathLabel, 90d);
        pathLabel.setFont(new Font(40));
        pathLabel.setAlignment(Pos.CENTER);

        Button disconnectButton = new Button();
        ImageView disconnectButtonImageView =
                new ImageView("static_files/images/disconnect_icon.png");
        disconnectButtonImageView.setFitHeight(50);
        disconnectButtonImageView.setFitWidth(50);
        disconnectButton.setGraphic(disconnectButtonImageView);
        disconnectButton.setMinWidth(50);
        disconnectButton.setMaxWidth(50);
        disconnectButton.setMinHeight(50);
        disconnectButton.setMaxHeight(50);
        disconnectButton.setFocusTraversable(false);
        disconnectButton.setOnAction(
                _ -> {
                    openConnectWindow();
                    stage.close();
                });
        AnchorPane.setTopAnchor(disconnectButton, 15d);
        AnchorPane.setRightAnchor(disconnectButton, 15d);

        Button uploadButton = new Button();
        ImageView uploadButtonImageView = new ImageView("static_files/images/upload_icon.png");
        uploadButtonImageView.setFitHeight(50);
        uploadButtonImageView.setFitWidth(50);
        uploadButton.setGraphic(uploadButtonImageView);
        uploadButton.setMinWidth(50);
        uploadButton.setMaxWidth(50);
        uploadButton.setMinHeight(50);
        uploadButton.setMaxHeight(50);
        uploadButton.setFocusTraversable(false);
        AnchorPane.setTopAnchor(uploadButton, 15d);
        AnchorPane.setRightAnchor(uploadButton, 80d);

        Button refreshButton = new Button();
        ImageView refreshButtonImageView = new ImageView("static_files/images/refresh_icon.png");
        refreshButtonImageView.setFitHeight(50);
        refreshButtonImageView.setFitWidth(50);
        refreshButton.setGraphic(refreshButtonImageView);
        refreshButton.setMinWidth(50);
        refreshButton.setMaxWidth(50);
        refreshButton.setMinHeight(50);
        refreshButton.setMaxHeight(50);
        refreshButton.setFocusTraversable(false);
        refreshButton.setOnAction(_ -> refreshFilesViewEvent.run());
        AnchorPane.setTopAnchor(refreshButton, 15d);
        AnchorPane.setRightAnchor(refreshButton, 145d);

        Button createDirectoryButton = new Button();
        ImageView createDirectoryButtonImageView =
                new ImageView("static_files/images/create_directory.png");
        createDirectoryButtonImageView.setFitHeight(50);
        createDirectoryButtonImageView.setFitWidth(50);
        createDirectoryButton.setGraphic(createDirectoryButtonImageView);
        createDirectoryButton.setMinWidth(50);
        createDirectoryButton.setMaxWidth(50);
        createDirectoryButton.setMinHeight(50);
        createDirectoryButton.setMaxHeight(50);
        createDirectoryButton.setFocusTraversable(false);
        createDirectoryButton.setOnAction(_ -> refreshFilesViewEvent.run());
        AnchorPane.setTopAnchor(createDirectoryButton, 15d);
        AnchorPane.setRightAnchor(createDirectoryButton, 210d);

        AnchorPane topGroup =
                new AnchorPane(
                        backButton,
                        pathLabel,
                        disconnectButton,
                        uploadButton,
                        refreshButton,
                        createDirectoryButton);
        topGroup.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                new BorderWidths(0, 0, 1, 0))));
        topGroup.setMinHeight(80);
        topGroup.setMaxHeight(80);
        topGroup.prefWidthProperty().bind(layout.widthProperty());

        AnchorPane.setTopAnchor(filesBox, 90d);
        AnchorPane.setRightAnchor(filesBox, 0d);
        AnchorPane.setBottomAnchor(filesBox, 0d);
        AnchorPane.setLeftAnchor(filesBox, 10d);
        refreshFilesView(
                stage, path, filesBox, pathLabel, uploadButton, backButton, createDirectoryButton);

        layout.getChildren().addAll(topGroup, filesBox);
        Scene scene = new Scene(layout);

        stage.setWidth(1500);
        stage.setHeight(900);
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> Platform.exit());

        stage.show();

        refreshFilesViewEvent =
                () -> {
                    try {
                        refreshFilesView(
                                stage,
                                filesBox,
                                pathLabel,
                                uploadButton,
                                backButton,
                                createDirectoryButton);
                    } catch (Exception ex) {
                        openErrorWindow(ex + " " + ex.getMessage());
                    }
                };
    }

    private void openFileViewWindow(String path) throws Exception {
        path = Paths.get(path).normalize().toString();
        Stage stage = new Stage();
        AnchorPane layout = new AnchorPane();

        Label pathLabel = new Label(Paths.get(path).normalize().toString());
        pathLabel.setFont(new Font(40));
        pathLabel.setAlignment(Pos.TOP_CENTER);
        pathLabel.prefWidthProperty().bind(layout.widthProperty());

        Label textLabel = new Label(client.read(path));
        textLabel.setFont(new Font(20));
        FlowPane textLabelGroup = new FlowPane(textLabel);
        FlowPane.setMargin(textLabel, new Insets(5, 5, 5, 5));

        ScrollPane scrollPane = new ScrollPane(textLabelGroup);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        AnchorPane scrollAnchorPane = new AnchorPane(scrollPane);
        AnchorPane.setTopAnchor(scrollPane, 100d);
        AnchorPane.setRightAnchor(scrollPane, 25d);
        AnchorPane.setBottomAnchor(scrollPane, 100d);
        AnchorPane.setLeftAnchor(scrollPane, 25d);
        scrollAnchorPane.prefWidthProperty().bind(layout.widthProperty());
        scrollAnchorPane.prefHeightProperty().bind(layout.heightProperty());

        scrollPane.prefWidthProperty().bind(scrollAnchorPane.widthProperty());
        scrollPane.prefHeightProperty().bind(scrollAnchorPane.heightProperty());

        Button okButton = new Button("OK");
        okButton.setMinSize(100, 40);
        okButton.setMaxSize(100, 40);
        okButton.setFont(new Font(20));
        okButton.setAlignment(Pos.CENTER);
        okButton.setStyle("-fx-background-color:white");
        okButton.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        okButton.setDefaultButton(true);
        okButton.setOnAction(_ -> stage.close());

        StackPane okButtonPane = new StackPane(okButton);
        okButtonPane.prefWidthProperty().bind(layout.widthProperty());
        okButtonPane.setMinHeight(100);
        StackPane.setAlignment(okButton, Pos.CENTER);
        AnchorPane.setBottomAnchor(okButtonPane, 0d);

        layout.getChildren().addAll(pathLabel, scrollAnchorPane, okButtonPane);

        Scene scene = new Scene(layout);

        stage.setHeight(500);
        stage.setWidth(500);
        stage.setScene(scene);
        stage.show();
    }

    private void openChangeNameWindow(String path) {
        Stage stage = new Stage();

        TextField newNameField = new TextField();
        newNameField.setPromptText("Enter new name");
        newNameField.setMinSize(300, 50);
        newNameField.setMaxSize(300, 50);
        newNameField.setFont(new Font(20));
        newNameField.setAlignment(Pos.CENTER);
        newNameField.isHover();

        Button submit = new Button("Rename");
        submit.setMinSize(200, 40);
        submit.setMaxSize(200, 40);
        submit.setFont(new Font(20));
        submit.setAlignment(Pos.CENTER);
        submit.setTranslateY(30);
        submit.setTranslateX(50);
        submit.setStyle("-fx-background-color:white");
        submit.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        submit.setDefaultButton(true);
        submit.setOnAction(
                _ -> {
                    try {
                        client.changeData(path, newNameField.getText());
                        refreshFilesViewEvent.run();
                        stage.close();
                    } catch (Exception ex) {
                        openErrorWindow(ex + " " + ex.getMessage());
                    }
                });

        VBox controlPanel = new VBox(newNameField, submit);

        StackPane layout = new StackPane(new Group(controlPanel));
        StackPane.setAlignment(controlPanel, Pos.TOP_CENTER);

        Scene scene = new Scene(layout);
        stage.setHeight(500);
        stage.setWidth(750);
        stage.setMinHeight(300);
        stage.setMinWidth(350);
        stage.setScene(scene);
        stage.show();
    }

    private void openDirectoryNoEmptyWindow(String path) {
        Stage stage = new Stage();
        AnchorPane layout = new AnchorPane();

        Label messageLabel =
                new Label("       Directory isn't empty.\nDo you still want to delete it?");
        messageLabel.setFont(new Font(30));
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.prefWidthProperty().bind(layout.widthProperty());
        AnchorPane.setTopAnchor(messageLabel, 100d);

        Button yesButton = new Button("YES");
        yesButton.setMinSize(100, 60);
        yesButton.setMaxSize(100, 60);
        yesButton.setLayoutY(300);
        yesButton.setLayoutX(75);
        yesButton.setTranslateX(200);
        yesButton.setFont(new Font(20));
        yesButton.setAlignment(Pos.CENTER);
        yesButton.setStyle("-fx-background-color:white");
        yesButton.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        yesButton.setOnAction(
                _ -> {
                    try {
                        client.deleteAll(path);
                        refreshFilesViewEvent.run();
                    } catch (Exception ex) {
                        openErrorWindow(ex + " " + ex.getMessage());
                    }

                    stage.close();
                });

        Button noButton = new Button("NO");
        noButton.setMinSize(100, 60);
        noButton.setMaxSize(100, 60);
        noButton.setLayoutY(300);
        noButton.setLayoutX(-75);
        noButton.setTranslateX(200);
        noButton.setFont(new Font(20));
        noButton.setAlignment(Pos.CENTER);
        noButton.setStyle("-fx-background-color:white");
        noButton.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        noButton.setOnAction(_ -> stage.close());

        layout.getChildren().addAll(messageLabel, yesButton, noButton);

        Scene scene = new Scene(layout);

        stage.setHeight(500);
        stage.setWidth(500);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void openCreateDirectoryWindow(String path) {
        Stage stage = new Stage();

        TextField nameField = new TextField();
        nameField.setPromptText("Enter directory name");
        nameField.setMinSize(300, 50);
        nameField.setMaxSize(300, 50);
        nameField.setFont(new Font(20));
        nameField.setAlignment(Pos.CENTER);
        nameField.isHover();

        Button submit = new Button("Create");
        submit.setMinSize(200, 40);
        submit.setMaxSize(200, 40);
        submit.setFont(new Font(20));
        submit.setAlignment(Pos.CENTER);
        submit.setTranslateY(30);
        submit.setTranslateX(50);
        submit.setStyle("-fx-background-color:white");
        submit.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        submit.setDefaultButton(true);
        submit.setOnAction(
                _ -> {
                    try {
                        client.createDirectory(path + "/" + nameField.getText());
                        refreshFilesViewEvent.run();
                        stage.close();
                    } catch (Exception ex) {
                        openErrorWindow(ex + " " + ex.getMessage());
                    }
                });

        VBox controlPanel = new VBox(nameField, submit);

        StackPane layout = new StackPane(new Group(controlPanel));
        StackPane.setAlignment(controlPanel, Pos.TOP_CENTER);

        Scene scene = new Scene(layout);
        stage.setHeight(500);
        stage.setWidth(750);
        stage.setMinHeight(300);
        stage.setMinWidth(350);
        stage.setScene(scene);
        stage.show();
    }

    // Public Functions
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        openConnectWindow(stage);
    }
}
