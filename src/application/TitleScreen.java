package application;

import javafx.animation.AnimationTimer;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TitleScreen
{
    private static Button btnTutorial, btnPlay;
    private static GridPane titleScreenPane;
    private static Label lblTitle;

    // Initialize title screen
    public static void init()
    {
        // Crate title screen root gridpane
        titleScreenPane = new GridPane();
        titleScreenPane.setMinWidth(AppProps.REAL_WIDTH);
        titleScreenPane.setMinHeight(AppProps.REAL_HEIGHT);
        titleScreenPane.setAlignment(Pos.CENTER);
        titleScreenPane.setVisible(true);

        // Create label and buttons
        lblTitle = new Label("ROUGE");
        lblTitle.setFont(Font.loadFont(AppProps.fontAPath, 30 * AppProps.SCALE));
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setAlignment(Pos.CENTER);
        lblTitle.setPrefWidth(AppProps.REAL_WIDTH);

        // Add label to pane
        titleScreenPane.getRowConstraints().add(new RowConstraints(500));
        titleScreenPane.add(lblTitle, 0, 0);
        Font buttonFont = Font.loadFont(AppProps.fontAPath, 15);

        HBox b = new HBox(10);

        // Add tutorial button
        btnTutorial = new Button("Tutorial");
        btnTutorial.setPrefSize(200, 50);
        btnTutorial.setFont(buttonFont);
        btnTutorial.setAlignment(Pos.CENTER);
        btnTutorial.setOnAction(e -> {
        	showTutorial();
        });

        b.getChildren().add(btnTutorial);

        // Add play button
        btnPlay = new Button("Play");
        btnPlay.setPrefSize(200, 50);
        btnPlay.setFont(buttonFont);
        GridPane.setHalignment(btnPlay, HPos.CENTER);
        b.getChildren().add(btnPlay);
        
        // Start game when play pressed
        btnPlay.setOnAction(e -> {
        	GameManager.getRoot().getChildren().remove(titleScreenPane);
        	GameManager.play();
        });

        b.setAlignment(Pos.CENTER);
        GridPane.setHalignment(b, HPos.CENTER);
        titleScreenPane.add(b, 0, 1);

        // Set background color
		titleScreenPane.setStyle(String.format("-fx-background-color: rgb(%d, %d, %d)", 10, 10, 10));

        // Add titlepane to root
        GameManager.getRoot().getChildren().add(titleScreenPane);
    }
    
    // Method to display tutorial using alerts
    public static void showTutorial()
    {
    	Image imgTheBullet = new Image("file:assets/dialogue/thebullet.png");

    	// Create alerts in order to display to player
    	ImageView v = new ImageView(imgTheBullet);
    	v.setFitWidth(70);
    	v.setPreserveRatio(true);

    	Alert alert = new Alert(AlertType.INFORMATION);
    	alert.setHeaderText(null);
    	alert.setTitle("The Bullet");
    	alert.setGraphic(v);

        // Show dialogue pieces
        alert.setContentText("Hello there! My name is the bullet and I'm here to help!");
    	alert.showAndWait();

        alert.setContentText("This game uses a keyboard and mouse control scheme. you can use WASD to move and the mouse to aim.");
    	alert.showAndWait();

        alert.setContentText("You can shoot at enemies, crates and barrels by aiming and clicking or holding on the left mouse button!");
    	alert.showAndWait();

        alert.setContentText("If you're ever in a tight spot, right click the mouse to perform a dodge - It will make you temporarily invulnerable.");
    	alert.showAndWait();

        alert.setContentText("Your job is to go as far down into the dungeon as possible. Avoid dying from enemy attacks and explosions while traversing the labyrinth, and you will be awarded with points! The more destruction, the more points!");
    	alert.showAndWait();

        alert.setContentText("Destroying crates and barrels will drop valuable items, such as health, ammo, or even more guns (other than increasing your score)");
    	alert.showAndWait();

        alert.setContentText("There will be a radar at the top right of your view to help you find out where the end room is. However, you may have to do some searching yourself before it pops up.");
    	alert.showAndWait();

        alert.setContentText("Be wary soldier, ammo is limited. I have equipped you with a basic weapon with unlimited ammo, but it definitely isn't the most optimal weapon.");
    	alert.showAndWait();

        alert.setContentText("Good luck out there, generic red square!");
    	alert.showAndWait();
    }
}
