package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Optional;

import application.World.Map;
import application.World.Tilemap;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
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
	private static int points;

	// Rendering
	private static WritableImage bgImg;
	private static int bgImgDepthIndex;
	private static WritableImage fgImg;
	private static ImageView bgView;
	private static ImageView fgView;
	private static Tilemap tilemap;

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

	// Start is called at the beginning of the program
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

		// Default level and score
		currentLevel = 0;
		points = 0;

		// Show primary stage
		primaryStage.setTitle("ROUGE");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// Pay is called at the beginning of a run
	public static void play()
	{
		try
		{
			// Check if player is progressing and not just starting the game
			boolean progressing = currentLevel != 0;
			ArrayList<Gun> oldGuns = null;
			int oldHp = 0, oldWeaponIndex = 0;

			if (progressing)
			{
				oldGuns = new ArrayList<Gun>(player.getWeapons());
				oldHp = player.getHp();
				oldWeaponIndex = player.getWeaponIndex();
				
				// 100 * currentLevel points are added to incentivise progression
				points += currentLevel * 100;
			}

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
			map = new Map(100, 80, 25, tilemap);
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

			// Carry over guns and hp from previous level
			if (progressing)
			{
				// Replace player guns with stored guns
				player.setWeapons(oldGuns);
				player.setHp(oldHp);
				player.setWeaponIndex(oldWeaponIndex);
			}

			// Initialize VFX
			VFX.init();
			
			// Initialize Audio
			AudioManager.init();

			// Start main game
			gameTimer.start();

			// Show label on ui of current level
			UI.setLabelInfo("Level " + currentLevel, 240);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Main update-draw methods
	private static void update(long deltaTime)
	{
		// Update every enemy if player isn't dead
		if (!player.getDead())
		{
			for (int enemy = 0; enemy < enemies.size(); enemy++)
			{
				Enemy currentEnemy = enemies.get(enemy);
				currentEnemy.update();
			}
		}

		// Continue to spawn next round of enemies when necessary
		if (enemies.size() == 0 && player.getActiveRoom() != null)
		{
			nextRound();
		}

		// Update player
		player.update();

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

			// If the projectile is not player owned, collision check against the player
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

			// If the projectile is player owned, collision check against the enemies
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
			
			// Check if each prop is intersecting with the projectile for destruction
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
				spawnPos.x = room.getX() + 20 + Math.random() * (room.getWidth() - 80);
				spawnPos.y = room.getY() + 20 + Math.random() * (room.getHeight() - 80);

				for (int otherEnemy = 0; otherEnemy < enemies.size(); otherEnemy++)
				{
					Rectangle other = enemies.get(otherEnemy).getMask();
					Rectangle boundCheckRect = new Rectangle(other.getX() - 20, other.getY() - 20, other.getWidth() + 40, other.getWidth() + 40);
					if (boundCheckRect.contains(spawnPos.x, spawnPos.y))
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
	public static void gameOver()
	{
		// Remove all enemies
		for (int i = 0; i < enemies.size(); i++)
		{
			removeEnemy(enemies.get(i));
		}

		// Stop keeping track of all game entities
		enemies.clear();
		projectiles.clear();
		props.clear();
		explosions.clear();
		pickups.clear();

		Platform.runLater(
			new Runnable()
			{
				public void run()
				{
					// File manipulation
					try 
					{
						// Prompt user for name
						Alert namePromptAlert = new Alert(AlertType.INFORMATION);
						namePromptAlert.setContentText("Would you like to save your score?");
						namePromptAlert.setTitle("Save score?");
						namePromptAlert.setHeaderText(null);
						namePromptAlert.getButtonTypes().clear();
						namePromptAlert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
						Optional<ButtonType> alertResponse = namePromptAlert.showAndWait();

						if (alertResponse.isPresent() && alertResponse.get() == ButtonType.YES)
						{
							// Prompt user for their name
							TextInputDialog nameDialog = new TextInputDialog();
							nameDialog.setTitle("Save score");
							nameDialog.setHeaderText(null);
							nameDialog.setContentText("Enter your name! :");

							Optional<String> response = nameDialog.showAndWait();
							if (response.isPresent())
							{
								// Get name
								String name = response.get().trim();
								if (name.length() == 0 || name.equals(" "))
								{
									return;
								}

								// Reference File that keeps track of various scores
								File scoresFile = new File("scores.txt");

								if (scoresFile.exists())
								{
									FileReader fr = new FileReader(scoresFile);
									BufferedReader br = new BufferedReader(fr);

									// Read all scores and names
									ArrayList<Integer> scores = new ArrayList<Integer>();
									ArrayList<String> names = new ArrayList<String>();

									String currentLine;
									while ((currentLine = br.readLine()) != null)
									{
										String[] parts = currentLine.split(" ");

										// after 0th index is name because it may include spaces
										String fullName = "";
										for (int segment = 1; segment < parts.length; segment++)
										{
											fullName += parts[segment] + " ";
										}

										// First part is int score, the rest is the name string
										scores.add(Integer.parseInt(parts[0]));
										names.add(fullName);
									}

									// Add current to list
									scores.add(points);
									names.add(name);

									// Sort using insertion sort
									for (int end = 1; end < scores.size(); end++)
									{
										int item = scores.get(end);
										String itemStr = names.get(end);
										int index = end;

										while (index > 0 && item < scores.get(index - 1))
										{
											// Manipulate both arraylists based on one to sort the pairs
											scores.set(index, scores.get(index - 1));
											names.set(index, names.get(index - 1));

											index--;
										}

										scores.set(index, item);
										names.set(index, itemStr);
									}

									int newIndex = -1;

									// Get index (score/name might be the same, so make sure)
									for (int i = 0; i < scores.size(); i++)
									{
										if (scores.get(i) == points && names.get(i).equals(name))
										{
											newIndex = i;
										}
									}

									// Show score
									Alert scoreAlert = new Alert(AlertType.INFORMATION);
									scoreAlert.setContentText("Your score was in the top " + (scores.size() - newIndex) + " of recorded scores!");
									scoreAlert.setHeaderText(null);
									scoreAlert.setTitle("Score");
									scoreAlert.showAndWait();

									// Write back to file (append new score)
									FileWriter fw = new FileWriter(scoresFile, true);
									BufferedWriter bw = new BufferedWriter(fw);
									bw.write(Integer.toString(points) + " " + name + "\n");
									bw.close();
								}
								else
								{
									// Just write to file, as there are no other scores to compare to
									FileWriter fw = new FileWriter(scoresFile);
									BufferedWriter bw = new BufferedWriter(fw);

									// Write [score] [name]
									bw.write(Integer.toString(points) + " " + name);
									bw.newLine();
									bw.flush();

									// Close writers
									bw.close();
									fw.close();
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		);
	}
	
	// Methods for adding and getting points
	public static void addPoints(int num)
	{
		points += num;
	}
	public static int getPoints()
	{
		return points;
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

	// Methods to check if player collided with a projectile or explosion
	public static boolean playerCollision(Projectile projectile)
	{
		return projectile.getMask().intersects(player.getMask());
	}
	public static boolean playerCollision(Explosion explosion)
	{
		return explosion.getMask().intersects(player.getMask());
	}

	// Various accessor methods for core game parts
	public static Stage getStage()
	{
		return primaryStage;
	}
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