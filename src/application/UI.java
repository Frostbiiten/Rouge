package application;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UI
{
	public static GridPane uiPane;
	
	// Health
	private final static int heartImageSize = 20;
	private static Image heartFull, heartEmpty;
	private static HBox heartsBox;
	private static int previousHp;

	public static void init(Pane root)
	{
		uiPane = new GridPane();
		uiPane.setPadding(new Insets(10));
		//uiPane.setGridLinesVisible(true);

		// HP test
		Label lblHp = new Label("HP");
		lblHp.setFont(Font.loadFont(AppProps.fontPath, 20));
		uiPane.add(lblHp, 0, 0);
		lblHp.setPadding(new Insets(5, 3, 5, 5));
		lblHp.setTextFill(Color.WHITE);

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

		previousHp = 4;

		uiPane.add(heartsBox, 1, 0);
		updateHealth(2);

		// Add ui gridpane to root pane
		root.getChildren().add(uiPane);
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