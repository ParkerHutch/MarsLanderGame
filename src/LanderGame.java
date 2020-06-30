import javafx.scene.*;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.geometry.Dimension2D;

import java.math.RoundingMode;
import java.util.*;
import javafx.scene.shape.*;
import javafx.scene.transform.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import java.util.ArrayList;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.scene.text.*;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

import java.io.*;

public class LanderGame extends Application implements EventHandler<KeyEvent> {

   Scene gameScene;
   Stage theStage;
   
   // Terrain
   Line [] land;
   ArrayList<LandingPad> landingPads;
   Terrain map;
   
   // Dimensions
   int WIDTH = 1000;
   int HEIGHT = 680; 
   Rectangle2D screenBounds;
   
   // Useful global variables
   Group root;
   Canvas canvas;
   double cameraZoom = 1;
   
   // Camera
   private static GameCamera gameCamera;
   
   GraphicsContext g2d;
   
   // Player images
   static Image landingLegs = new Image("/images/landingLegsExtended.png");// = new Image("/src/images/landingLegsExtended.png");
   
   // Variables used for correct drawing of images
   double imageXOffset = 0;
   double xDrawPosition = 0;
   
   // Starting player properties (these should probably all be removed)
   MarsModule player;
   double xInitial = 50;
   double yInitial = 50;
   double playerWidth = landingLegs.getWidth(); 
   double playerHeight = landingLegs.getHeight();
   
   Rectangle playerHitBox;
   boolean playerCloseToGround = false;
   
   // currentKey, used for controlling player
   String currentKey = "None";
   
   // "Proximity Box", can be used to detect when the player is close to colliding
   Rectangle proximityBox;
   double boxBuffer = 50.0; // distance from center of player to edge of box
   
   // Framerate Measuring
   final long[] frameTimes = new long[100];
   int frameTimeIterator = 0;
   boolean frameTimesArrayFull = false;
   
   //Settings(Maybe make a file to store some of these?
   String gameMode = "EASY";
   static boolean centerEnabled = true;
   boolean autoZoomEnabled = false;
   boolean cancelAutoZoom = false;
   double rotationRate = 0.1; // how fast the player can rotate the rocket
   double rotateDecelerationRate = rotationRate / 2;
   double thrustAccelerationRate = 0.04; // how fast the player can increase thrust
   double thrustDecelerationRate = thrustAccelerationRate / 1.1; // I think this should be removed
   // how fast thrust decreases(when player engages thrust, this factor will be overcome)
   double thrustCap = 0.16;
   
   
   // Misc Variables
   int appliedZooms = 0;
   int scaleCoefficient = 4;
   String playerScoreReport = "Nice Landing! + 1000"; 
   int scoreReportDuration = 0;
   Text [] information = new Text[9]; // will hold 8 pieces of info for the player
   boolean startGame = false;
   AnimationTimer animator;
   File f = new File("src/sounds/engine1.mp3");
   //Media engineSoundFile = new Media(f.toURI().toString());
   //MediaPlayer engineNoise = new MediaPlayer(engineSoundFile);
   boolean engineSoundPlaying = false;
   
   
   public static void main(String[] args)
   {
      launch(args);
      /*
         IMPORTANT:
         	Make landing pads disappear and not offer points after being landed on
         	Stat report screen/game over screen
         	Maybe make an array of objects called "Reports"(make class) that store a duration,
         		information, and an x and y position. Then, when something happens, ex. player
         		out of fuel but presses W, display it on the screen
            
         Stuff to do:
         	Make a counter for how many times the player landed, display on game over screen
         	Score report screen(after player crashed)
            Make terrain generate based on the player position(saves time on startup, edge case if player goes 4,000 units from the center)
                  
            Noises
         Other ideas:
            Make an animation for raising/lowering the landing legs
               - When the proximityBox intersects the ground and zooms in, the legs
                 should extend, and when the player gets far enough away, they should
                 retract
            Replay system?
            	Maybe use a time variable for this?
      */
      
   }
   public void init() throws Exception
   {
	  // initialize the camera
      gameCamera = new GameCamera(this, 0,0);
      
      // generate the terrain
      map = new Terrain(WIDTH, HEIGHT);
      map.generateRandomLevel();
      land = map.getMap();
      landingPads = map.getLandingPads();
      
      normalizeAllLines(land); 
      
      // initialize the player
      player = new MarsModule(
                              0, map.getPlayerStartY(), playerWidth, playerHeight, 90, 
                              thrustDecelerationRate, rotateDecelerationRate, thrustCap
                              );      
      
   }
   public void start(Stage gameStage) throws Exception
   {

	  theStage = gameStage;
	  theStage.setTitle("Parker Hutchinson: Mars Lander");
      
      root = new Group();
      gameScene = new Scene(root, WIDTH, HEIGHT);
      
      final Box keyboardNode = new Box();
      keyboardNode.setFocusTraversable(true);
      keyboardNode.requestFocus();
      keyboardNode.setOnKeyPressed(this);
      keyboardNode.setOnKeyReleased(new EventHandler<KeyEvent>() 
         {
    	    public void handle(KeyEvent ke)
    	  	{    	  
    	  	   StringBuilder modifiableString = new StringBuilder(currentKey);
    	  	    	
    	  	   String letterPressed = ke.getText().toUpperCase(); // currentKey uses upper case values
    	  	        
    	  	   for (int i = 0; i < modifiableString.length(); i++)
    	  	   {
    	  	      String currentCharAsString = Character.toString(modifiableString.charAt(i));
    	  	    	   
    	  	      if (currentCharAsString.equals(letterPressed))
    	  	      {
    	  	         modifiableString.deleteCharAt(i);
    	  	      }
    	  	   }
    	  	    	
    	  	   currentKey = modifiableString.toString();
    	     }
         });
      
      root.getChildren().add(keyboardNode);
      
      screenBounds = Screen.getPrimary().getVisualBounds();
  	  theStage.setX(screenBounds.getMinX());
  	  theStage.setY(screenBounds.getMinY());
  	  theStage.setWidth(screenBounds.getWidth());
  	  theStage.setHeight(screenBounds.getHeight());
  	  
      canvas = new Canvas(theStage.getWidth(), theStage.getHeight());//(WIDTH, HEIGHT);// adding #'s here seems to help render more of the screen (max canvas size is 8192^2?)
      // also, adding numbers moves the canvas down further on the screen or further to the right
      
      g2d = canvas.getGraphicsContext2D();
      
      root.getChildren().add(canvas);
            
      showMainMenu(theStage);
      
	  g2d.setFill(Color.ORANGE);
	  g2d.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
            
      animator = new AnimationTimer()
      {
         @Override
         public void handle(long arg0)
         {
        	WIDTH = (int) screenBounds.getWidth();
        	HEIGHT = (int) screenBounds.getHeight();
        	       	
        	// replace all of these with setText
        	information[0] = new Text(WIDTH - 150, 200, 
        			 "X: " + (int) player.getCenterX() + " Y: " + (int) (HEIGHT - player.getCenterY()));
        	 
            information[1] = new Text(WIDTH - 150, 225, 
            		 "Distance to Ground: " + (int) player.getDistanceToGround());
             
            information[2] = new Text(WIDTH - 150, 250,
            		 "Rotation speed: " + (int) player.getRotateVelocity());
             
            information[3] = new Text(WIDTH - 150, 275, 
            		 String.format("Horizontal Velocity: %.2f", player.getXVelocity() ));
             
            information[4] = new Text(WIDTH - 150, 300,
            		 String.format("Vertical Velocity:  %.2f", -player.getYVelocity() ));
            
            information[5] = new Text(WIDTH - 150, 325,
            		"Framerate: " + (int) getFrameRate(arg0));
            
            information[6] = new Text(WIDTH - 150, 350,
            		String.format("Fuel: %.1f", player.getFuelLevel()) );
            
            information[7] = new Text(WIDTH - 150, 375,
            		"Score: " + player.getScore() );
            
            information[8] = new Text(WIDTH - 150, 400,
            		"Successful Landings: " + player.getLandingCount());
            
            root.setScaleX(cameraZoom); // canvas can also go here instead of root
            root.setScaleY(cameraZoom);
            
            // UDPATE
        	            
            g2d.clearRect(0,0, WIDTH, HEIGHT); // clear the screen
            
            // draw background
            g2d.setFill(Color.NAVAJOWHITE);
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            
            boxBuffer = 100;
            proximityBox = new Rectangle(
               player.getCenterX() - boxBuffer/2, 
               player.getCenterY() - boxBuffer/2,
               boxBuffer, 
               boxBuffer
               ); // gameCamera offset values are used when this is drawn
            
            if (colliding(land, proximityBox))
            {
            	int padBelowPlayerIndex = -1;
            	if (getPadBelowPlayerIndex(landingPads) >= 0)
                {
                  padBelowPlayerIndex = getPadBelowPlayerIndex(landingPads);
             	  LandingPad padBelowPlayer = landingPads.get(padBelowPlayerIndex);
                   if (colliding(padBelowPlayer, player.getHitBox()))
                   {
                 	  if (padBelowPlayer.safeLanding(player))
                 	  {
                                           		  
                          // if the player makes a safe landing, and the lander state isn't "crashed"...
                          if (
                              !player.getLanderState().equals("LANDED") && 
                              //!padBelowPlayer.landedOn() &&
                              !currentKey.contains("W") &&
                    	      !player.getLanderState().equalsIgnoreCase("CRASHED")
                    	     )
                          {
                         	// ...and the lander state hasn't been set to "landed" or "crashed" 
                         	// already, and the player isn't trying to engage the thruster, 
                         	// set lander state to landed and increase the score
                         	player.setXVelocity(0);
                         	player.setYVelocity(0);
                         	player.setLanderState("LANDED");
                         	if (!padBelowPlayer.landedOn())
                         	{
                         	   padBelowPlayer.setLandedOn(true);
                         	   System.out.println("Successful Landing!");
                         	   scoreReportDuration = 100;
                         	   player.setScore(player.getScore() + 1000);
                         	   player.setLandingCount(player.getLandingCount() + 1);
                         	}
                          }
                          
                 	  }
                 	  else
                 	  {
                 		 // if it wasn't a successful landing, set the lander state to "crashed"
                 		 // and display an explosion
                 		 
                 		 player.setXVelocity(0);
                  		 player.setYVelocity(0);
                  		 //drawCrashReport();
                 		 player.setExploding(true);
                  		 player.setLanderState("CRASHED");
                  		 
                  		 // Note: if the lander state is not set to "crashed" here, on the next iteration
                  		 // it will be registered as a safe landing since the velocities were set to 0
                 	  }
                   }
                }
               if (playerTouchingGround(land) && padBelowPlayerIndex == -1)
               {
            	  // if the player is touching the ground and there isn't a landing pad below...
            	  // (if there is a landing pad below, the safe landing method will take care
            	  // of a crash with the player)
            	  
            	  // if the player has crashed into the ground
            	  
                  player.setLanderState("CRASHED");
                  //drawCrashReport();
                  player.setExploding(true);
               }
               // if the player is close to the ground and the camera is zoomed out,
               // zoom in(gradually)	
               if (cameraZoom < 2.5 && autoZoomEnabled)
               {
                  cameraZoom *= 1.02;
                  appliedZooms += 1; 
               }
               cancelAutoZoom = true;
               // cancelAutoZoom will be used to make sure that we zoom out when
               // the player is no longer close to the ground 
                              
            }
            else
            {
               if (cancelAutoZoom && appliedZooms > 0)
               {
                  // if the player is no longer close to the ground, zoom back out
            	  
                  cameraZoom /= 1.02;
                  appliedZooms -= 1;
                  if (appliedZooms == 0)
                  {
                	  cancelAutoZoom = false; // the zoom from before has been cancelled, don't need to zoom out anymore
                  }
                  
               }
            }
            
            // RENDER
            g2d.setStroke(Color.BLACK);
            drawMap(land); // draw the ground
            
            drawLandingPads(landingPads);
            g2d.setStroke(Color.BLACK);
            
            if (player.getLanderState().equals("CRASHED"))
            {
               drawCrashReport();
            }
            
            applyInput(currentKey);
            // draw useful info for the player(change the font)
            g2d.setFill(Color.BLACK);
            for(int i = 0; i < information.length; i++)
            {
               g2d.fillText(information[i].getText(), 
            		   information[i].getX(), information[i].getY());
            }
            
            g2d.setStroke(Color.BLUE);
            
            if (centerEnabled)
            {
               gameCamera.centerOn(player);
            }
            else
            {
               //gameCamera.setxOffset(0);
               //gameCamera.setyOffset(0);
            }
            
            g2d.setStroke(Color.YELLOW); // draw proximity box
               
            g2d.setFill(Color.RED); // draw player 
            drawRotatedPlayer(g2d, player.getCurrentImage(), player, player.getHeadingAngle());
            
            if (scoreReportDuration > 0)
            {
               g2d.fillText("Nice Landing! +1000", 
            		   player.getCenterX() - getGameCamera().getxOffset(), 
            		   (player.getCenterY() - 50) - getGameCamera().getyOffset()
            		   );
               scoreReportDuration -= 1;
            }
            
            player.update(currentKey); // update the player
            
            drawFuelLevel();
            /*
            if (player.getEngineIgnited() && !engineSoundPlaying)
            {
               engineNoise.play();
            }
            else if (!player.getEngineIgnited())
            {
               engineNoise.stop();
            }
            */
         }
      };
      if (startGame)
      {
    	  animator.start();
      }
      
      theStage.show();
   }
   
   // handling keyboard input
   public void handle(KeyEvent arg0)
   {
      // use "current key" method to make the player move more naturally
      if (arg0.getCode() == KeyCode.I)
      {
    	// If the player presses the 'i' key, zoom out
         cameraZoom *= 1.02;
      }
      if (arg0.getCode() == KeyCode.N)
      {
         // If the player presses the 'n' key, zoom out
         cameraZoom /= 1.02;
      }
      if (arg0.getCode() == KeyCode.A)
      {
         // If the player presses the 'a' key, rotate left
    	 setCurrentKey("A");
      }
      if (arg0.getCode() == KeyCode.D)
      {
         // If the player presses the 'd' key, rotate right
         setCurrentKey("D");
      }
      if (arg0.getCode() == KeyCode.W)
      {
    	 setCurrentKey("W");
      }
      
      if (arg0.getCode() == KeyCode.X)
      {
         // toggle player focus
         centerEnabled = !centerEnabled; // flip the boolean
      }
      
      if (arg0.getCode() == KeyCode.SPACE && player.getLanderState().equals("CRASHED"))
      {
         // show the main menu
         showMainMenu(theStage);
      }
   }
   
   private void showMainMenu(Stage stage)
   {
	   
	   Pane mainMenuPane = new Pane();
	      
	   Canvas mainMenuCanvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
	   mainMenuPane.getChildren().add(mainMenuCanvas);
	    
	   GraphicsContext mainMenuGC = mainMenuCanvas.getGraphicsContext2D();
	      
	   mainMenuGC.setFill(Color.GRAY);
	   mainMenuGC.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
	     
	   mainMenuGC.setFill(Color.GOLD);
	   mainMenuGC.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, 100));
	   mainMenuGC.fillText("MARS LANDER", screenBounds.getWidth() / 2 - 200, 200, 400);
	   mainMenuGC.setFont(Font.font("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, 42));
	   mainMenuGC.fillText("By Parker Hutchinson", screenBounds.getWidth() / 2 - 100, 250, 200);
	   
	     
	   Button settingsButton = new Button("Controls/Settings");
	   settingsButton.setPrefSize(200, 25);
	   settingsButton.setLayoutX((screenBounds.getWidth() / 2) - settingsButton.getPrefWidth() / 2);
	   settingsButton.setLayoutY(screenBounds.getHeight() - 350);
	   settingsButton.setOnAction(event -> showSettings(stage));
	   
	   Button startButton = new Button("Start Game (Difficulty: " + gameMode + ")");
	   startButton.setPrefSize(200, 50);
	   startButton.setLayoutX((screenBounds.getWidth() / 2) - startButton.getPrefWidth() / 2);
	   startButton.setLayoutY(screenBounds.getHeight() - 300);
       startButton.setOnAction(event -> startGame(stage, gameScene));
		  
	   mainMenuPane.getChildren().add(startButton);
	   mainMenuPane.getChildren().add(settingsButton);
		  
	   Scene mainMenuScene = new Scene(mainMenuPane, 300, 300);
	   stage.setScene(mainMenuScene);
   }
   
   private void startGame(Stage stage, Scene nextScene)
   {
      startGame = true;
      player.setLanderState("FLYING");
      player.setExploding(false);
      player.setFuelLevel(player.getMaxFuel());
      player.xPos = 0;
      player.yPos = map.getPlayerStartY();
      player.setLandingCount(0);
      player.setScore(0);
      player.setHeadingAngle(90);
      player.setRotateVelocity(0);
      for (int i = 0; i < landingPads.size(); i++)
      {
         landingPads.get(i).setLandedOn(false);
      }
      animator.start();
      stage.setScene(nextScene);
   }
   
   private void showSettings(Stage stage)
   {
      Pane settingsPane = new Pane();
      
      Canvas settingsCanvas = new Canvas(screenBounds.getWidth(), screenBounds.getHeight());
      settingsPane.getChildren().add(settingsCanvas);
      GraphicsContext settingsGC = settingsCanvas.getGraphicsContext2D();
      
      settingsGC.setFill(Color.GRAY);
      settingsGC.fillRect(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
      Text currentGameMode = new Text(
    		  (screenBounds.getWidth() / 2) - 150, 
    		  315,
    		  "Current Difficulty: " + gameMode
    		  );
      currentGameMode.setFill(Color.GOLD);
      currentGameMode.setFont(Font.font("Times New Roman", FontWeight.BOLD, 30));
      
      Text clue = new Text(
    		  (screenBounds.getWidth() / 2) - 250, 
    		  (screenBounds.getHeight() - 300),
    		  "Click to change difficulty ->"
    		  );
      clue.setFill(Color.GOLD);
      clue.setFont(Font.font("Times New Roman", FontWeight.BOLD, 16));
      
      Text difficultyInfoHeading = new Text(
    		  (screenBounds.getWidth() / 2) - 150,
    		  125,
    		  "Difficulty Information"
    		  );
      difficultyInfoHeading.setFill(Color.GOLD);
      difficultyInfoHeading.setFont(Font.font("Times New Roman", FontWeight.BOLD, 36));
      
      Text difficultyInfo = new Text(
    		  (screenBounds.getWidth() / 2) - 200, 
    		  150,
    		  "Easy: Pressing A/D keys moves the lander left/right\n" + 
    		  "Hard: Pressing A/D keys rotates the lander\n" +
    		  "Pressing W fires the engine\n" + 
    		  "Press M/N to zoom in/out"
    		  );
      difficultyInfo.setFill(Color.GOLD);
      difficultyInfo.setFont(Font.font("Times New Roman", FontWeight.BOLD, 20));
      difficultyInfo.setTextAlignment(TextAlignment.CENTER);
      
      Button backToMenuButton = new Button("Back to Menu");
      backToMenuButton.setPrefSize(200, 50);
      backToMenuButton.setLayoutX((screenBounds.getWidth() / 2) - 100);
      backToMenuButton.setLayoutY((screenBounds.getHeight()) - 100);
      backToMenuButton.setOnAction(event -> showMainMenu(stage));
      
      Button setGameModeEasyButton = new Button("Easy");
      setGameModeEasyButton.setPrefSize(100, 50);
      setGameModeEasyButton.setLayoutX((screenBounds.getWidth() / 2) - 50);
      setGameModeEasyButton.setLayoutY((screenBounds.getHeight()) - 350);
      setGameModeEasyButton.setOnAction(event ->
      {
    	  gameMode = "EASY";
    	  currentGameMode.setText("Current Difficulty: " + gameMode);
      });
      
      Button setGameModeHardButton = new Button("Hard");
      setGameModeHardButton.setPrefSize(100, 50);
      setGameModeHardButton.setLayoutX((screenBounds.getWidth() / 2) - 50);
      setGameModeHardButton.setLayoutY(screenBounds.getHeight() - 300);
      setGameModeHardButton.setOnAction(event ->
      {
    	  gameMode = "HARD";
    	  currentGameMode.setText("Current Difficulty: " + gameMode);
      });
      
      
      settingsPane.getChildren().addAll(
    		  backToMenuButton, setGameModeEasyButton, setGameModeHardButton, 
    		  currentGameMode, clue, difficultyInfoHeading, difficultyInfo);
      
      Scene settingsScene = new Scene(settingsPane, screenBounds.getWidth(), screenBounds.getHeight());
      stage.setScene(settingsScene);
   }
   
   public void applyInput(String key)
   {
      if (key.contains("W") && player.getLanderState().equals("FLYING"))
      {
    	 if (gameMode.equals("HARD"))
    	 {
    		// if the gamemode is hard, increase the thrust
    		player.increaseThrustIntensity(thrustAccelerationRate);
    	 }
    	 else
    	 {
    		// if the gamemode is easy, just change the y velocity and consume fuel
    		if (player.getFuelLevel() > 0)
    		{
    		   player.setFuelLevel(player.getFuelLevel() - 0.1);
    		   player.setYVelocity(player.getYVelocity() - 0.06);
    		}
    	 }
      }
      if (key.contains("A") && player.getLanderState().equals("FLYING"))
      {
    	 if (gameMode.equals("HARD"))
    	 {
    	   player.setRotateVelocity(player.getRotateVelocity() - rotationRate);
    	 }
    	 else
    	 {
            player.setXVelocity(player.getXVelocity() - 0.1);
    	 }
      }
      if (key.contains("D") && player.getLanderState().equals("FLYING"))
      {
    	 if (gameMode.equals("HARD"))
    	 {
    		player.setRotateVelocity(player.getRotateVelocity() + rotationRate);
    	 }
    	 else
    	 {
    	    player.setXVelocity(player.getXVelocity() + 0.1);
    	 }
      }
   }
   
   public void setCurrentKey(String key)
   {
	   if (currentKey.equalsIgnoreCase("None"))
  	   {
          currentKey = key; // if no key was pressed, the current key is the one given
  	   }
  	   else if (!(currentKey.contains(key)))
  	   {
  	      // if the currentKey already contained given key, don't keep adding it
          currentKey += key; // if another key is also being pressed, add the W to it
  	   }
   }
   
   private static void rotate(GraphicsContext g, double angle, double px, double py)
   {
	  // make the transformation without applying it (the 90-angle is required to draw the image properly)
      Rotate r = new Rotate(90 - angle,
         px - getGameCamera().getxOffset(), py - getGameCamera().getyOffset()); 
      // apply the transformation
      g.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
   }
   
   private static void drawRotatedPlayer(GraphicsContext g, Image img, MarsModule user, double angle)
   {
      // "user" is the player object
	   // save the current transformation(rotation) of the GraphicsContext
      g.save(); 
      // rotate the GraphicsContext by the player's heading angle
      rotate(g, user.getHeadingAngle(), user.getCenterX(), user.getCenterY()); 
      // draw the player
      g.drawImage(img, user.getX() - getGameCamera().getxOffset() - user.getImageXOffset(),
                  user.getY() - getGameCamera().getyOffset());
      // restore the GraphicsContext transformation to original
      g.restore();
   }
   
   public void drawFuelLevel()
   {
	  double fuelBarX1 = 400;
	  double fuelBarX2 = WIDTH - fuelBarX1;
	  double barLength = fuelBarX2 - fuelBarX1;
	  
	  double fuelToBarRatio = barLength / player.getMaxFuel();
	  
      g2d.save();
      g2d.setLineWidth(5);
      g2d.setStroke(Color.RED);
      g2d.strokeLine(fuelBarX1, 50, fuelBarX2, 50);
      if (player.getFuelLevel() > 0.3)
      {
    	 // the if condition is to make sure there isn't a leftover green line part
    	 g2d.setStroke(Color.GREEN);
    	 g2d.strokeLine(fuelBarX1, 50, (int)(fuelBarX1 + player.getFuelLevel() * fuelToBarRatio), 50);
      }
      
      g2d.setFill(Color.BLACK);
      g2d.setLineWidth(1);
      g2d.setFont(Font.font("Sans Serif", FontWeight.BOLD, 14));
      g2d.fillText("Fuel: " + (int)player.getFuelLevel() + " / " + (int)player.getMaxFuel(), 
 			 fuelBarX1 + player.getFuelLevel() * fuelToBarRatio, 70);
      
      g2d.restore();
   }
   
   public void drawScoreReport(String c, double reportX, double reportY)
   {
      // draws string c at (reportX, reportY) for the duration given
	  // (duration is made smaller by 1 every frame, so it isn't in seconds units)
      g2d.save();
      g2d.setStroke(Color.BLACK);
      g2d.setLineWidth(3);
	  g2d.fillText(c, reportX - getGameCamera().getxOffset(), reportY - getGameCamera().getyOffset());
	  g2d.restore();
   }
   
   public void drawCrashReport()
   {
      String crashReport = "You crashed!\nYou landed " + player.getLandingCount() + " times.\nPress space to play again!";
      g2d.save();
      g2d.setFont(Font.font("Sans Serif", FontWeight.BOLD, 12));
      g2d.setFill(Color.BLACK);
      g2d.setTextAlign(TextAlignment.CENTER);
      g2d.fillText(crashReport, 
    		  player.getCenterX() - getGameCamera().getxOffset(),
    		  player.getCenterY() - 100 - getGameCamera().getyOffset());
      g2d.restore();
   }

   public void normalizeAllLines(Line[] lineArray)
   {
	  // converts the lines to coordinates that can be drawn by the computer
      for (int i = 0; i < lineArray.length; i++)
      {
         lineArray[i] = Terrain.normalizeLine(lineArray[i], HEIGHT);
      }
   }
   
   public void drawMap(Line [] lineArray)
   {
	  // for greater efficiency, the player's distance to the ground is 
	  // calculated in the drawing for loop of this method.
	  
      double x1;
      double x2;
      double y1;
      double y2;
      for (int i = 0; i < lineArray.length; i++)
      {
         Line drawableLine = lineArray[i];
         
         x1 = drawableLine.getStartX() - getGameCamera().getxOffset();
         y1 = drawableLine.getStartY() - getGameCamera().getyOffset();
         x2 = drawableLine.getEndX() - getGameCamera().getxOffset();
         y2 = drawableLine.getEndY() - getGameCamera().getyOffset();
         
         // draw the line
         //g2d.strokeLine(x1, y1, x2, y2);
         //g2d.setStroke(Color.RED);
         if (
            (drawableLine.getStartX() > (player.getCenterX() - ((WIDTH+50) / 2) )) &&
            (drawableLine.getEndX() < (player.getCenterX() + ((WIDTH+50) / 2) )))            
         {
        	 
        	 g2d.save();
        	 g2d.setStroke(Color.ORANGE);       	 
        	 g2d.setLineWidth(1.3);
        	 fillBelowLine(drawableLine);
        	 
        	 
        	 g2d.setStroke(Color.BLACK);
        	 g2d.strokeLine(x1, y1, x2, y2);
        	 g2d.restore();
        	 
        	 if ( (player.getCenterX() > lineArray[i].getStartX()) && (player.getCenterX() < lineArray[i].getEndX()) )
             {
                player.calculateDistanceToGround(lineArray[i]);
                
                /*
                 * Below code can be used to draw a line from the player to the ground
                g2d.strokeLine(player.getCenterX() -getGameCamera().getxOffset(), player.getCenterY() - getGameCamera().getyOffset(), 
                		player.getCenterX() - getGameCamera().getxOffset(), player.evaluateLineAt(lineArray[i], player.getCenterX()) - getGameCamera().getyOffset());
                	*/
             }
         }
         //fillBelowLine(drawableLine);
         g2d.setFill(Color.BLACK); // take this out?
      }      
   }
   
   public void fillBelowLine(Line line)
   {
      int x1 = (int) line.getStartX();
      int x2 = (int) line.getEndX();
      
      for (int i = x1; i < x2; i++)
      {
    	 // draw a line from the x and y coord of the line to the bottom of the screen(HEIGHT);
         g2d.strokeLine(
        		 i - getGameCamera().getxOffset(),
        		 Terrain.evaluateLineAt(line, i) - getGameCamera().getyOffset(), 
        		 i - getGameCamera().getxOffset(), 
        		 HEIGHT);
      }
    		  
   }
   
   public void drawLandingPads(ArrayList<LandingPad> pads)
   {
      for (int i = 0; i < pads.size(); i++)
      {
    	 LandingPad current = pads.get(i);
    	 g2d.setFill(current.getColor());
         g2d.fillRect(
        		      current.getX() - getGameCamera().getxOffset(),
        		      current.getY() - getGameCamera().getyOffset(), 
        		      current.getWidth(), current.getHeight());
      }
   }
   
   public boolean playerTouchingGround(Line[] ground)
   {
      boolean collisionStatus = false;
      
      for (int i = 0; i < ground.length; i++)
      {
    	 Line currentLine = ground[i];
    	 if (currentLine.getStartX() < player.getCenterX() && currentLine.getEndX() > player.getCenterX())
    	 {
    		// if the player is above the line...
    	    for (double j = player.getX(); j < player.getX() + player.getWidth(); j+= 0.5)
    	    {
    	       // for every x value in the line that is under the player...
    	       if (player.getHitBox().contains(j, Terrain.evaluateLineAt(currentLine, j)))
    	       {
    	    	  // ... if the player's hitbox contains the point, there is a collision
    	    	  collisionStatus = true;
    	       }
    	    }
    	 }
    	 /*
    	 if (ground[i].intersects(player.getX(), player.getY(), 5, 5))
    	 {
    	    collisionStatus = true;
    	 }
    	 */
    	 /*
    	 if (ground[i].contains(player.getX(), player.getY() + player.getHeight()))
    	 {
            collisionStatus = true;
    	 }*/
    	  
         // check if the hitbox collides with any of the lines
    	  
    	 /*
         if ( ground[i].getBoundsInLocal().intersects(hitBox.getBoundsInParent()))
         {
        	
            // if the hitbox is colliding with any of the lines, return true
            collisionStatus = true;
         }*/
      }
      return collisionStatus;
   }
   
   public boolean colliding(Line [] ground, Rectangle hitBox)
   {
	  boolean collisionStatus = false;
      for (int i = 0; i < ground.length; i++)
      {
         if ( ground[i].getBoundsInLocal().intersects(hitBox.getBoundsInParent()))
         {
         	
            // if the hitbox is colliding with any of the lines, return true
            collisionStatus = true;
         }  
      }
      return collisionStatus;
   }
   
   public boolean colliding(LandingPad pad, Rectangle hitBox)
   {
      boolean collisionStatus = false;
      if ( pad.getPadRectangle().intersects(hitBox.getBoundsInParent()) )
      {
         // if the hitbox is colliding with any of the lines, return true
         collisionStatus = true;
      }
      
      return collisionStatus;
   }
   
   public int getPadBelowPlayerIndex(ArrayList<LandingPad> pads)
   {
	  // this method returns the index of the pad below the player,
	  // if there is one. If there isn't, it returns -1.
	  int padBelow = -1;
      for (int i = 0; i < pads.size(); i++)
      {
         if (
             pads.get(i).getPadRectangle().getX() < player.getCenterX() && 
             pads.get(i).getPadRectangle().getX() + pads.get(i).getPadRectangle().getWidth() > player.getCenterX())
         {
            // if the player's center x-val is within the x-values of the landing pad, it's below the player
        	padBelow = i;
         }
      }
      return padBelow;
   }
   
   public double getFrameRate(long currentNanoTime)
   {
      long oldFrameTime, elapsedNanoSecondsPerFrame;
      double frameRate = 0;
      
	   oldFrameTime = frameTimes[frameTimeIterator];
   	frameTimes[frameTimeIterator] = currentNanoTime;
   	frameTimeIterator = (frameTimeIterator + 1) % frameTimes.length; 
   	// once frameTimeIterator == the length of the frameTimes array, ^ will return 0
   	if (frameTimeIterator == 0)
   	{
         frameTimesArrayFull = true;
   	}
   	if (frameTimesArrayFull)
   	{
         elapsedNanoSecondsPerFrame = (currentNanoTime - oldFrameTime) / frameTimes.length;
         //long elapsedNanoSecondsPerFrame = elapsedNanos / frameTimes.length;
         // convert nanoseconds to seconds
         frameRate = 1_000_000_000.0 / elapsedNanoSecondsPerFrame;               
   	}
   	return frameRate;
   }
   
   public static GameCamera getGameCamera()
   {
      return gameCamera;
   }
   public int getWidth()
   {
      return WIDTH;
   }
   public int getHeight()
   {
      return HEIGHT;
   }
}