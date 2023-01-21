/*
 * ICS4U1 CPT: ROUGE
 * Edem Hoggar
 * Mr. Conway
 * Description: This is a 2d top-down shooter in which the player traverses a maze of generated rooms in an attempt to progress down further levels
 * as much as possible. They accomplish this by traversing rooms with WASD and the mouse to find the end room to progress to the next level. However, it
 * isn't so simple. There are enemies in most rooms that must be defeated before the player can pass by. The player can engage in combat by holding down
 * or clicking the left mouse button to fire at the enemies. However, the enemies will fire back too with the same weapons. The player attains points by
 * destroying enemies, crates and barrels. Crates and barrels will provide useful items, such as health, ammo, and guns. It is important to be careful however,
 * because the player must avoid explosive crates, which can harm. However, the most important point source is the level the player is on. As they discover more
 * end rooms, difficulty increases but so does reward. One useful trick the player can use is dodging, which they can perform by clicking the right mouse button.
 * It allows them to move faster and temporarily become invulnerable, which can be useful when in a tricky situation. Sadly, all runs will eventually come to an
 * end due to diffuculty ramping up. When the player loses, they are shown a dialogue asking about if they want to save their score. If they answer yes, the program
 * will read previous scores from a file to compare and inform the user how well they performed. Subsequently, it will then write their score and name to the file
 * for future uses. The player will then be given the option to return to the title screen or replay, by pressing the L button to leave or the R button to replay.
 * Most of the game's progression happens within the player themselves rather than through in-game currency, similar to the 'rougelite/rougelike' game genre
 * 
 * Program details:
 * Some JavaFx components used include labels for text, ImageViews for images and Buttons for buttons. These were used when necessary rather than for 'filler' purposes
 * Alerts are demonstrated in two parts of the program: the tutorial and the score saving screen. The tutorial uses alerts to inform the user how to play the game. The score saving screen
 * uses alerts to display and prompt the user concerning the saving and comparing of their score.
 * Layouts were used mainly in the GUI, title screen and game over screen. One such layout is the gridpane, which was used in all three examples. GridPane helped to correctly display the layouts
 * as intended while making it easier and more efficient to do at the developer's standpoint
 * 1D and 2D arrays were used in many areas, mainly in the map generation. A blatant example of this is the storing of the tiles in the map, which actually was done using a 3D array.
 * The 2D part of it was to represent the floor tiles, but the wall tiles were represented as "extrusions" from the floor. The abstractions provided by multidimensional arrays make it much
 * easier to perform such tasks
 * Object oriented programming was used to model many of the game's objects, such as the player, enemies, projectiles, explosions, etc. The program properly obeyed encapsulation, using accessor
 * and mutator methods to manipulate data concisely.
 * Inheritance and polymorphism was utilized mainly for bullets and enemies. Each derived from their own parent class, and as a result had feautrues
 * of the parent. However, extra functionality was added at the subclass level for more flexibility/tweakability.
 * Animation and collision detection were used for the majority of the game, the player's ability to move and interact with various game objects, for example is a result of proper implementation
 * of such animation and collision detection features, especially using the rectangle class and bounds.
 * As described previously, files were read and written from for the saving/loading segments of the game 
 * Sounds were demonstrated from the explosion and gunshot sound effects
 */

package application;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			GameManager.start(primaryStage);
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
