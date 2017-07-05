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

public class CampaignArmadaEscortLeashScript implements EveryFrameScript
{
  private final SectorAPI sector;
  private final CampaignFleetAPI self;
  private final CampaignFleetAPI target;
  private final float leash_length;
  
  public CampaignArmadaEscortLeashScript(
    SectorAPI sector,
    CampaignFleetAPI self, 
    CampaignFleetAPI target, 
    float leash_length )
  {
    this.sector = sector;
    this.self = self;
    this.target = target;
    this.leash_length = leash_length;
  }
  
  @Override
  public void advance(float amount) {
    //
    if (self.getContainingLocation() == target.getContainingLocation()) {
      // same containing location
      //   distance check
      Vector2f self_loc = self.getLocation();
      Vector2f target_loc = target.getLocation();
      float leash_length_padded = (self.getRadius() + target.getRadius() + leash_length);
      float distance = Misc.getDistance( self_loc, target_loc );
      if (distance > leash_length_padded) {
        Vector2f intercept = Misc.getInterceptPoint(self, target);
        self.setMoveDestinationOverride( intercept.x, intercept.y );
        // Research: how do I "clear" the MoveDestinationOverride, once set? can I? should I?
        //   doesn't seem like it gets in the way much
        if (distance > (2f * leash_length_padded))
          self.getAbility("emergency_burn").activate();
      }
      //   battle check: fuckin' spread it on
      BattleAPI self_battle = self.getBattle();
      BattleAPI target_battle = target.getBattle();
      //
      if (target_battle != null
      && !target_battle.isInvolved( self )
      && !target_battle.isDone()
      && (self_battle == null || self_battle.isDone())
      && target_battle.canJoin( self )) {
        target_battle.join( self );
      }
    }
    else {
      // containing locations differ
      if (target.isInHyperspaceTransition()) {
        // target is jumping; time for a "transverse jump"
        sector.doHyperspaceTransition(self, self, new JumpDestination(target, "Jump"));
      }
    }
  }

  @Override
  public boolean isDone() {
    return (self == null || target == null || !self.isAlive() || !target.isAlive());
  }

  @Override
  public boolean runWhilePaused() {
    return false;
  }
}
