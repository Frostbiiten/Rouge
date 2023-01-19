package application;

import java.util.ArrayList;

import application.Vector2.Direction;
import application.World.Map;
import application.World.Tilemap;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
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
	private static ArrayList<Explosion> explosions;

	// Rendering
	private static WritableImage bgImg;
	private static int bgImgDepthIndex;
	private static WritableImage fgImg;
	private static ImageView bgView;
	private static ImageView fgView;
	private static Tilemap tilemap;

	// Debug
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
			primaryStage.setResizable(false);

			// Create root and scene
			root = new Pane();
			scene = new Scene(root, AppProps.REAL_WIDTH, AppProps.REAL_HEIGHT);
			scene.setCursor(Cursor.NONE);

			// Start loading on another thread
			loadTimer.start();
			Platform.runLater(new Runnable()
			{
				public void run()
				{
					System.out.println("Loading map");

					// Load tilemap
					tilemap = new Tilemap("1", 16);

					// Set background color
					root.setStyle(String.format("-fx-background-color: rgb(%d, %d, %d)", 20, 20, 18));

					// Create map and get start room
					map = new Map(100, 80, 25, tilemap);
					Rectangle startRoom = map.getStartRoom();

					System.out.println("Loading imageviews");

					// Set up game imageview
					bgView = new ImageView();
					bgView.setFitWidth(AppProps.REAL_WIDTH);
					bgView.setFitHeight(AppProps.REAL_HEIGHT);

					fgView = new ImageView();
					fgView.setFitWidth(AppProps.REAL_WIDTH);
					fgView.setFitHeight(AppProps.REAL_HEIGHT);

					root.getChildren().addAll(bgView, fgView);
					bgImgDepthIndex = root.getChildren().size() - 2;

					System.out.println("Loading projectiles & explosions");

					// Create projectile arraylist
					projectiles = new ArrayList<Projectile>();
					explosions = new ArrayList<Explosion>();
					
					System.out.println("Loading input");

					// Initialize input
					InputManager.init(scene);

					System.out.println("Loading player");

					// Initialize player, set position to center of spawnroom
					player = new Player();
					player.setPosition((startRoom.getX() + startRoom.getWidth() / 2) * tilemap.getTileSize(), (startRoom.getY() + startRoom.getHeight() / 2) * tilemap.getTileSize());
					root.getChildren().add(bgImgDepthIndex + 1, player.getGunNode());

					// Initialize camera, set center to center of spawn room
					Camera.init();
					Camera.setPos(player.getPosition().x - AppProps.BASE_WIDTH / 2, player.getPosition().y - AppProps.BASE_HEIGHT / 2);
					// Initialize UI
					UI.init(root);

					// Initialize VFX
					VFX.init();
					
					System.out.println("Loading complete!");
					
					// Start main game timer
					loadTimer.stop();
					gameTimer.start();
				}

			});


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
		fgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
		bgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
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

		// Update every projectile
		for (int projectile = 0; projectile < projectiles.size(); projectile++)
		{
			Projectile currentProjectile = projectiles.get(projectile);
			currentProjectile.update();

			// Get the position on the map in terms of tiles
			int tileX = (int)(currentProjectile.getXPos() / tilemap.getTileSize());
			int tileY = (int)(currentProjectile.getYPos() / tilemap.getTileSize());

			// If the projectile is outide the map or hit a wall, destroy it
			if (tileX < 0 || tileX >= map.getWidthInTiles() || tileY < 0 || tileY >= map.getHeightInTiles() || !map.getFloorTile(tileX, tileY))
			{
				currentProjectile.collide();
				root.getChildren().remove(currentProjectile.getNode());
				projectiles.remove(currentProjectile);
			}
		}

		// Update every explosion
		for (int explosion = 0; explosion < explosions.size(); explosion++)
		{
			Explosion currentExplosion = explosions.get(explosion);
			currentExplosion.update();

			// When finished playing, remove
			if (!currentExplosion.getSprite().isPlaying())
			{
				root.getChildren().remove(currentExplosion.getNode());
				explosions.remove(currentExplosion);
			}
		}

		// Update player
		player.update();

		// Update vfx
		VFX.update();

		// Update
		addDebugText("ms ", String.format("%.2f", deltaTime / 1000000.0));
		addDebugText("fps ", String.format("%.2f",  1 / (deltaTime / 1000000000.0)));
		addDebugText("mod ", String.format("%.2f", (100.0/60) / (deltaTime / 10000000.0)));

		// Set debug text
		lblDebug.setText(debugStr.toString());
	}

	public static void draw(long deltaTime)
	{
		// Clear render images. (when tested, this was faster than refilling them)
		fgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
		bgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);

		// Draw background
		map.draw(Camera.getPos(), bgImg, true);
		
		// Draw player
		player.draw(bgImg);

		// Draw foreground
		map.draw(Camera.getPos(), fgImg, false);
		
		// Set background and foreground images
		fgView.setImage(fgImg);
		bgView.setImage(bgImg);
	}
	
	// Game feature methods

	// Method to spawn projectile and add image to pane
	public static void spawnProjectile(Projectile projectile)
	{
		// Add projectile to arraylist
		projectiles.add(projectile);
		
		// Add projectile to index just above bg to render above background but under foreground
		root.getChildren().add(bgImgDepthIndex + 1,  projectile.getNode());
	}

	// Method to add animated sprite to the pane
	public static void addAnimatedSprite(AnimatedSprite sprite)
	{
		// Add sprite above bg, below fg
		root.getChildren().add(bgImgDepthIndex + 1,  sprite.getNode());
	}

	// Method to add explosion to the pane
	public static void addExplosion(Explosion explosion)
	{
		// Add explosion to arraylist
		explosions.add(explosion);

		// Add sprite above bg, below fg
		root.getChildren().add(bgImgDepthIndex + 1,  explosion.getNode());
	}

	public static boolean playerCollision(Projectile projectile)
	{
		return projectile.getMask().intersects(player.getMask());
	}

	public static boolean playerCollision(Explosion explosion)
	{
		return explosion.getMask().intersects(player.getMask());
	}

	// Accessor methods for various game parts
	public static Pane getRoot()
	{
		return root;
	}
	public static ImageView getGameView()
	{
		return bgView;
	}
	public static Map getMap()
	{
		return map;
	}
	public static Player getPlayer()
	{
		return player;
	}
	public static int getBgDepth()
	{
		return bgImgDepthIndex;
	}
}