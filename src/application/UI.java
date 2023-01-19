package application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UI
{
	public static BorderPane uiPane;
	
	// Health
	private final static int heartImageSize = 20;
	private static Image heartFull, heartEmpty;
	private static HBox heartsBox;
	private static int previousHp;

	// Weapon
	private static Label lblAmmo, lblMags, lblWeaponInfo;
	private static ImageView weaponImgView;
	
	private static ImageView crosshairView;

	public static void init(Pane root)
	{
		// Create main ui borderpane
		uiPane = new BorderPane();
		uiPane.setPadding(new Insets(10));
		uiPane.setMinSize(root.getWidth(), root.getHeight());

		initTop();
		initBottom();

		// Update weapon
		updateWeapon();
		
		crosshairView = new ImageView(new Image("file:assets/crosshair.png"));
		GameManager.getRoot().getChildren().add(crosshairView);
		crosshairView.setPreserveRatio(true);
		crosshairView.setFitWidth(32);

		// Add ui gridpane to root pane
		root.getChildren().add(uiPane);
	}
	
	private static void initTop()
	{
		// Main hbox containing elements
		HBox topBox = new HBox();

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
		}

		// HP text
		Label lblHp = new Label("HP");
		lblHp.setFont(Font.loadFont(AppProps.fontBPath, 20));
		lblHp.setPadding(new Insets(5, 3, 5, 5));
		lblHp.setTextFill(Color.WHITE);

		previousHp = 4;
		updateHealth(4);

		topBox.getChildren().addAll(lblHp, heartsBox);
		uiPane.setTop(topBox);
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
	
	// Method to update crosshair position on screen
	public static void updateCrosshairPos(double x, double y)
	{
		crosshairView.setX(x - 16);
		crosshairView.setY(y - 16);
		crosshairView.toFront();
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
		// TODO: if previousHP > hp, flash hearts (decrease in hp)
		for (int i = 0; i < heartsBox.getChildren().size(); i++)
		{
			ImageView currentHeartView = (ImageView)heartsBox.getChildren().get(i);
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
}