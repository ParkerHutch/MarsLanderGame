import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.ArrayList;
import javafx.scene.canvas.*;
public class Terrain
{
   int playerX; // maybe use doubles here?
   int playerY;
   int playerStartY; // player starting y(needed so they won't be too high/low from the terrain)
   int width;
   int height;
   int initialDistanceFromGround = 200;
   
   double landingPadWidth = 25;
   double landingPadHeight = 5;
   int landingPadDelay = 0; // will be used to stop landing pads from being placed next to each other
   
   Line [] lines; 
   
   ArrayList<LandingPad> landingPads = new ArrayList<LandingPad>();
   
   Polygon groundPolygon; // will be used so it is easy to fill the ground with a color
   
   Terrain(){}
   
   Terrain(int w, int h)
   {
	   width = w;
	   height = h;
   }
   
   
   public void update(int x, int y)
   {
      playerX = x;
      playerY = y;
   }
   public void generateRandomLevel()
   {
	   Line currentLine;
	   Line playerStartLine = new Line(); // the line the player will spawn over
	   
	   int startY = 0;
	   int arrayIterator = 0; // used to add values to the line array
	   
	   int arrayIndexesNeeded = (width * 2 / 25) - 0; // multiplied by 2 because we need pos and negative lines
	   arrayIndexesNeeded *= 100; // get even more lines
	   lines = new Line[arrayIndexesNeeded];
	   for (int i = -width * 100; i < width * 100; i+= 25) // width needs to be multiplied by the
		   //coefficient above(for arrayIndexesNeeded)
	   {
		   // should make this procedural based on player's x position
		   
		   // generate the next line on the map based on the previous line, then add it to the array
		   // landing pad generation is enabled
		   currentLine = generateNextLine(i, startY, true);
		   //groundPolygon.getPoints().add(new Double[] {(double)i, startY});
		   lines[arrayIterator] = currentLine;
		   
		   if (i == 0)
		   {
			   // if the line is in the center of the map, save it so we can use it for spawning the player
			   playerStartLine = lines[arrayIterator];
		   }
		   
		   startY = (int) currentLine.getEndY();
		   arrayIterator += 1;
	   }
	   // first, turn the y-coordinate of the line into a computer coordinate, 
	   // then subtract the initial distance from ground to make the player start above the line
	   // at that distance
	   playerStartY = (int) ((height - playerStartLine.getStartY()) - initialDistanceFromGround);
	   
   }
   public void generateLevel1()
   {
      // make sure the array dimension(see line 10) matches the number of lines + 1
      // to make mountain fill color:
      // maybe draw a colored rectangle at an x position to the y position of a line
      // at that x?
      // maybe make a way to store line info(x1, y1, x2, y2) in a file and read it?
      lines = new Line[45];
      
      lines[0] = new Line(0, 50, 50, 100);
      lines[1] = new Line(50, 100, 75, 100);
      lines[2] = new Line(75, 100, 100, 60);
      lines[3] = new Line(100, 60, 140, 60);
      lines[4] = new Line(140, 60, 150, 90);
      lines[5] = new Line(150, 90, 190, 25);
      lines[6] = new Line(190, 25, 215, 25);
      lines[7] = new Line(215, 25, 230, 75);
      lines[8] = new Line(230, 75, 255, 75);
      lines[9] = new Line(255, 75, 275, 75);
      lines[10] = new Line(275, 75, 290, 175);
      lines[11] = new Line(290, 175, 315, 175);
      
      lines[12] = new Line(315, 175, 330, 200);
      lines[13] = new Line(330, 200, 340, 275);
      lines[14] = new Line(340, 275, 350, 290);
      lines[15] = new Line(350, 290, 375, 290);
      lines[16] = new Line(375, 290, 390, 250); // right side of first big peak
      lines[17] = new Line(390, 250, 400, 235); //
      lines[18] = new Line(400, 235, 425, 200);
      lines[19] = new Line(425, 200, 450, 190);
      lines[20] = new Line(450, 190, 475, 190);
      lines[21] = new Line(475, 190, 500, 200);
      lines[22] = new Line(500, 200, 525, 235); // end of crater
      lines[23] = new Line(525, 235, 550, 250);
      lines[24] = new Line(550, 250, 565, 250);
      lines[25] = new Line(565, 250, 570, 235);
      lines[26] = new Line(570, 235, 585, 270);
      lines[27] = new Line(585, 270, 600, 270);
      lines[28] = new Line(600, 270, 625, 240);
      lines[29] = new Line(625, 240, 650, 280);
      lines[30] = new Line(650, 280, 675, 280);
      lines[31] = new Line(675, 280, 700, 320);
      lines[32] = new Line(700, 320, 725, 300);
      lines[33] = new Line(725, 300, 750, 260);
      lines[34] = new Line(750, 260, 775, 260);
      lines[35] = new Line(775, 260, 800, 225);
      lines[36] = new Line(800, 225, 825, 215);
      lines[37] = new Line(825, 215, 850, 245);
      lines[38] = new Line(850, 245, 875, 245);
      lines[39] = new Line(875, 245, 900, 270);
      lines[40] = new Line(900, 270, 925, 315);
      lines[41] = new Line(925, 315, 950, 315);
      lines[42] = new Line(950, 315, 975, 340);
      lines[43] = new Line(975, 340, 1000, 400);
      
      lines[44] = new Line(-20, 30, 0, 0); // this works, shows up when player moves to the left
      // so make a function to generate a line using perlin noise from the last x and y coord
   }
   
   public Line [] getMap()
   {
      return lines;
   }
   
   public ArrayList<LandingPad> getLandingPads()
   {
	   return landingPads;
   }
   
   public Line generateNextLine(int lastX, int lastY, boolean makeLandingPads)
   {
	   int nextX, nextY;
	   // this will generate landing pads randomly if makeLandingPads is true
	   int landingPadProbability = 10; 
	   // there is approximately 1/ whatever number assigned here chance of creating a landing pad
	   int lineInterval = 25; // x distance for each line
	   double maxYDistance = 50;
	   
	   nextX = lastX + lineInterval;
	   nextY = lastY + (int)((Math.random() * (maxYDistance * 2)) - maxYDistance);
	   if (nextY < 0)
	   {
		   nextY *= -1;// make sure the lines are always positive(y values)
	   }
	   while(nextY > height)
	   {
		   nextY -= Math.random() * maxYDistance;
	   }
	   
	   if (makeLandingPads && landingPadDelay == 0)
	   {
		   // this will give the landing pad its y coordinate
		   // the landing pad object will have nothing other than 
		   // the x, y, width, and height
		   if ((int)(Math.random() * landingPadProbability) == landingPadProbability - 1)
		   {
		      nextY = lastY;
		      LandingPad landingPad = new LandingPad(lastX, height - lastY, landingPadWidth, landingPadHeight, Color.LIME);
		      landingPad.setAngleMargin(180);
		      landingPad.setMaxXVelocity(2.5);
		      landingPad.setMaxYVelocity(2.5);
		      landingPads.add(landingPad);
		      landingPadDelay += 5; // make 5 more lines before creating another landing pad
		      
		   }
	   }
	   
	   
	   Line nextLine = new Line(lastX, lastY, nextX, nextY);
	   if (landingPadDelay > 0)
	   {
	      landingPadDelay -= 1;
	   }
	   return nextLine;
   }
   
   public static double evaluateLineAt(Line function, double x)
   {
	  double slope, yIntercept, y;
	  slope = (
			(function.getEndY() - function.getStartY()) / 
		    ( (function.getEndX() - function.getStartX()) )
			 );
      // b = y - mx, using start x and y coords and slope to calculate
	  yIntercept = function.getStartY() - (slope * function.getStartX()); 
	  y = (slope * x) + yIntercept;
	  return y;
   }
   
   public static Line normalizeLine(Line unConverted, int h)
   {
      // This method converts the line's start and end positions to "normal" x/y coords
      // As opposed to y counting from the top of the display
      // If this isn't done, the collision detection will be weird
      
      // width variable won't actually be used
      // do I need to incorporate camera offset values?
      double x1, x2, unConvertedY1, unConvertedY2, convertedY1, convertedY2;
      x1 = unConverted.getStartX();
      x2 = unConverted.getEndX();
      unConvertedY1 = unConverted.getStartY();
      unConvertedY2 = unConverted.getEndY();
      
      convertedY1 = h - unConvertedY1;
      convertedY2 = h - unConvertedY2;
      
      Line normalLine = new Line(x1, convertedY1, x2, convertedY2);
      return normalLine;
   }
   
   public int getPlayerStartY()
   {
	   return playerStartY;
   }
}