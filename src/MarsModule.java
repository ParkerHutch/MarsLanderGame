import javafx.scene.shape.*;
import javafx.scene.image.*;

public class MarsModule
{

	
	// rename this class to Mars Module
	
	
   int landingCount = 0;
	
   double gravity = 0.02;
   
   // variables related to coordinates and updating them
   double xPos;
   double yPos;
   double width; 
   double height;
   Rectangle hitBox;
   double xVelocity = 0;
   double yVelocity = 0;
   double distanceToGround;
   String landerState = "FLYING";

   // variables related to input
   String currentKey = "";
   
   // variables related to thrust and RCS
   boolean engineIgnited = false;
   boolean leftRCSactivated = false;
   boolean rightRCSactivated = false;
   double thrustIntensity;
   double thrustDecelerationRate;
   double thrustCap; // max thrust intensity(given by game class's settings)
   
   // variables related to rotation
   double rotateVelocity = 0;
   double rotateDecelerationRate = 0.3;
   double angle;
   
   // variables related to fuel and score
   double maxFuel = 50;
   double fuelConsumptionRate = 0.5;
   double fuel = maxFuel;
   int playerScore = 0;
   
   // variables related to the player image
   boolean exploding = false;
   double explosionSpritesIterator = 0;
   Image [] explosionSprites = new Image[7];
   Image [] engineOffSprites = new Image[1];
   Image [] engineIgnitedSprites = new Image[1];
   // the first image in the below 3 arrays is with engine off, 2nd is with engine on
   Image [] leftRCSsprites = new Image[2];
   Image [] rightRCSsprites = new Image[2];
   Image [] doubleRCSsprites = new Image[2];
   // ^images for when both of the RCS systems are engaged
   Image blankImage;
   int spritesIterator = 0;
   Image currentImage;
   double imagexOffset = 0;
   double xDrawPosition = 0;
   
   MarsModule(
               double x, double y, double w, double h, double a, 
               double thrustDecelRate, double rotateDecelRate, double tCap
               )
   {
      this.xPos = x;
      this.yPos = y;
      this.width = w;
      this.height = h;
      this.angle = a;
      this.thrustDecelerationRate = thrustDecelRate; 
      this.thrustCap = tCap;
      // deceleration rate should be given by game class since it is half of the 
      // acceleration rate, which is determined in the settings in the game class
      this.rotateDecelerationRate = rotateDecelRate;
      
      
      engineOffSprites[0] = new Image("/images/landingLegsExtended.png");
      engineIgnitedSprites[0] = new Image("/images/ignited1.png");
      
      leftRCSsprites[0] = new Image("/images/leftRCS1.png");
      leftRCSsprites[1] = new Image("/images/leftRCS2.png");
      
      rightRCSsprites[0] = new Image("/images/rightRCS1.png");
      rightRCSsprites[1] = new Image("/images/rightRCS2.png");
      
      doubleRCSsprites[0] = new Image("/images/doubleRCS.png");
      doubleRCSsprites[1] = new Image("/images/doubleRCS1.png");
      
      explosionSprites[0] = new Image("/images/explosion1.png");
      explosionSprites[1] = new Image("/images/explosion2.png");
      explosionSprites[2] = new Image("/images/explosion3.png");
      explosionSprites[3] = new Image("/images/explosion4.png");
      explosionSprites[4] = new Image("/images/explosion5.png");
      explosionSprites[5] = new Image("/images/explosion6.png");
      explosionSprites[6] = new Image("/images/explosion7.png");
      
      blankImage= new Image("/images/blankimage.png"); // blank image for after lander is destroyed
   }
   
   
   public void update(String key)
   {
	  this.currentKey = key;
	  
	  if (landerState.equalsIgnoreCase("FLYING"))
	  {
		   if (key.contains("W") && (getFuelLevel() > fuelConsumptionRate * getThrustIntensity()) )
		   {
		      // if there is enough fuel, ignite the engine
		      setEngineIgnited(true);
		   }
		   else
		   {
		      // if there isn't, the engine can't be ignited.
		      setEngineIgnited(false);
		   }
		   
         if (key.contains("A") && getLanderState().equals("FLYING"))
         {
            rightRCSactivated = true;
         }
         else
         {
            rightRCSactivated = false;
         }
         if (key.contains("D") && getLanderState().equals("FLYING"))
         {
            leftRCSactivated = true;
         }
         else
         {
            leftRCSactivated = false;
         }
	     setHeadingAngle(getHeadingAngle() - getRotateVelocity()); // apply rotation
         
         decreaseThrustIntensity(thrustDecelerationRate);
         applyThrust();
      
         if (rotateVelocity > 0)
         {
            setRotateVelocity(getRotateVelocity() - rotateDecelerationRate);
         }
         else if (rotateVelocity < 0)
         {
            setRotateVelocity(getRotateVelocity() + rotateDecelerationRate);
         }
         
         yVelocity += gravity;
         xPos += xVelocity;
         yPos += yVelocity;
	  }
	  else if (getLanderState().equals("LANDED"))
	  {
	     if (currentKey.contains("W"))
	     {
	    	 // only allow the player to exit "landed" state if they are using the thruster
	    	 // this is to prevent the player from falling into the ground when gravity is
	    	 // applied again
	    	 
	    	 setLanderState("FLYING");
	     }
	  }
	  else if (getLanderState().equals("CRASHED"))
	  {
	     setXVelocity(0);
	     setYVelocity(0);    
	  }
   }
   
   public boolean getExploding()
   {
      return exploding;
   }
   
   public void setExploding(boolean status)
   {
      this.exploding = status;
      if (!status)
      {
         explosionSpritesIterator = 0;
      }
   }
   
   public void calculateDistanceToGround(Line lineBelowPlayer)
   {
	  // this method calculates the player's distance to the line directly below it
	  // the line below the player must be given
	   
	  double dist, targetY;
      // calculate the equation of the line y = mx + b - > b = y - mx
	  
	  targetY = Terrain.evaluateLineAt(lineBelowPlayer, getCenterX());
	  
	  dist = targetY - (getY() + getHeight());
	  
      setDistanceToGround(dist);
   }
   
   public void increaseThrustIntensity(double th)
   {
      if (getThrustIntensity() + th > thrustCap)
      {
         this.thrustIntensity = thrustCap;
      }
      else
      {
         this.thrustIntensity += th;
      }
   }
   
   public void decreaseThrustIntensity(double th)
   {
      if (getThrustIntensity() - th < 0)
      {
         // we don't want to have a negative thrust, so if the number will be negative
         // just set the thrust intensity to 0
         this.thrustIntensity = 0;
      }
      else
      {
         this.thrustIntensity -= th;
      }
   }
   
   public void applyThrust()
   {      
	  if (fuel > this.getThrustIntensity() * fuelConsumptionRate)
	  {
		 // if there is enough fuel to fire the engine, apply the thrust towards the player's heading direction
	     yVelocity += this.getThrustIntensity() * -Math.sin(Math.toRadians(getHeadingAngle()));
         xVelocity += this.getThrustIntensity() * Math.cos(Math.toRadians(getHeadingAngle()));
         // reduce the amount of fuel consumed during thrust
         setFuelLevel(getFuelLevel() - (fuelConsumptionRate * this.getThrustIntensity()));
	  }
	  else
	  {
	     System.out.println("No fuel");
	  }
   }
   
   public Rectangle getHitBox()
   {
      hitBox = new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
      return hitBox;
   }
   
   public Image getCurrentImage()
   {
	  if (getExploding())
	  {
		 // if the lander is exploding, "play" the "animation"
		 if (explosionSpritesIterator < 7)
		 {
			currentImage = explosionSprites[(int)explosionSpritesIterator];
			explosionSpritesIterator += 0.05; // # here changes how fast the animation is
		 }
		 else
		 {
		    currentImage = blankImage;
		 }
	  }
	  
	  else if (engineIgnited)
	  {
		 // if the engine is ignited..
		 if (leftRCSactivated || rightRCSactivated)
		 {
	        if (leftRCSactivated && !rightRCSactivated)
	        {
	           // ... in addition to the left RCS
	           currentImage = leftRCSsprites[1];
	        }
	        else if (!leftRCSactivated && rightRCSactivated)
	        {
	           // ... in addition to the right RCS
	           currentImage = rightRCSsprites[1];
	        }
	        else if (leftRCSactivated && rightRCSactivated)
	        {
	           // in addition to both of the RCS systems
	           currentImage = doubleRCSsprites[1];
	        }
		 }
		 else
		 {
			// ... and RCS isn't activated, the image is just the engine ignited 
			currentImage = engineIgnitedSprites[0];
		 }
	  }
	  else
	  {
		 // if the engine isn't ignited...
		 if (leftRCSactivated || rightRCSactivated)
		 {
		       if (leftRCSactivated && !rightRCSactivated)
		       {
		          currentImage = leftRCSsprites[0];
		       }
		       else if (!leftRCSactivated && rightRCSactivated)
		       {
		          currentImage = rightRCSsprites[0];
		       }
		       else if (leftRCSactivated && rightRCSactivated)
		       {
		          currentImage = doubleRCSsprites[0];
		       }
		 }
		 else
		 {
			 currentImage = engineOffSprites[0];
		 }
	  }
	  
      return currentImage;
   }
   
   public void setCurrentImage(Image img)
   {
      this.currentImage = img;
   }
   
   public double getImageXOffset()
   {
	  imagexOffset = 0;
	  for (int i = 0; i < leftRCSsprites.length; i++)
	  {
		  if (
			 getCurrentImage().equals(leftRCSsprites[i]) ||
			 getCurrentImage().equals(doubleRCSsprites[i])
			 )
		  {
		     imagexOffset = calculateImagexOffset();
		  }
	  }
      // if the image has a modification on left side, calculate new x here
	  // else, it's fine
	  return imagexOffset;
   }
   
   public double calculateImagexOffset()
   {
	  // subtract the current image width from the original image width
	  imagexOffset = Math.abs(leftRCSsprites[0].getWidth() - engineOffSprites[0].getWidth());
	  return imagexOffset;
   }
   public double getX()
   {
      return this.xPos;
   }
   public double getY()
   {
      return this.yPos;
   }
   public double getCenterX()
   {
      return this.getX() + (this.getWidth() / 2);
   }
   public double getCenterY()
   {
      return this.getY() + (this.getHeight() / 2);
   }
   public double getWidth()
   {
      return this.width;
   }
   public double getHeight()
   {
      return this.height;
   }
   public double getXVelocity()
   {
      return xVelocity;
   }
   public void setXVelocity(double v)
   {
      this.xVelocity = v;
   }
   public void setYVelocity(double v)
   {
      this.yVelocity = v;
   }
   public double getYVelocity()
   {
      return yVelocity;
   }
   public double getRotateVelocity()
   {
      return rotateVelocity;
   }
   public void setRotateVelocity(double rV)
   {
      this.rotateVelocity = rV;
   }
   public double getHeadingAngle()
   {
      // returns the angle to the "top" of the rocket
      return angle;
   }
   public void setHeadingAngle(double a)
   {
      this.angle = a;
   }
   public double getThrustIntensity()
   {
      return this.thrustIntensity;
   }
   public void setDistanceToGround(double d)
   {
      this.distanceToGround = d;
   }
   public double getDistanceToGround()
   {
      return distanceToGround;
   }
   public boolean getEngineIgnited()
   {
      return engineIgnited;
   }
   public void setEngineIgnited(boolean b)
   {
      this.engineIgnited = b;
   }
   public double getFuelLevel()
   {
      return fuel;
   }
   public void setFuelLevel(double f)
   {
      this.fuel = f;
   }
   public double getMaxFuel()
   {
      return maxFuel;
   }
   public void setScore(int s)
   {
      this.playerScore = s;
   }
   public int getScore()
   {
      return playerScore;
   }
   public void setLanderState(String state)
   {
      this.landerState = state;
   }
   public String getLanderState()
   {
      return landerState;
   }
   public void setLandingCount(int num)
   {
      this.landingCount = num;
   }
   public int getLandingCount()
   {
      return landingCount;
   }
}