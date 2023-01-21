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
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TitleScreen
{
    private static Button btnTutorial, btnPlay, btnSettings;
    private static GridPane titleScreenPane;
    private static Label lblTitle;

    private static String currentDialogStr;

    // Initialize title screen
    public static void init()
    {
        // Crate title screen root gridpane
        titleScreenPane = new GridPane();
        titleScreenPane.setGridLinesVisible(true);
        titleScreenPane.setMinWidth(AppProps.REAL_WIDTH);
        titleScreenPane.setMinHeight(AppProps.REAL_HEIGHT);
        titleScreenPane.setAlignment(Pos.CENTER);

        // Create label and buttons
        lblTitle = new Label("ROUGE");
        lblTitle.setFont(Font.loadFont(AppProps.fontAPath, 30 * AppProps.SCALE));
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setAlignment(Pos.CENTER);
        lblTitle.setPrefWidth(AppProps.REAL_WIDTH);

        // Add label to pane
        titleScreenPane.getRowConstraints().add(new RowConstraints(500));
        titleScreenPane.add(lblTitle, 0, 0);
        GridPane.setColumnSpan(lblTitle, 3);

        Font buttonFont = Font.loadFont(AppProps.fontAPath, 15);

        // Add tutorial button
        titleScreenPane.getColumnConstraints().add(new ColumnConstraints(AppProps.REAL_WIDTH / 3));
        btnTutorial = new Button("Tutorial");
        btnTutorial.setPrefSize(200, 50);
        btnTutorial.setFont(buttonFont);

        btnTutorial.setOnAction(e -> {
        	showTutorial();
        });

        GridPane.setHalignment(btnTutorial, HPos.RIGHT);
        titleScreenPane.add(btnTutorial, 0, 1);

        // Add play button
        titleScreenPane.getColumnConstraints().add(new ColumnConstraints(AppProps.REAL_WIDTH / 3));
        btnPlay = new Button("Play");
        btnPlay.setPrefSize(200, 50);
        btnPlay.setFont(buttonFont);
        GridPane.setHalignment(btnPlay, HPos.CENTER);
        titleScreenPane.add(btnPlay, 1, 1);
        
        // Start game when play pressed
        btnPlay.setOnAction(e -> {
        	GameManager.getRoot().getChildren().remove(titleScreenPane);
        	GameManager.play();
        });
        
        titleScreenPane.getColumnConstraints().add(new ColumnConstraints(AppProps.REAL_WIDTH / 3));
        btnSettings = new Button("Settings");
        btnSettings.setPrefSize(200, 50);
        btnSettings.setFont(buttonFont);
        GridPane.setHalignment(btnSettings, HPos.LEFT);
        titleScreenPane.add(btnSettings, 2, 1);


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
    	Alert alert = new Alert(AlertType.INFORMATION);
    	ImageView v = new ImageView(imgTheBullet);
    	v.setFitWidth(70);
    	v.setPreserveRatio(true);
    	alert.setHeaderText(null);
    	alert.setGraphic(v);
    	currentDialogStr = "Hello there! jiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii!!!";
    	alert.setTitle("The Bullet");
    	alert.showAndWait();
    
		// Timers
		AnimationTimer typingTimer = new AnimationTimer()
		{
			@Override
			public void handle(long now)
			{
				// Next index is current length (because length - 1 relation)
				int nextIndex = alert.getContentText().length();
				
				if (nextIndex == currentDialogStr.length())
				{
					this.stop();
					return;
				}

				alert.setContentText(alert.getContentText() + currentDialogStr.charAt(nextIndex));
			}
		};
		typingTimer.start();

		alert.setContentText("");
    	currentDialogStr = "two";
    	typingTimer.start();
    }
}
