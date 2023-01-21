package application;
import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class UI
{
	public static BorderPane uiPane;
	
	// Health
	private final static int heartImageSize = 20;
	private static Image heartFull, heartEmpty;
	private static ArrayList<ImageView> hearts;
	private static HBox heartsBox;
	private static int previousHp;
	private static Timeline hpFlickerTimeline;
	private static int hpFlickerCounter;

	// Weapon
	private static Label lblAmmo, lblMags, lblWeaponInfo;
	private static ImageView weaponImgView;
	
	// Crosshair
	private static ImageView crosshairView;

	// Minimap:
	private static ArrayList<Rectangle> minimapRoomReferences;
	private static ArrayList<Rectangle> minimapVisualRooms;
	private static final double minimapScale = 20;
	private static ImageView iconView;
	private static Pane minimap;
	private static Label lblStartRoom, lblEndRoom;

	// Label for extra info
	public static Label lblInfo;
	private static double infoTimer;

	// Room border gradients
	private static ImageView rightGradient, leftGradient, topGradient, bottomGradient;

	public static void init(Pane root)
	{
		// Create main ui borderpane
		uiPane = new BorderPane();
		uiPane.setPadding(new Insets(10));
		uiPane.setMinSize(root.getWidth(), root.getHeight());

		initTop();
		initBottom();
		initBorders();

		// Update weapon
		updateWeapon();
		
		crosshairView = new ImageView(new Image("file:assets/crosshair.png"));
		GameManager.getRoot().getChildren().add(crosshairView);
		crosshairView.setPreserveRatio(true);
		crosshairView.setFitWidth(32);

		hpFlickerCounter = 0;
		hpFlickerTimeline = new Timeline(new KeyFrame(Duration.millis(35), new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (previousHp < 0)
				{
					return;
				}

				for (int i = 0; i < hearts.size(); i++)
				{
					hearts.get(i).setTranslateY(Math.random() * 10 - 5);
				}

				hpFlickerCounter++;

				ImageView currentHeart = hearts.get((int)Math.min(previousHp, 3));
				if (hpFlickerCounter % 4 > 2)
				{
					currentHeart.setImage(heartFull);
					currentHeart.setScaleY(1.1);
				}
				else
				{
					currentHeart.setImage(heartEmpty);
					currentHeart.setScaleY(1);
				}
			}
		}));
		hpFlickerTimeline.setOnFinished(e ->
		{
			for (int i = 0; i < hearts.size(); i++)
			{
				hearts.get(i).setTranslateY(0);
			}
		});
		hpFlickerTimeline.setCycleCount(40);

		// Add ui gridpane to root pane
		root.getChildren().add(uiPane);
	}
	
	private static void initTop()
	{
		// Create top gridpane and set width
		GridPane topPane = new GridPane();
		topPane.setMinWidth(uiPane.getWidth());
		topPane.setMaxWidth(uiPane.getWidth());

		// Set colum widths
		topPane.getColumnConstraints().addAll
		(
			new ColumnConstraints(AppProps.REAL_WIDTH / 3 - 100),
			new ColumnConstraints(AppProps.REAL_WIDTH / 3 + 180),
			new ColumnConstraints(AppProps.REAL_WIDTH / 3 - 100)
		);

		// Main hbox containing elements
		HBox hpBox = new HBox();
		hearts = new ArrayList<ImageView>();

		// HP
		// Load heart images for health
		heartsBox = new HBox(10);
		heartsBox.setPadding(new Insets(10));
		heartFull = new Image("file:assets/hud/HeartFull.png");
		heartEmpty = new Image("file:assets/hud/HeartEmpty.png");
		for (int i = 0; i < 4; i++)
		{
			ImageView heart = new ImageView(heartFull);
			heart.setFitHeight(heartImageSize);
			heart.setPreserveRatio(true);
			heartsBox.getChildren().add(heart);
			hearts.add(heart);
		}

		// HP text
		Label lblHp = new Label("HP");
		lblHp.setFont(Font.loadFont(AppProps.fontBPath, 20));
		lblHp.setPadding(new Insets(5, 3, 5, 5));
		lblHp.setTextFill(Color.WHITE);

		previousHp = 4;
		updateHealth(4);

		hpBox.getChildren().addAll(lblHp, heartsBox);
		topPane.add(hpBox, 0, 0);

		// Create minimap with clipping
		// Pane Clipping https://news.kynosarges.org/2016/11/03/javafx-pane-clipping/
		minimap = new Pane();
		minimap.setMaxWidth(200);
		minimap.setMinHeight(200);
		minimap.setMaxHeight(200);
		minimap.setClip(new Rectangle(200, 200));
		minimap.setPadding(new Insets(5, 3, 5, 5));
		GridPane.setHgrow(minimap, Priority.ALWAYS);
		GridPane.setHalignment(minimap, HPos.RIGHT);

		// Add player icon to minimap
		iconView = new ImageView(new Image("file:assets/playerIcon.png"));
		iconView.setFitWidth(20);
		iconView.setPreserveRatio(true);
		iconView.setX(90);
		iconView.setY(90);
		minimap.getChildren().add(iconView);

		HBox.setHgrow(minimap, Priority.ALWAYS);

		// Create arraylist of actual rooms for reference and rectangle rooms that are displayed
		minimapRoomReferences = new ArrayList<Rectangle>();
		minimapVisualRooms = new ArrayList<Rectangle>();

		// Set minimap colors
		minimap.setStyle("-fx-background-color: rgb(10, 10, 10, 0.9)");
		minimap.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1))));

		// Add end room to minimap
		UI.addMinimapRoom(GameManager.getMap().getEndRoom());

		// Set up info label (move offscreen by default)
		infoTimer = 0;
		lblInfo = new Label();
		lblInfo.setFont(Font.loadFont(AppProps.fontBPath, 15));
		lblInfo.setTextFill(Color.WHITE);
		lblInfo.setTranslateY(-200);
		lblInfo.setWrapText(true);
		GridPane.setHalignment(lblInfo, HPos.CENTER);

		// Add nodes to panes
		topPane.add(lblInfo, 1, 0);
		topPane.add(minimap, 2, 0);
		uiPane.setTop(topPane);
	}
	private static void initBottom()
	{
		HBox bottomBox = new HBox();
		bottomBox.setPadding(new Insets(20, 30, 20, 20));

		// Ammo text
		lblAmmo = new Label("AMMO");
		lblAmmo.setFont(Font.loadFont(AppProps.fontBPath, 40));
		lblAmmo.setPadding(new Insets(5, 3, 5, 5));
		lblAmmo.setTextFill(Color.WHITE);

		// Magazine text
		lblMags = new Label("MAGS");
		lblMags.setFont(Font.loadFont(AppProps.fontBPath, 30));
		lblMags.setPadding(new Insets(5, 3, 5, 5));
		lblMags.setTextFill(Color.rgb(200, 200, 200));

		// Info text (reloading, out, etc.)
		lblWeaponInfo = new Label("INFO");
		lblWeaponInfo.setFont(Font.loadFont(AppProps.fontBPath, 20));
		lblWeaponInfo.setTextFill(Color.rgb(200, 200, 200));

		// Add text to ammo VBox
		HBox ammoBox = new HBox();
		ammoBox.setAlignment(Pos.BOTTOM_RIGHT);
		ammoBox.getChildren().addAll(lblAmmo, lblMags);
		ammoBox.setPrefWidth(200);

		weaponImgView = new ImageView(new Image("file:assets/guns/default.png"));
		weaponImgView.setPreserveRatio(true);
		weaponImgView.setFitHeight(50);

		// Weapon HBox
		VBox weaponVBox = new VBox();
		weaponVBox.setMaxHeight(50);
		weaponVBox.getChildren().addAll(lblWeaponInfo, ammoBox, weaponImgView);
		weaponVBox.setAlignment(Pos.BOTTOM_RIGHT);

		bottomBox.getChildren().add(weaponVBox);
		bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
		uiPane.setBottom(bottomBox);
	}
	private static void initBorders()
	{
		// Create gradients
		rightGradient = new ImageView(new Image("file:assets/gradient_horizontal.png"));
		rightGradient.setFitHeight(AppProps.REAL_HEIGHT);
		rightGradient.setFitWidth(AppProps.REAL_WIDTH);
		rightGradient.setScaleX(-1);
		GameManager.getRoot().getChildren().add(rightGradient);

		leftGradient = new ImageView(new Image("file:assets/gradient_horizontal.png"));
		leftGradient.setFitHeight(AppProps.REAL_HEIGHT);
		leftGradient.setFitWidth(AppProps.REAL_WIDTH);
		GameManager.getRoot().getChildren().add(leftGradient);

		topGradient = new ImageView(new Image("file:assets/gradient_vertical.png"));
		topGradient.setFitHeight(AppProps.REAL_HEIGHT);
		topGradient.setFitWidth(AppProps.REAL_WIDTH);
		GameManager.getRoot().getChildren().add(topGradient);

		bottomGradient = new ImageView(new Image("file:assets/gradient_vertical.png"));
		bottomGradient.setFitHeight(AppProps.REAL_HEIGHT);
		bottomGradient.setFitWidth(AppProps.REAL_WIDTH);
		bottomGradient.setScaleY(-1);
		GameManager.getRoot().getChildren().add(bottomGradient);
	}
	
	// Method to update crosshair position on screen
	public static void updateCrosshairPos(double x, double y)
	{
		if (crosshairView != null)
		{
			crosshairView.setX(x - 16);
			crosshairView.setY(y - 16);
			crosshairView.toFront();
		}
	}

	public static void updateWeapon()
	{
		Player player = GameManager.getPlayer();

		// Check if player has been created
		if (player == null)
		{
			return;
		}

		Gun weapon = player.getWeapon();

		if (weapon.getMagazines() == 0 && weapon.getAmmo() == 0)
		{
			lblAmmo.setTextFill(Color.rgb(240, 5, 5));
			lblMags.setTextFill(Color.rgb(240, 5, 5));
			lblMags.setTextFill(Color.rgb(240, 5, 5));
			lblWeaponInfo.setText("OUT");
		}
		else
		{
			if (weapon.getAmmo() == 0)
			{
				lblAmmo.setTextFill(Color.rgb(250, 100, 20));
			}
			else
			{
				lblAmmo.setTextFill(Color.WHITE);
			}

			if (weapon.getMagazines() == 0)
			{
				lblMags.setTextFill(Color.rgb(240, 5, 5));
			}
			else
			{
				lblMags.setTextFill(Color.rgb(200, 200, 200));
			}

			if (weapon.getReloading())
			{
            	lblWeaponInfo.setText("Reloading");
			}
			else
			{
				// Default fallback: weapon name
				lblWeaponInfo.setTextFill(Color.rgb(200, 200, 200));
				lblWeaponInfo.setText(weapon.getName());
			}
		}

		// Set correct image and text
		weaponImgView.setImage(weapon.getImage());
		lblAmmo.setText(Integer.toString(weapon.getAmmo()));
		lblMags.setText("/" + Integer.toString(weapon.getMagazines()));
	}

	public static void updateHealth(int hp)
	{
		if (previousHp > hp)
		{
			hpFlickerTimeline.play();
		}

		for (int i = 0; i < hearts.size(); i++)
		{
			ImageView currentHeartView = hearts.get(i);
			if (hp > i)
			{
				currentHeartView.setImage(heartFull);
			}
			else
			{
				currentHeartView.setImage(heartEmpty);
			}
		}
		
		previousHp = hp;
	}

	public static void updateBorders()
	{
		Rectangle room = GameManager.getPlayer().getActiveRoom();

		if (room != null)
		{
			topGradient.setY((room.getY() - Camera.getY()) * AppProps.SCALE - AppProps.REAL_HEIGHT);
			bottomGradient.setY((room.getY() + room.getHeight() - Camera.getY()) * AppProps.SCALE - 50);
			leftGradient.setX((room.getX() - Camera.getX()) * AppProps.SCALE - AppProps.REAL_WIDTH);
			rightGradient.setX((room.getX() + room.getWidth() - Camera.getX()) * AppProps.SCALE);

			// Interpolate opacity from 0 to 1 to fade in
			topGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 1, 0.1));
			bottomGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 1, 0.1));
			leftGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 1, 0.1));
			rightGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 1, 0.1));
		}
		else
		{
			// Interpolate opacity from 1 to 0 to fade away
			topGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 0, 0.1));
			bottomGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 0, 0.1));
			leftGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 0, 0.1));
			rightGradient.setOpacity(Util.lerp(topGradient.getOpacity(), 0, 0.1));
		}
	}

	public static void addMinimapRoom(Rectangle room)
	{
		if (room == null || minimapRoomReferences.contains(room))
		{
			return;
		}

		// Initialize new rectangle for room to be displayed and add it to arraylist
		Rectangle minimapRoom = new Rectangle(room.getWidth() / minimapScale * 2, room.getWidth() / minimapScale * 2);

		// Set rectangle properies
		minimapRoom.setFill(Color.WHITE);
		minimapRoom.setOpacity(0);
		minimapVisualRooms.add(minimapRoom);

		// Keep track of the room that the minimap rectangle is referencing
		minimapRoomReferences.add(room);

		// Add to minimap pane at back to not overlap player icon
		minimap.getChildren().add(0, minimapRoom);

		// Add label if necessary
		if (room == GameManager.getMap().getStartRoom())
		{
			lblStartRoom = new Label("Spawn");
			lblStartRoom.setTextFill(Color.rgb(220, 20, 20));
			lblStartRoom.setFont(Font.loadFont(AppProps.fontAPath, 10));
			lblStartRoom.setPrefWidth(100);
			lblStartRoom.setAlignment(Pos.CENTER);

			minimap.getChildren().add(lblStartRoom);
		}
		else if (room == GameManager.getMap().getEndRoom())
		{
			lblEndRoom = new Label("GOAL");
			lblEndRoom.setTextFill(Color.rgb(220, 20, 20));
			lblEndRoom.setFont(Font.loadFont(AppProps.fontAPath, 10));
			lblEndRoom.setPrefWidth(100);
			lblEndRoom.setAlignment(Pos.CENTER);

			minimap.getChildren().add(lblEndRoom);
		}
	}

	public static void updateMinimap()
	{
		for (int i = 0; i < minimapRoomReferences.size(); i++)
		{
			// Get minimap room and real room
			Rectangle realRoom = minimapRoomReferences.get(i);
			Rectangle minimapRoom = minimapVisualRooms.get(i);

			minimapRoom.setWidth(Util.lerp(minimapRoom.getWidth(), realRoom.getWidth() / minimapScale, 0.1));
			minimapRoom.setHeight(Util.lerp(minimapRoom.getHeight(), realRoom.getHeight() / minimapScale, 0.1));

			minimapRoom.setLayoutX((realRoom.getX() - GameManager.getPlayer().getPosition().x) / minimapScale + 100);
			minimapRoom.setLayoutY((realRoom.getY() - GameManager.getPlayer().getPosition().y) / minimapScale + 100);

			// Lerp opacity
			minimapRoom.setOpacity(Util.lerp(minimapRoom.getOpacity(), 1, 0.03));

			// Label start room
			if (minimapRoomReferences.get(i) == GameManager.getMap().getStartRoom())
			{
				lblStartRoom.setLayoutX(minimapRoom.getLayoutX() + minimapRoom.getWidth() / 2 - lblStartRoom.getPrefWidth() / 2);
				lblStartRoom.setLayoutY(minimapRoom.getLayoutY() - 15);
			}

			// Label end room
			if (minimapRoomReferences.get(i) == GameManager.getMap().getEndRoom())
			{
				lblEndRoom.setLayoutX(minimapRoom.getLayoutX() + minimapRoom.getWidth() / 2 - lblEndRoom.getPrefWidth() / 2);
				lblEndRoom.setLayoutY(minimapRoom.getLayoutY() - 15);
			}
		}
	}

	public static void updateInfoLabel()
	{
		// Decrement timer and lerp based on if label should be on screen or not
		if (infoTimer > 0)
		{
			infoTimer--;
			lblInfo.setTranslateY(Util.lerp(lblInfo.getTranslateY(), 10, 0.1));
		}
		else
		{
			lblInfo.setTranslateY(Util.lerp(lblInfo.getTranslateY(), -150, 0.1));
		}
	}

	// Method to display a tip on the info label for a set time
	public static void setLabelInfo(String text, double time)
	{
		lblInfo.setText(text);
		infoTimer = time;
	}
}