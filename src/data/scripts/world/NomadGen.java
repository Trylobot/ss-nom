package src.data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;

import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import java.util.List;
import src.data.scripts.world.systems.TheNomadsNur;

public class NomadGen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);

        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("nomads");
        new TheNomadsNur().generate(sector);

    }
    

    private static void initFactionRelationships(SectorAPI sector) {

        FactionAPI nom = sector.getFaction("nomads");
        FactionAPI nom_oasis = sector.getFaction("nomads_oasis");
        // if in doubt, make factions hostile to Looters
        List<FactionAPI> factionList = sector.getAllFactions();
        factionList.remove(nom);
        factionList.remove(nom_oasis);
        for (FactionAPI fact : factionList) {
            nom.setRelationship(fact.getId(), RepLevel.HOSTILE);
            nom_oasis.setRelationship(fact.getId(), RepLevel.NEUTRAL);
        }
        nom_oasis.setRelationship("nomads", RepLevel.FRIENDLY);
        nom.setRelationship("player", RepLevel.NEUTRAL);
        nom.setRelationship("neutral", RepLevel.FRIENDLY);
        nom.setRelationship("independent", RepLevel.FRIENDLY);

    }
}
