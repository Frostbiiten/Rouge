package application.World;

import java.util.ArrayList;

import application.Vector2;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Partition
{
	// How much to vary width and height in proportion to the parent partition
	private final double VARY_WIDTH = 0.25, VARY_HEIGHT = 0.25;

	private int width, height, depth;
	private ArrayList<Partition> children;
	private int childCount;
	private int xPos, yPos;
	
	public Partition(int xPos, int yPos, int width, int height, int depth)
	{
		// Start with 0 children
		childCount = 0;

		children = new ArrayList<Partition>();

		// Initialize position, width, height and depth
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	// TODO: add padding
	public void addPartition()
	{
		childCount++;
		if (children.size() < 2)
		{
			// Initialize arguments to be passed into the new partition's constructor
			int baseXPos, baseYPos, newXPos, newYPos, baseWidth, baseHeight, variedWidth, variedHeight;

			// Check if partitions will be created horizontally or vertically
			boolean horizontal = width > height;

			// Check if rooms should be placed side by side or vertically
			if (horizontal)
			{
				baseWidth = width / 2;
				baseHeight = height;
			}
			else
			{
				baseWidth = width;
				baseHeight = height / 2;
			}

			// Vary width and height (reduction only, as expansion could overflow into other rooms)
			variedWidth = baseWidth - (int)(baseWidth * Math.random() * VARY_WIDTH * Math.log(1 + depth));
			variedHeight = baseHeight - (int)(baseHeight * Math.random() * VARY_HEIGHT * Math.log(1 + depth));

			// Default to creating at the top-left
			baseXPos = xPos;
			baseYPos = yPos;
			
			// If there is already a child, move partition to the bottom right (first would be created in top-left)
			if (children.size() != 0)
			{
				if (horizontal)
				{
					baseXPos += baseWidth;
				}
				else
				{
					baseYPos += baseHeight;
				}
			}

			// Randomize position in the respective half of the cell
			newXPos = baseXPos + (int)(Math.random() * (baseWidth - variedWidth));
			newYPos = baseYPos + (int)(Math.random() * (baseHeight - variedHeight));

			// Create partition and add to children arraylist
			children.add(new Partition(newXPos, newYPos, variedWidth, variedHeight, depth + 1));
		}
		else
		{
			int childCountA = children.get(0).getChildCount();
			int childCountB = children.get(1).getChildCount();
		
			// Decide what child to add new partition in
			Partition targetPartition;

			// Choose the room with the least children
			if (childCountA < childCountB)
			{
				targetPartition = children.get(0);
			}
			else if (childCountA > childCountB)
			{
				targetPartition = children.get(1);
			}
			else
			{
				// Randomly choose either index 0 or 1 by rounding a random value (between 0 and 1) up or down to 0 or 1
				targetPartition = children.get((int)Math.round(Math.random()));
			}

			targetPartition.addPartition();
		}
	}
	
	// Accessor methods
	public int getChildCount()
	{
		return childCount;
	}
	public int getXPos()
	{
		return xPos;
	}
	public int getYPos()
	{
		return yPos;
	}
	public int getWidth()
	{
		return width;
	}
	public int getHeight()
	{
		return height;
	}
	public ArrayList<Partition> getPartitions()
	{
		ArrayList<Partition> list = new ArrayList<Partition>();
		if (childCount == 0)
		{
			list.add(this);
		}
		else
		{
			for (int i = 0; i < children.size(); i++)
			{
				list.addAll(children.get(i).getPartitions());
			}
		}

		return list;
	}

	public void drawBounds(Vector2 cameraPos, WritableImage img)
	{
		PixelWriter writer = img.getPixelWriter();
		
		if (childCount == 0)
		{
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					int realX = xPos + x - (int)cameraPos.x;
					int realY = yPos + y - (int)cameraPos.y;

					// Make sure pixel is in range
					if (realX >= 0 && realX < img.getWidth() && realY >= 0 && realY < img.getHeight())
					{
						if (x == 0 || x == width - 1 || y == 0 || y == height - 1)
						{
							//int r = (int)(255 * (depth / 10.0));
							//writer.setArgb(realX, realY, 0xFF000000 | r);
							writer.setArgb(realX, realY, 0xFF000000);
						}
					}
				}
			}
		}

		for (int i = 0; i < children.size(); i++)
		{
			children.get(i).drawBounds(cameraPos, img);
		}
	}
}
