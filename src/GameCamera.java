public class GameCamera
{
   private float xOffset, yOffset;
   private LanderGame game;
   
   public GameCamera(LanderGame game, float xOffset, float yOffset)
   {
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.game = game;
   }
   
   public void move(float xAmount, float yAmount)
   {
      xOffset += xAmount;
      yOffset += yAmount;
   }
   
   public void centerOn(MarsModule player)
   {
      xOffset = (float) (player.getCenterX() - game.getWidth() / 2 );
      yOffset = (float) (player.getCenterY() - game.getHeight() / 2 );
   }
   
   public float getxOffset()
   {
      return xOffset;
   }
   public float getyOffset()
   {
      return yOffset;
   }
   public void setxOffset(float n)
   {
      xOffset = n;
   }
   public void setyOffset(float n)
   {
      yOffset = n;
   }
}