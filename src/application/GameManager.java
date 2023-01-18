package application;

import java.util.ArrayList;

import application.World.Map;
import application.World.Tilemap;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
	private static Player player;

	// Game objects
	private static ArrayList<Projectile> projectiles;

	// Render at proper resolution
	private static WritableImage renderImg;
	private static ImageView gameView;
    private static StringBuilder debugStr = new StringBuilder();
    private static Label lblDebug;

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
			draw(deltaTime);
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


			// Start loading on another thread
			loadTimer.start();
			Platform.runLater(new Runnable()
			{
				public void run()
				{
					
					System.out.println("Loading map");

					// Load tilemap
					Tilemap tilemap = new Tilemap("1", 16);

					// Set background color
					root.setStyle(String.format("-fx-background-color: rgb(%d, %d, %d)", 20, 20, 18));

					// Create map and get start room
					map = new Map(80, 60, 25, tilemap);
					Rectangle startRoom = map.getStartRoom();

					// Create projectile arraylist
					projectiles = new ArrayList<Projectile>();

					System.out.println("Loading player");

					// Initialize player, set position to center of spawnroom
					player = new Player();
					player.setPosition((startRoom.getX() + startRoom.getWidth() / 2) * tilemap.getTileSize(), (startRoom.getY() + startRoom.getHeight() / 2) * tilemap.getTileSize());

					// Initialize camera, set center to center of spawn room
					Camera.init();
					Camera.setPos(player.getPosition().x - AppProps.BASE_WIDTH / 2, player.getPosition().y - AppProps.BASE_HEIGHT / 2);
					
					System.out.println("Loading complete!");
					
					// Start main game timer
					loadTimer.stop();
					gameTimer.start();
				}

			});

			// Set up game imageview
			gameView = new ImageView();
			gameView.setFitWidth(AppProps.REAL_WIDTH);
			gameView.setFitHeight(AppProps.REAL_HEIGHT);
			gameView.setSmooth(false);
			root.getChildren().add(gameView);

			// Initialize input
			InputManager.Init(scene);
			
			// Initialize UI
			UI.Init(root);

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

			//root.getChildren().add(box);

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

		// Update every bullet
		for (int projectile = 0; projectile < projectiles.size(); projectile++)
		{
			projectiles.get(projectile).update();
		}

		// Update player
		player.update();

		// Update
		addDebugText("ms ", String.format("%.2f", deltaTime / 1000000.0));
		addDebugText("fps ", String.format("%.2f",  1 / (deltaTime / 1000000000.0)));
		addDebugText("mod ", String.format("%.2f", (100.0/60) / (deltaTime / 10000000.0)));

		// Set debug text
		lblDebug.setText(debugStr.toString());
	}
	public static void draw(long deltaTime)
	{
		renderImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
		map.draw(Camera.getPos(), renderImg);
		player.draw(renderImg);
		gameView.setImage(renderImg);
	}
	
	// Game feature methods
	public static void spawnProjectile(Projectile projectile)
	{
		projectiles.add(projectile);
	}
	public static boolean playerCollision(Projectile projectile)
	{
		return projectile.getMask().intersects(player.getMask());
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
	public static Map getMap()
	{
		return map;
	}
	public static Player getPlayer()
	{
		return player;
	}
}