package application;

import application.World.Map;
import application.World.Tilemap;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameManager
{
	// Javafx
	private static Stage primaryStage;
	private static Scene scene;
	private static Pane root;

	// Game
	private static long currentTime, deltaTime;
	private static long frame;
	private static Map map;

	// Render at proper resolution
	private static WritableImage renderImg;
	private static ImageView gameView;
    private static StringBuilder debugStr = new StringBuilder();
    private static Label lblDebug;
	private static Canvas debugCanvas;

	// Timers
	private static AnimationTimer loadTimer = new AnimationTimer()
	{
		@Override
		public void handle(long now)
		{
			// deltatime in nanoseconds
			deltaTime = now - currentTime;
			currentTime = now;
			loadUpdate(deltaTime);
		}
	};
	private static AnimationTimer gameTimer = new AnimationTimer()
	{
		@Override
		public void handle(long now)
		{
			// deltatime in nanoseconds
			deltaTime = now - currentTime;
			currentTime = now;
			update(deltaTime);
		}
	};

	public static void start(Stage primaryStage)
	{
		try
		{
			// Reset frame
			frame = 0;

			// Set stage
			GameManager.primaryStage = primaryStage;

			// Create root and scene
			root = new Pane();
			scene = new Scene(root, AppProps.REAL_WIDTH, AppProps.REAL_HEIGHT);

			// Start loading
			loadTimer.start();

			Platform.runLater(new Runnable()
			{
				public void run()
				{
					System.out.println("Loading map");

					// Initialize map
					Tilemap tilemap = new Tilemap("1", 16);
					map = new Map(100, 75, 25, tilemap);
					
					System.out.println("Loading complete!");
					
					// Start main game timer
					loadTimer.stop();
					gameTimer.start();
				}

			});

			// Default camera position
			Camera.setPos(new Vector2(0, 0));
			
			// Set up game imageview
			gameView = new ImageView();
			gameView.setFitWidth(AppProps.REAL_WIDTH);
			gameView.setFitHeight(AppProps.REAL_HEIGHT);
			gameView.setSmooth(false);
			root.getChildren().add(gameView);

			debugCanvas = new Canvas();
			debugCanvas.setWidth(AppProps.REAL_WIDTH);
			debugCanvas.setHeight(AppProps.REAL_HEIGHT);
			root.getChildren().add(debugCanvas);

			// Initialize input
			InputManager.Init(scene);

			// Add debug label with background
			Label lblDebugTitle = new Label();
			lblDebugTitle.setFont(Font.loadFont("file:Inter-ExtraBold.ttf", 20));
			lblDebugTitle.setText("Debug");

			lblDebug = new Label();
			lblDebug.setFont(Font.loadFont("file:SpaceGrotesk-SemiBold.ttf", 12));

			VBox box = new VBox();
			box.setSpacing(2);
			box.setLayoutX(20);
			box.setLayoutY(20);
			box.setStyle("-fx-background-color: white");
			box.setPadding(new Insets(8));
			box.setPrefWidth(100);
			box.getChildren().addAll(lblDebugTitle, lblDebug);

			root.getChildren().add(box);

			// Show primary stage
			primaryStage.setTitle("Test");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void addDebugText(String label, Object value)
	{
		debugStr.append(label + ": " + value.toString() + '\n');
	}

	// Update during loading screens
	public static void loadUpdate(long deltaTime)
	{
		renderImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
	}
	public static void update(long deltaTime)
	{
		// Clear by resetting
		debugStr = new StringBuilder();
		frame++;

		// frame by frame debug
		/*
		if ((frame % 5) > 0)
		{
			return;
		}
		*/

		GraphicsContext ct = debugCanvas.getGraphicsContext2D();

		Camera.freeCam();
		renderImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
		map.draw(Camera.getPos(), renderImg);
		gameView.setImage(renderImg);
		//map.draw(Camera.getPos(), renderImg);

		// Update
		addDebugText("ms ", String.format("%.2f", deltaTime / 1000000.0));
		addDebugText("fps ", String.format("%.2f",  1 / (deltaTime / 1000000000.0)));

		// Set debug text
		lblDebug.setText(debugStr.toString());
	}
	
	// Accessor methods for various game parts
	public static Pane getRoot()
	{
		return root;
	}
	public static ImageView getGameView()
	{
		return gameView;
	}
	public static Canvas getCanvas()
	{
		return debugCanvas;
	}
	public static Map getMap()
	{
		return map;
	}
}