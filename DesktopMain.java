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
    private String main_path = "/";
    // private final lib.EventHandler<Runnable> refresh_files_view_event = new lib.EventHandler<>();
    private Runnable refresh_files_view_event;

    // Window open functions
    private void open_connect_window(Stage stage) {
        TextField address_field = new TextField();
        address_field.setPromptText("Address");
        address_field.setMinSize(300, 50);
        address_field.setMaxSize(300, 50);
        address_field.setFont(new Font(20));
        address_field.setAlignment(Pos.CENTER);
        address_field.isHover();

        TextField port_field = new TextField();
        port_field.setPromptText("Port");
        port_field.setMinSize(300, 50);
        port_field.setMaxSize(300, 50);
        port_field.setFont(new Font(20));
        port_field.setAlignment(Pos.CENTER);
        port_field.setTranslateY(10);
        port_field.setFocusTraversable(false);

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
                                        address_field.getText(),
                                        Integer.parseInt(port_field.getText()));

                        open_directory_view_window("/");
                        stage.close();
                    } catch (NumberFormatException ex) {
                        open_error_window("Port field must contain number");
                    } catch (UnknownHostException ex) {
                        open_error_window("Address is incorrect");
                    } catch (Exception ex) {
                        open_error_window(ex + " " + ex.getMessage());
                    }
                });

        VBox control_panel = new VBox(address_field, port_field, submit);

        StackPane layout = new StackPane(new Group(control_panel));
        StackPane.setAlignment(control_panel, Pos.TOP_CENTER);

        Scene scene = new Scene(layout);
        stage.setHeight(500);
        stage.setWidth(750);
        stage.setMinHeight(300);
        stage.setMinWidth(350);
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> Platform.exit());
        stage.show();
    }

    private void open_connect_window() {
        open_connect_window(new Stage());
    }

    private void open_error_window(String message) {
        Stage stage = new Stage();

        Label error_label = new Label("Error");
        error_label.setFont(new Font(40));
        error_label.setLayoutX(200);
        error_label.setLayoutY(10);

        Label message_label = new Label(message);
        message_label.setFont(new Font(20));

        FlowPane message_label_pane = new FlowPane(message_label);
        FlowPane.setMargin(message_label, new Insets(5, 5, 5, 5));

        ScrollPane scroll_pane = new ScrollPane(message_label_pane);
        scroll_pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll_pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll_pane.setMinSize(450, 300);
        scroll_pane.setMaxSize(450, 300);
        scroll_pane.setLayoutY(75);
        scroll_pane.setLayoutX(25);

        Button ok_button = new Button("OK");
        ok_button.setMinSize(100, 40);
        ok_button.setMaxSize(100, 40);
        ok_button.setLayoutY(390);
        ok_button.setTranslateX(200);
        ok_button.setFont(new Font(20));
        ok_button.setAlignment(Pos.CENTER);
        ok_button.setStyle("-fx-background-color:white");
        ok_button.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        ok_button.setDefaultButton(true);
        ok_button.setOnAction(_ -> stage.close());

        Group group = new Group(error_label, scroll_pane, ok_button);

        Scene scene = new Scene(group);

        stage.setHeight(500);
        stage.setWidth(500);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void refresh_files_view(
            Stage stage,
            String new_path,
            FlowPane files_box,
            Label path_label,
            Button upload_button,
            Button back_button,
            Button create_directory_button)
            throws Exception {
        String path = Paths.get(new_path).normalize().toString();
        var entries = client.show_files(path);
        path_label.setText(path);
        files_box.getChildren().clear();
        for (var el : entries) {
            AnchorPane pane = new AnchorPane();
            pane.setMinWidth(150);
            pane.setMaxWidth(150);
            pane.setMinHeight(125);
            pane.setMaxHeight(125);

            Label file_name = new Label(el.name());
            file_name.setAlignment(Pos.CENTER);
            file_name.setFont(new Font(15));
            AnchorPane.setTopAnchor(file_name, 90d);
            AnchorPane.setRightAnchor(file_name, 0d);
            AnchorPane.setBottomAnchor(file_name, 15d);
            AnchorPane.setLeftAnchor(file_name, 0d);

            Button new_button = new Button();
            new_button.setFocusTraversable(false);
            AnchorPane.setTopAnchor(new_button, 0d);
            AnchorPane.setLeftAnchor(new_button, 25d);

            ContextMenu menu = new ContextMenu();
            menu.setStyle(
                    """
                    -fx-border-color: black;
                    -fx-border-width: 0.5;\s
                    """);
            MenuItem open_menu_item = new MenuItem("open");
            MenuItem rename_menu_item = new MenuItem("rename");
            rename_menu_item.setOnAction(_ -> open_change_name_window(path + "/" + el.name()));
            MenuItem download_menu_item = new MenuItem("download");
            download_menu_item.setOnAction(
                    _ -> {
                        try {
                            DirectoryChooser download_file_chooser = new DirectoryChooser();
                            File directory = download_file_chooser.showDialog(stage);

                            if (directory != null) {
                                if (new File(directory + "/" + el.name()).exists()) {
                                    open_error_window("File already exists");
                                } else if (directory.isDirectory() && directory.exists()) {
                                    var file = new FileOutputStream(directory + "/" + el.name());
                                    file.write(client.read(path + "/" + el.name()).getBytes());
                                } else {
                                    open_error_window("You must choose directory");
                                }
                            }
                        } catch (Exception ex) {
                            open_error_window(ex.getMessage());
                        }
                    });
            MenuItem delete_menu_item = new MenuItem("delete");
            delete_menu_item.setOnAction(
                    _ -> {
                        try {
                            client.delete(path + "/" + el.name());
                            refresh_files_view_event.run();
                        } catch (clients.errors.RequestError ex) {
                            if (ex.type == CommandClient.ResponseStatus.DIRECTORY_NOT_EMPTY) {
                                try {
                                    open_directory_no_empty_window(path + "/" + el.name());
                                } catch (Exception e) {
                                    open_error_window(ex.getMessage());
                                }
                            } else {
                                open_error_window(ex.getMessage());
                            }
                        } catch (Exception ex) {
                            open_error_window(ex.getMessage());
                        }
                    });
            menu.getItems()
                    .addAll(open_menu_item, rename_menu_item, download_menu_item, delete_menu_item);
            new_button.setContextMenu(menu);

            ImageView new_button_image_view = new ImageView();
            new_button.setStyle("-fx-background-color:rgb(0,0,0,0)");
            if (el.type() == CommandClient.DirectoryEntryType.DIRECTORY) {
                new_button_image_view.setImage(new Image("static_files/images/directory_icon.png"));
                EventHandler<ActionEvent> event =
                        _ -> {
                            try {
                                main_path = path + "/" + el.name();
                                refresh_files_view_event.run();
                            } catch (Exception ex) {
                                open_error_window(ex + " " + ex.getMessage());
                            }
                        };
                open_menu_item.setOnAction(event);
                new_button.setOnAction(event);
            } else if (el.type() == CommandClient.DirectoryEntryType.FILE) {
                new_button_image_view.setImage(new Image("static_files/images/file_icon.png"));
                EventHandler<ActionEvent> event =
                        _ -> {
                            try {
                                open_file_view_window(path + "/" + el.name());
                            } catch (Exception ex) {
                                open_error_window(ex + " " + ex.getMessage());
                            }
                        };
                open_menu_item.setOnAction(event);
                new_button.setOnAction(event);
            }
            new_button_image_view.setFitHeight(75);
            new_button_image_view.setFitWidth(75);
            new_button.setGraphic(new_button_image_view);
            new_button.setMinWidth(100);
            new_button.setMaxWidth(100);
            new_button.setMinHeight(100);
            new_button.setMaxHeight(100);

            pane.getChildren().addAll(new_button, file_name);
            files_box.getChildren().add(pane);
        }

        back_button.setOnAction(
                _ -> {
                    try {
                        Path parent_path = Paths.get(path).getParent();
                        if (parent_path != null) {
                            main_path = parent_path.toString();
                            refresh_files_view_event.run();
                        }
                    } catch (Exception ex) {
                        open_error_window(ex + " " + ex.getMessage());
                    }
                });
        FileChooser upload_file_chooser = new FileChooser();
        upload_button.setOnAction(
                _ -> {
                    File file = upload_file_chooser.showOpenDialog(stage);
                    if (file != null) {
                        try (var input_stream = new FileInputStream(file)) {
                            client.create_file(
                                    path + "/" + file.getName(),
                                    new String(
                                            input_stream.readAllBytes(), StandardCharsets.UTF_8));
                            refresh_files_view_event.run();
                        } catch (java.io.FileNotFoundException _) {
                        } catch (Exception ex) {
                            open_error_window(ex.getMessage());
                        }
                    }
                });

        create_directory_button.setOnAction(_ -> open_create_directory_window(path));
        main_path = path;
    }

    private void refresh_files_view(
            Stage stage,
            FlowPane files_box,
            Label path_label,
            Button upload_button,
            Button back_button,
            Button create_directory_button)
            throws Exception {
        refresh_files_view(
                stage,
                main_path,
                files_box,
                path_label,
                upload_button,
                back_button,
                create_directory_button);
    }

    private void open_directory_view_window(String path) throws Exception {
        Stage stage = new Stage();
        AnchorPane layout = new AnchorPane();
        FlowPane files_box = new FlowPane(10, 10);

        Button back_button = new Button();
        ImageView back_button_image_view = new ImageView("static_files/images/back_icon.png");
        back_button_image_view.setFitHeight(50);
        back_button_image_view.setFitWidth(50);
        back_button.setGraphic(back_button_image_view);
        back_button.setMinWidth(50);
        back_button.setMaxWidth(50);
        back_button.setMinHeight(50);
        back_button.setMaxHeight(50);
        back_button.setFocusTraversable(false);
        AnchorPane.setTopAnchor(back_button, 15d);
        AnchorPane.setLeftAnchor(back_button, 15d);

        Label path_label = new Label(Paths.get(path).normalize().toString());
        AnchorPane.setTopAnchor(path_label, 10d);
        AnchorPane.setRightAnchor(path_label, 90d);
        AnchorPane.setLeftAnchor(path_label, 90d);
        path_label.setFont(new Font(40));
        path_label.setAlignment(Pos.CENTER);

        Button disconnect_button = new Button();
        ImageView disconnect_button_image_view =
                new ImageView("static_files/images/disconnect_icon.png");
        disconnect_button_image_view.setFitHeight(50);
        disconnect_button_image_view.setFitWidth(50);
        disconnect_button.setGraphic(disconnect_button_image_view);
        disconnect_button.setMinWidth(50);
        disconnect_button.setMaxWidth(50);
        disconnect_button.setMinHeight(50);
        disconnect_button.setMaxHeight(50);
        disconnect_button.setFocusTraversable(false);
        disconnect_button.setOnAction(
                _ -> {
                    open_connect_window();
                    stage.close();
                });
        AnchorPane.setTopAnchor(disconnect_button, 15d);
        AnchorPane.setRightAnchor(disconnect_button, 15d);

        Button upload_button = new Button();
        ImageView upload_button_image_view = new ImageView("static_files/images/upload_icon.png");
        upload_button_image_view.setFitHeight(50);
        upload_button_image_view.setFitWidth(50);
        upload_button.setGraphic(upload_button_image_view);
        upload_button.setMinWidth(50);
        upload_button.setMaxWidth(50);
        upload_button.setMinHeight(50);
        upload_button.setMaxHeight(50);
        upload_button.setFocusTraversable(false);
        AnchorPane.setTopAnchor(upload_button, 15d);
        AnchorPane.setRightAnchor(upload_button, 80d);

        Button refresh_button = new Button();
        ImageView refresh_button_image_view = new ImageView("static_files/images/refresh_icon.png");
        refresh_button_image_view.setFitHeight(50);
        refresh_button_image_view.setFitWidth(50);
        refresh_button.setGraphic(refresh_button_image_view);
        refresh_button.setMinWidth(50);
        refresh_button.setMaxWidth(50);
        refresh_button.setMinHeight(50);
        refresh_button.setMaxHeight(50);
        refresh_button.setFocusTraversable(false);
        refresh_button.setOnAction(_ -> refresh_files_view_event.run());
        AnchorPane.setTopAnchor(refresh_button, 15d);
        AnchorPane.setRightAnchor(refresh_button, 145d);

        Button create_directory_button = new Button();
        ImageView create_directory_button_image_view =
                new ImageView("static_files/images/create_directory.png");
        create_directory_button_image_view.setFitHeight(50);
        create_directory_button_image_view.setFitWidth(50);
        create_directory_button.setGraphic(create_directory_button_image_view);
        create_directory_button.setMinWidth(50);
        create_directory_button.setMaxWidth(50);
        create_directory_button.setMinHeight(50);
        create_directory_button.setMaxHeight(50);
        create_directory_button.setFocusTraversable(false);
        create_directory_button.setOnAction(_ -> refresh_files_view_event.run());
        AnchorPane.setTopAnchor(create_directory_button, 15d);
        AnchorPane.setRightAnchor(create_directory_button, 210d);

        AnchorPane top_group =
                new AnchorPane(
                        back_button,
                        path_label,
                        disconnect_button,
                        upload_button,
                        refresh_button,
                        create_directory_button);
        top_group.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                new BorderWidths(0, 0, 1, 0))));
        top_group.setMinHeight(80);
        top_group.setMaxHeight(80);
        top_group.prefWidthProperty().bind(layout.widthProperty());

        AnchorPane.setTopAnchor(files_box, 90d);
        AnchorPane.setRightAnchor(files_box, 0d);
        AnchorPane.setBottomAnchor(files_box, 0d);
        AnchorPane.setLeftAnchor(files_box, 10d);
        refresh_files_view(
                stage,
                path,
                files_box,
                path_label,
                upload_button,
                back_button,
                create_directory_button);

        layout.getChildren().addAll(top_group, files_box);
        Scene scene = new Scene(layout);

        stage.setWidth(1500);
        stage.setHeight(900);
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> Platform.exit());

        stage.show();

        refresh_files_view_event =
                () -> {
                    try {
                        refresh_files_view(
                                stage,
                                files_box,
                                path_label,
                                upload_button,
                                back_button,
                                create_directory_button);
                    } catch (Exception ex) {
                        open_error_window(ex + " " + ex.getMessage());
                    }
                };
    }

    private void open_file_view_window(String path) throws Exception {
        path = Paths.get(path).normalize().toString();
        Stage stage = new Stage();
        AnchorPane layout = new AnchorPane();

        Label path_label = new Label(Paths.get(path).normalize().toString());
        path_label.setFont(new Font(40));
        path_label.setAlignment(Pos.TOP_CENTER);
        path_label.prefWidthProperty().bind(layout.widthProperty());

        Label text_label = new Label(client.read(path));
        text_label.setFont(new Font(20));
        FlowPane text_label_group = new FlowPane(text_label);
        FlowPane.setMargin(text_label, new Insets(5, 5, 5, 5));

        ScrollPane scroll_pane = new ScrollPane(text_label_group);
        scroll_pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll_pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        AnchorPane scroll_anchor_pane = new AnchorPane(scroll_pane);
        AnchorPane.setTopAnchor(scroll_pane, 100d);
        AnchorPane.setRightAnchor(scroll_pane, 25d);
        AnchorPane.setBottomAnchor(scroll_pane, 100d);
        AnchorPane.setLeftAnchor(scroll_pane, 25d);
        scroll_anchor_pane.prefWidthProperty().bind(layout.widthProperty());
        scroll_anchor_pane.prefHeightProperty().bind(layout.heightProperty());

        scroll_pane.prefWidthProperty().bind(scroll_anchor_pane.widthProperty());
        scroll_pane.prefHeightProperty().bind(scroll_anchor_pane.heightProperty());

        Button ok_button = new Button("OK");
        ok_button.setMinSize(100, 40);
        ok_button.setMaxSize(100, 40);
        ok_button.setFont(new Font(20));
        ok_button.setAlignment(Pos.CENTER);
        ok_button.setStyle("-fx-background-color:white");
        ok_button.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        ok_button.setDefaultButton(true);
        ok_button.setOnAction(_ -> stage.close());

        StackPane ok_button_pane = new StackPane(ok_button);
        ok_button_pane.prefWidthProperty().bind(layout.widthProperty());
        ok_button_pane.setMinHeight(100);
        StackPane.setAlignment(ok_button, Pos.CENTER);
        AnchorPane.setBottomAnchor(ok_button_pane, 0d);

        layout.getChildren().addAll(path_label, scroll_anchor_pane, ok_button_pane);

        Scene scene = new Scene(layout);

        stage.setHeight(500);
        stage.setWidth(500);
        stage.setScene(scene);
        stage.show();
    }

    private void open_change_name_window(String path) {
        Stage stage = new Stage();

        TextField new_name_field = new TextField();
        new_name_field.setPromptText("Enter new name");
        new_name_field.setMinSize(300, 50);
        new_name_field.setMaxSize(300, 50);
        new_name_field.setFont(new Font(20));
        new_name_field.setAlignment(Pos.CENTER);
        new_name_field.isHover();

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
                        client.change_data(path, new_name_field.getText());
                        refresh_files_view_event.run();
                        stage.close();
                    } catch (Exception ex) {
                        open_error_window(ex + " " + ex.getMessage());
                    }
                });

        VBox control_panel = new VBox(new_name_field, submit);

        StackPane layout = new StackPane(new Group(control_panel));
        StackPane.setAlignment(control_panel, Pos.TOP_CENTER);

        Scene scene = new Scene(layout);
        stage.setHeight(500);
        stage.setWidth(750);
        stage.setMinHeight(300);
        stage.setMinWidth(350);
        stage.setScene(scene);
        stage.show();
    }

    private void open_directory_no_empty_window(String path) {
        Stage stage = new Stage();
        AnchorPane layout = new AnchorPane();

        Label message_label =
                new Label("       Directory isn't empty.\nDo you still want to delete it?");
        message_label.setFont(new Font(30));
        message_label.setAlignment(Pos.CENTER);
        message_label.prefWidthProperty().bind(layout.widthProperty());
        AnchorPane.setTopAnchor(message_label, 100d);

        Button yes_button = new Button("YES");
        yes_button.setMinSize(100, 60);
        yes_button.setMaxSize(100, 60);
        yes_button.setLayoutY(300);
        yes_button.setLayoutX(75);
        yes_button.setTranslateX(200);
        yes_button.setFont(new Font(20));
        yes_button.setAlignment(Pos.CENTER);
        yes_button.setStyle("-fx-background-color:white");
        yes_button.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        yes_button.setOnAction(
                _ -> {
                    try {
                        client.delete_all(path);
                        refresh_files_view_event.run();
                    } catch (Exception ex) {
                        open_error_window(ex + " " + ex.getMessage());
                    }

                    stage.close();
                });

        Button no_button = new Button("NO");
        no_button.setMinSize(100, 60);
        no_button.setMaxSize(100, 60);
        no_button.setLayoutY(300);
        no_button.setLayoutX(-75);
        no_button.setTranslateX(200);
        no_button.setFont(new Font(20));
        no_button.setAlignment(Pos.CENTER);
        no_button.setStyle("-fx-background-color:white");
        no_button.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                CornerRadii.EMPTY,
                                BorderWidths.DEFAULT)));
        no_button.setOnAction(_ -> stage.close());

        layout.getChildren().addAll(message_label, yes_button, no_button);

        Scene scene = new Scene(layout);

        stage.setHeight(500);
        stage.setWidth(500);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    private void open_create_directory_window(String path) {
        Stage stage = new Stage();

        TextField name_field = new TextField();
        name_field.setPromptText("Enter directory name");
        name_field.setMinSize(300, 50);
        name_field.setMaxSize(300, 50);
        name_field.setFont(new Font(20));
        name_field.setAlignment(Pos.CENTER);
        name_field.isHover();

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
                        client.create_directory(path + "/" + name_field.getText());
                        refresh_files_view_event.run();
                        stage.close();
                    } catch (Exception ex) {
                        open_error_window(ex + " " + ex.getMessage());
                    }
                });

        VBox control_panel = new VBox(name_field, submit);

        StackPane layout = new StackPane(new Group(control_panel));
        StackPane.setAlignment(control_panel, Pos.TOP_CENTER);

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
        open_connect_window(stage);
    }
}
