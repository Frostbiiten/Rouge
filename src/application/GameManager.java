package application;

import java.util.ArrayList;

import application.World.Map;
import application.World.Tilemap;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
	private static Map map;
	private static Player player;
	private static int currentRoomRound;
	private static int[] roomSpawnPatterns;

	// Game objects
	private static ArrayList<Projectile> projectiles;
	private static ArrayList<Explosion> explosions;
	private static ArrayList<Pickup> pickups;
	private static ArrayList<Enemy> enemies;
	private static ArrayList<Prop> props;
	
	// Progression
	private static AnimatedSprite portal;
	private static int currentLevel;

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
		// Set stage
		GameManager.primaryStage = primaryStage;
		primaryStage.setResizable(false);

		// Create root and scene
		root = new Pane();
		scene = new Scene(root, AppProps.REAL_WIDTH, AppProps.REAL_HEIGHT);

		// Init title screen
		TitleScreen.init();

		// Default level
		currentLevel = 0;

		// Show primary stage
		primaryStage.setTitle("ROUGE");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void play()
	{
		try
		{
			// Increment current level
			currentLevel++;

			// Stop previous level timer and clear floor root children
			gameTimer.stop();
			root.getChildren().clear();

			// Disable cursor
			scene.setCursor(Cursor.NONE);

			// Set up game foreground and background imageviews
			bgView = new ImageView();
			bgView.setFitWidth(AppProps.REAL_WIDTH);
			bgView.setFitHeight(AppProps.REAL_HEIGHT);

			fgView = new ImageView();
			fgView.setFitWidth(AppProps.REAL_WIDTH);
			fgView.setFitHeight(AppProps.REAL_HEIGHT);

			root.getChildren().addAll(bgView, fgView);
			bgImgDepthIndex = root.getChildren().size() - 2;

			// Create projectile, explosion, enemies, and props arraylist
			projectiles = new ArrayList<Projectile>();
			explosions = new ArrayList<Explosion>();
			pickups = new ArrayList<Pickup>();
			enemies = new ArrayList<Enemy>();
			props = new ArrayList<Prop>();

			// Initialize input
			InputManager.init(scene);

			// Init camera
			Camera.init();

			// Load tilemap
			tilemap = new Tilemap("1", 16);

			// Set background color
			root.setStyle(String.format("-fx-background-color: rgb(%d, %d, %d)", 20, 20, 18));

			// Create map, get start room and get props arraylist
			map = new Map(100, 80, 5, tilemap);
			Rectangle startRoom = map.getStartTileRoom();
			props = map.getProps();

			// Create portal for end room
			portal = new AnimatedSprite(new Image("file:assets/portal.png"), 16, 91, 1, true);
			portal.getNode().setFitWidth(200);
			portal.getNode().setPreserveRatio(true);
			addAnimatedSprite(portal);
			portal.play();

			// Initialize player, set position to center of spawnroom
			player = new Player();
			player.setPosition((startRoom.getX() + startRoom.getWidth() / 2) * tilemap.getTileSize(), (startRoom.getY() + startRoom.getHeight() / 2) * tilemap.getTileSize());
			root.getChildren().add(bgImgDepthIndex + 1, player.getGunNode());
			Camera.setPos(player.getPosition().x - AppProps.BASE_WIDTH / 2, player.getPosition().y - AppProps.BASE_HEIGHT / 2);

			// Initialize UI
			UI.init(root);

			// Initialize VFX
			VFX.init();
			
			// Initialize Audio
			AudioManager.init();
			
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

			// Start main game
			gameTimer.start();

			// Show tooltip of current level
			UI.setLabelInfo("Level " + currentLevel, 240);
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
	private static void loadUpdate(long deltaTime)
	{
		fgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
		bgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
	}
	private static void update(long deltaTime)
	{
		// Clear by resetting
		debugStr = new StringBuilder();

		// Update every enemy
		for (int enemy = 0; enemy < enemies.size(); enemy++)
		{
			Enemy currentEnemy = enemies.get(enemy);
			currentEnemy.update();
		}

		// Continue when necessary
		if (enemies.size() == 0 && player.getActiveRoom() != null)
		{
			nextRound();
		}

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

			if (!currentProjectile.getPlayerOwned())
			{
				if (playerCollision(currentProjectile))
				{
					player.damage();
					currentProjectile.collide();
					root.getChildren().remove(currentProjectile.getNode());
					projectiles.remove(currentProjectile);
				}
			}

			if (currentProjectile.getPlayerOwned())
			{
				for (int i = 0; i < enemies.size(); i++)
				{
					if (enemies.get(i).getMask().intersects(currentProjectile.getMask()))
					{
						enemies.get(i).damage();
						currentProjectile.collide();
						root.getChildren().remove(currentProjectile.getNode());
						projectiles.remove(currentProjectile);
					}
				}
			}
			
			for (int i = 0; i < props.size(); i++)
			{
				if (props.get(i).getMask().intersects(currentProjectile.getMask()))
				{
					currentProjectile.collide();
					root.getChildren().remove(currentProjectile.getNode());
					projectiles.remove(currentProjectile);
					props.get(i).hit(new Vector2(currentProjectile.getXVel(), currentProjectile.getYVel()));
				}
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

		// Update every prop
		for (int prop = 0; prop < props.size(); prop++)
		{
			Prop currentProp = props.get(prop);
			currentProp.update();
		}

		// Check if player is colliding with any pickup
		for (int pickup = 0; pickup < pickups.size(); pickup++)
		{
			Pickup currentPickup = pickups.get(pickup);
			if (currentPickup.getMask().intersects(player.getMask()))
			{
				currentPickup.pickup();
				root.getChildren().remove(currentPickup.getNode());
				pickups.remove(currentPickup);
			}
		}

		// Update vfx
		VFX.update();

		// Update
		addDebugText("ms ", String.format("%.2f", deltaTime / 1000000.0));
		addDebugText("fps ", String.format("%.2f",  1 / (deltaTime / 1000000000.0)));
		addDebugText("mod ", String.format("%.2f", (100.0/60) / (deltaTime / 10000000.0)));

		// Set debug text
		lblDebug.setText(debugStr.toString());
	}
	private static void draw(long deltaTime)
	{
		// Clear render images. (when tested, this was faster than refilling them)
		fgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);
		bgImg = new WritableImage(AppProps.BASE_WIDTH, AppProps.BASE_HEIGHT);

		// Update prop positions
		for (int prop = 0; prop < props.size(); prop++)
		{
			Prop currentProp = props.get(prop);
			currentProp.updateScreenPos();
		}

		// Update pickup positions
		for (int pickup = 0; pickup < pickups.size(); pickup++)
		{
			Pickup currentPickup = pickups.get(pickup);
			currentPickup.updateScreenPos();
		}

		// Draw background
		map.draw(Camera.getPos(), bgImg, true);
		
		// Draw player
		player.draw(bgImg);

		// Draw foreground
		map.draw(Camera.getPos(), fgImg, false);
		
		// Set background and foreground images
		fgView.setImage(fgImg);
		bgView.setImage(bgImg);

		// Set portal position on screen to center of end room
		Rectangle endRoom = map.getEndRoom();
		ImageView portalNode = portal.getNode();
		portalNode.setX(endRoom.getX() + endRoom.getWidth() / 2);
		portalNode.setY(endRoom.getY() + endRoom.getHeight() / 2);
		portalNode.setX((portalNode.getX() - Camera.getX()) * AppProps.SCALE - 100);
		portalNode.setY((portalNode.getY() - Camera.getY()) * AppProps.SCALE - 100);

		// Update ui element
		UI.updateBorders();
		UI.updateMinimap();
		UI.updateInfoLabel();
	}
	
	// Game feature methods
	public static void startRoom(Rectangle room)
	{
		roomSpawnPatterns = map.getRoomSpawnPattern(room);
		
		// Make sure room has spawn patterns
		if (roomSpawnPatterns != null)
		{
			// -1 allows it to increment to 0
			currentRoomRound = -1;
			nextRound();
		}
	}
	public static void nextRound()
	{
		currentRoomRound++;
		if (roomSpawnPatterns.length == currentRoomRound)
		{
			player.clearRoom(player.getActiveRoom());
			currentRoomRound--;
			return;
		}

		int enemyCount = roomSpawnPatterns[currentRoomRound];
		Rectangle room = player.getActiveRoom();

		for (int i = 0; i < enemyCount; i++)
		{
			Vector2 spawnPos = new Vector2();
			for (int attempts = 0; attempts < 10; attempts++)
			{
				spawnPos.x = room.getX() + 50 + Math.random() * (room.getWidth() - 100);
				spawnPos.y = room.getY() + 50 + Math.random() * (room.getHeight() - 100);

				for (int otherEnemy = 0; otherEnemy < enemies.size(); otherEnemy++)
				{
					if (enemies.get(otherEnemy).getMask().contains(spawnPos.x, spawnPos.y))
					{
						// Randomize again
						continue;
					}
				}
			}
			Enemy e = new NormalEnemy("0", new Vector2(spawnPos.x, spawnPos.y), room);
			addEnemy(e);
		}
	}

	// Methods for getting and setting level integer
	public static int getLevel()
	{
		return currentLevel;
	}
	public static void setLevel(int level)
	{
		currentLevel = level;
	}

	// Method to spawn projectile and add image to pane
	public static void spawnProjectile(Projectile projectile)
	{
		// Add projectile to arraylist
		projectiles.add(projectile);
		
		// Add projectile to index just above bg to render above background but under foreground
		root.getChildren().add(bgImgDepthIndex + 1,  projectile.getNode());
	}

	// Method to add animated sprite to the scene
	public static void addAnimatedSprite(AnimatedSprite sprite)
	{
		// Add sprite above bg, below fg
		root.getChildren().add(bgImgDepthIndex + 1,  sprite.getNode());
	}

	// Method to add explosion to the scene
	public static void addExplosion(Explosion explosion)
	{
		// Add explosion to arraylist
		explosions.add(explosion);
		root.getChildren().add(explosion.getNode());
	}

	// Method to add enemy to the scene
	public static void addEnemy(Enemy enemy)
	{
		// Add enemy to arraylist
		enemies.add(enemy);
	}

	// Method to remove enemy from the scene
	public static void removeEnemy(Enemy enemy)
	{
		// Remove enemy from arraylist
		enemies.remove(enemy);
	}

	// Method to add prop
	public static void addProp(Prop prop)
	{
		// Add to arraylist and pane
		props.add(prop);
		GameManager.getRoot().getChildren().add(prop.getNode());
	}
	
	// Method to remove prop
	public static void removeProp(Prop prop)
	{
		// Remove from arraylist and pane
		GameManager.getRoot().getChildren().remove(prop.getNode());
		props.remove(prop);
	}

	// Method to add pickup
	public static void addPickup(Pickup pickup)
	{
		pickups.add(pickup);
		GameManager.getRoot().getChildren().add(bgImgDepthIndex + 1, pickup.getNode());
	}

	// Method to get props list
	public static ArrayList<Prop> getProps()
	{
		return props;
	}

	public static boolean playerCollision(Projectile projectile)
	{
		return projectile.getMask().intersects(player.getMask());
	}

	public static boolean playerCollision(Explosion explosion)
	{
		return explosion.getMask().intersects(player.getMask());
	}

	// Various accessor methods for various game parts
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
	public static ArrayList<Enemy> getEnemies()
	{
		return enemies;
	}
}