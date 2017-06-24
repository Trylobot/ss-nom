package data.missions.nomads_retribution;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin
{
  public void defineMission(MissionDefinitionAPI api)
  {
    // Set up the fleets
    api.initFleet(FleetSide.PLAYER, "NA", FleetGoal.ATTACK, false);
    api.setFleetTagline(FleetSide.PLAYER, "The Nomads (Flycatcher Fang Patrol)");
    
    api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);
    api.setFleetTagline(FleetSide.ENEMY, "Anti-Fighter Detachment");

    // Set up the player's fleet
    api.addToFleet(FleetSide.PLAYER, "nom_flycatcher_fang", FleetMemberType.SHIP, true);
    api.addToFleet(FleetSide.PLAYER, "nom_flycatcher_fang", FleetMemberType.SHIP, true);
    api.addToFleet(FleetSide.PLAYER, "nom_flycatcher_fang", FleetMemberType.SHIP, true);
    api.addToFleet(FleetSide.PLAYER, "nom_flycatcher_fang", FleetMemberType.SHIP, true);

    // Set up the enemy fleet
    api.addToFleet(FleetSide.ENEMY, "wolf_PD", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "wolf_PD", FleetMemberType.SHIP, false);
    api.addToFleet(FleetSide.ENEMY, "medusa_PD", FleetMemberType.SHIP, false);

    // Set up the map.
    float width = 12000f;
    float height = 16000f;
    api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
  }

}






