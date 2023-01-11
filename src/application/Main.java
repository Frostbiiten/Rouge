package application;
	
import application.World.Map;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;


public class Main extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			
			Map map = new Map(new Image("file:s.png"), 100, 100);

			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
}
