package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import data.scripts.trylobot.TrylobotUtils;
import java.util.List;


public class TheNomadsCombatEnginePlugin implements EveryFrameCombatPlugin
{
	private CombatEngineAPI engine = null;
	
  @Override
	public void init( CombatEngineAPI engine )
	{
		this.engine = engine;
		executed_playNomadThemeMusic = false;
	}

  @Override
	public void advance( float amount, List events )
	{
    // once per init(), try to play the theme
    if (!executed_playNomadThemeMusic) {
      executed_playNomadThemeMusic = true;
      playNomadThemeMusic();
    }

	}
  
  private boolean executed_playNomadThemeMusic = true;
	//
  private void playNomadThemeMusic() {
    if ("nom_theme_music".equals(Global.getSoundPlayer().getCurrentMusicId()))
      return; // already playin', boys!
    //
    // If fighting against Nomads, or fighting using mostly Nomad ships, set the mood appropriately
    boolean set_the_mood = false;
    BattleCreationContext battle = engine.getContext();
    //
    if (battle != null) {
      CampaignFleetAPI other_fleet = battle.getOtherFleet();
      CampaignFleetAPI player_fleet = battle.getPlayerFleet();
      //
      if ( other_fleet != null && other_fleet.getFaction() != null
      && "nomads".equals( other_fleet.getFaction().getId()) ) {
        set_the_mood = true; // fighting Nomads! set the mood!
      }
      //
      if ( !set_the_mood && other_fleet != null ) {
        float ship_count = 0f;
        float nomad_ship_count = 0f;
        for (FleetMemberAPI ship : other_fleet.getMembersWithFightersCopy()) {
          ship_count += 1f;
          if (ship.getHullId().startsWith("nom_"))
            nomad_ship_count += 1f;
        }
        if (ship_count != 0f && ((nomad_ship_count / ship_count) >= 0.50f)) {
          set_the_mood = true; // we've obviously got a Nomads fan here, set the mood!
        }
      }
      //
      if ( !set_the_mood && player_fleet != null ) {
        float ship_count = 0f;
        float nomad_ship_count = 0f;
        for (FleetMemberAPI ship : player_fleet.getMembersWithFightersCopy()) {
          ship_count += 1f;
          if (ship.getHullId().startsWith("nom_"))
            nomad_ship_count += 1f;
        }
        if (ship_count != 0f && ((nomad_ship_count / ship_count) >= 0.50f)) {
          set_the_mood = true; // we've obviously got a Nomads fan here, set the mood!
        }
      }
		}
    //
    if ( set_the_mood ) {
      Global.getSoundPlayer().playMusic(2, 1, "nom_theme_music");
    }    
  }

  @Override
  public void renderInWorldCoords(ViewportAPI vapi) {
    // ...
  }

  @Override
  public void renderInUICoords(ViewportAPI vapi) {
    // ...
  }
  
}
