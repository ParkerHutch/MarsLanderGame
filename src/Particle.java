import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;

public class Particle
{
   double x;
   double y;
   double sideLength;
   // particles will be squares, so only one side length needed
   
   double xVelocity;
   double yVelocity;
   
   int lifespan;
   
   Color color;
   
   Rectangle hitBox;
   
   Particle(){}
   
   Particle(
         double xPos, double yPos, double sL, double xV, double yV, 
         Color c, int life
         )
   {
      this.x = xPos;
      this.y = yPos;
      this.sideLength = sL;
      this.xVelocity = xV;
      this.yVelocity = yV;
      this.color = c;
      this.lifespan = life;
   }
   
   
   public double getX()
   {
      return x;
   }
   public void setX(double a)
   {
      this.x = a;
   }
   public double getY()
   {
      return y;
   }
   public void setY(double a)
   {
      this.y = a;
   }
   public double getSideLength()
   {
      return sideLength;
   }
   public void setSideLength(double s)
   {
      this.sideLength = s;
   }
   public double getXVelocity()
   {
      return xVelocity;
   }
   public void setXVelocity(double v)
   {
      this.xVelocity = v;
   }
   public double getYVelocity()
   {
      return yVelocity;
   }
   public void setYVelocity(double v)
   {
      this.yVelocity = v;
   }
   public Color getColor()
   {
      return color;
   }
   public void setColor(Color c)
   {
      this.color = c;
   }
   public Rectangle getHitBox()
   {
	  generateHitBox(); // makes the hitbox with x, y, and side length variables
      return hitBox;
   }
   public void generateHitBox()
   {
      Rectangle rect = new Rectangle(getX(), getY(), sideLength, sideLength);
      this.hitBox = rect;
   }
     
}