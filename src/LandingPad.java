import javafx.scene.shape.*;
import javafx.scene.paint.Color;

public class LandingPad 
{
   boolean landedOn = false;
   
   // x and y positions of the landing pad
   int x;
   int y;
   // max x and y velocities the player can have for a successful landing
   double maxXVelocity;
   double maxYVelocity; 
   // angle the player should have at landing
   double targetAngle;
   // angle margin of error at landing (ex. player can't be upside down)
   double angleMargin;
   // width and height of the landing pad rectangle
   double width;
   double height;
   // color of the landing pad(rectangle)(changes color after player lands)
   // maybe make smaller landing pads with different colors = more points?
   Color color;
   
   LandingPad(){}
   
   LandingPad(
		      int xPos, int yPos, double w, double h,
		      Color c 
		     )
   {
      this.x = xPos;
      this.y = yPos;
      this.width = w;
      this.height = h;
      this.color = c;
   }
   
   LandingPad(
		      int xPos, int yPos, double w, double h,
		      Color c, double mX, double mY, 
		      double theta, double aM
		     )
   {
      this.x = xPos;
	  this.y = yPos;
	  
	  this.color = c;
	  
	  this.maxXVelocity = mX;
	  this.maxYVelocity = mY;
	  this.targetAngle = theta;
	  this.angleMargin = aM;
	  this.width = w;
	  this.height = h;
   }
   
   public Rectangle getPadRectangle()
   {
      Rectangle pad = new Rectangle(x, y, width, height);
      return pad;
   }
   
   public boolean safeLanding(MarsModule player)
   {
	  // assesses wheter or not the player landed safely
	  boolean landedSafely = false;
      if (
          Math.abs(player.getYVelocity()) < maxYVelocity && 
          player.getXVelocity() < maxXVelocity &&
          player.getHeadingAngle() > targetAngle - angleMargin &&
          player.getHeadingAngle() < targetAngle + angleMargin
          )
      {
    	  landedSafely = true;
    	  
      }
      else
      {
    	  landedSafely = false;
      }
      return landedSafely;
   }
   
   public int getX()
   {
      return x;
   }

   public void setX(int x) 
   {
      this.x = x;
   }

   public int getY() 
   {
      return y;
   }

   public void setY(int y) 
   {
      this.y = y;
   }
   
   public double getWidth()
   {
      return width;
   }
   public void setWidth(double w)
   {
      this.width = w;	
   }
   
   public double getHeight()
   {
      return height;
   }
   
   public void setHeight(double h)
   {
      this.height = h;   
   }
   
   public Color getColor()
   {
	  if (landedOn)
	  {
	     color = Color.YELLOW;
	  }
	  else
	  {
	     color = Color.LIME;
	  }
      return color;
   }
   
   public void setColor(Color c)
   {
      this.color = c;
   }
   
   public double getMaxXVelocity()
   {
      return maxXVelocity;
   }

   public void setMaxXVelocity(double maxXVelocity) 
   {
      this.maxXVelocity = maxXVelocity;
   }

   public double getMaxYVelocity() 
   {
      return maxYVelocity;
   }

   public void setMaxYVelocity(double maxYVelocity) 
   {
      this.maxYVelocity = maxYVelocity;
   }

   public double getTargetAngle() 
   {
      return targetAngle;
   }

   public void setTargetAngle(double targetAngle) 
   {
      this.targetAngle = targetAngle;
   }

   public double getAngleMargin() 
   {
      return angleMargin;
   }

   public void setAngleMargin(double angleMargin) 
   {
      this.angleMargin = angleMargin;
   }
   public boolean landedOn()
   {
      return landedOn;
   }
   public void setLandedOn(boolean b)
   {
      this.landedOn = b;
   }
}
