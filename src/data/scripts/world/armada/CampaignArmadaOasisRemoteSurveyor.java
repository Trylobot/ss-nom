package data.scripts.world.armada;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;


public class CampaignArmadaOasisRemoteSurveyor implements EveryFrameScript
{
  private CampaignFleetAPI fleet;
  
  public CampaignArmadaOasisRemoteSurveyor( CampaignFleetAPI fleet ) {
    this.fleet = fleet;
  }
  
  @Override
  public void advance(float amount) {
    
  }
  
  ///////////////////////////

  @Override
  public boolean isDone() {
    return (fleet == null || !fleet.isAlive());
  }

  @Override
  public boolean runWhilePaused() {
    return false;
  }

}
