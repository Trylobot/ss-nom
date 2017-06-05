package src.data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import java.util.Random;

public class Nomad_SpecialFactory {

    /*   
		"nom_flycatcher_toad"
        	"nom_flycatcher_scarab"
		"nom_flycatcher_iguana"
		"nom_flycatcher_fang"
     */
    private final String nameFaction = "nomads_oasis";

    public CampaignFleetAPI spawnRoyalCommandFleet(SectorEntityToken entity) {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(nameFaction, "Royal Command Fleet", true);

        FleetDataAPI data = fleet.getFleetData();

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "nom_gila_monster_antibattleship");
        member.setShipName("Royal Commander Ship");
        member.setFlagship(true);

        PersonAPI person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("nomads"), 20);

        member.setCaptain(person);
        data.addFleetMember(member);

        this.addRandom(data,
                new String[]{
                    "nom_sandstorm_assault",
                    "nom_rattlesnake_assault",
                    "nom_scorpion_assault",
                    "nom_komodo_mk2_assault",
                    "nom_komodo_assault",
                    "nom_roadrunner_pursuit",
                    "nom_flycatcher_scarab",
                    "nom_flycatcher_iguana",
                    "nom_death_bloom_strike",
                    "nom_yellowjacket_sniper"
                },
                new int[]{
                    3,
                    5,
                    2,
                    2,
                    5,
                    2,
                    1,
                    1,
                    3,
                    4,},
                28,
                180,
                20
        );

        FleetFactory.finishAndSync(fleet);

        this.putLocation(entity, fleet);
        return fleet;
    }

    public CampaignFleetAPI spawnScoutFleet(SectorEntityToken entity) {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(nameFaction, "Scout Fleet", true);

        FleetDataAPI data = fleet.getFleetData();

        int rand=new Random().nextInt(2);
        String ship=(rand==0)?"nom_flycatcher_fang":"nom_flycatcher_toad";
                
                
        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ship);
        member.setShipName("Scout Leader Ship");
        member.setFlagship(true);

        PersonAPI person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("nomads"), 5);

        member.setCaptain(person);
        data.addFleetMember(member);

        this.addRandom(data,
                new String[]{
                    "nom_wurm_assault",
                    "nom_yellowjacket_sniper"
                },
                new int[]{
                    1,
                    1,},
                2,
                20, 5);

        FleetFactory.finishAndSync(fleet);

        this.putLocation(entity, fleet);
        return fleet;
    }

    public CampaignFleetAPI spawnAssassinFleet(SectorEntityToken entity) {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(nameFaction, "Assassin Fleet", true);

        FleetDataAPI data = fleet.getFleetData();

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "nom_death_bloom_strike");
        member.setShipName("Assissin Leader Ship");
        member.setFlagship(true);

        PersonAPI person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("nomads"), 10);

        member.setCaptain(person);
        data.addFleetMember(member);

        this.addRandom(data,
                new String[]{
                    "nom_roadrunner_pursuit",
                    "nom_death_bloom_strike",},
                new int[]{
                    1,
                    1
                },
                2,
                38, 10);

        FleetFactory.finishAndSync(fleet);

        this.putLocation(entity, fleet);
        return fleet;
    }

    public CampaignFleetAPI spawnRoyalGuardFleet(SectorEntityToken entity) {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(nameFaction, "Royal Guard Fleet", true);

        FleetDataAPI data = fleet.getFleetData();

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "nom_sandstorm_assault");
        member.setShipName("Royal Guard Ship");
        member.setFlagship(true);

        PersonAPI person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("nomads"), 15);

        member.setCaptain(person);
        data.addFleetMember(member);

        this.addRandom(data,
                new String[]{
                    "nom_rattlesnake_assault",
                    "nom_scorpion_assault",
                    "nom_komodo_mk2_assault",
                    "nom_komodo_assault",
                    "nom_roadrunner_pursuit",
                    "nom_flycatcher_toad",
                    "nom_flycatcher_fang",
                    "nom_death_bloom_strike",
                    "nom_yellowjacket_sniper",
                    "nom_wurm_assault"
                },
                new int[]{
                    1,
                    2,
                    1,
                    3,
                    1,
                    1,
                    1,
                    1,
                    4,
                    6
                },
                21,
                75,
                15
        );

        FleetFactory.finishAndSync(fleet);

        this.putLocation(entity, fleet);
        return fleet;
    }

    public CampaignFleetAPI spawnFleet() {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(nameFaction, "Colony Fleet", true);

        FleetDataAPI data = fleet.getFleetData();

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "nom_oasis_standard");
        member.setShipName("Oasis");
        member.setFlagship(true);

        PersonAPI person = OfficerManagerEvent.createOfficer(Global.getSector().getFaction("nomads"), 20);

        member.setCaptain(person);
        data.addFleetMember(member);

        this.addRandom(data,
                new String[]{
                    "nom_sandstorm_assault",
                    "nom_rattlesnake_assault",
                    "nom_komodo_mk2_assault",
                    "nom_flycatcher_toad",
                    "nom_flycatcher_scarab",
                    "nom_flycatcher_iguana",
                    "nom_flycatcher_fang",},
                new int[]{
                    4,
                    4,
                    4,
                    1,
                    1,
                    1,
                    1
                },
                16,
                180,
                20
        );
      /*  Random random = new Random();
        FleetFactoryV2.addCommanderAndOfficers(3, 15, 20, fleet, person, random);*/
        FleetFactory.finishAndSync(fleet);
        return fleet;
    }

    private void putLocation(SectorEntityToken entity, CampaignFleetAPI newFleet) {
        LocationAPI location = entity.getContainingLocation();

        location.addEntity(newFleet);
        newFleet.setLocation(entity.getLocation().x, entity.getLocation().y);
    }

    private void addRandom(FleetDataAPI data, String[] members, int[] weight, int weightsize, int fleetPoints, int maxlevelOfficer) {

        int max = fleetPoints / 6;
        Random rander = new Random();
        int rand;
        int index;
        FleetMemberAPI ship;

        while (fleetPoints > max) {
            rand = rander.nextInt(weightsize);

            index = 0;
            while (rand > weight[index]) {
                rand -= weight[index];
                if (index == (weight.length - 1)) {
                    break;
                }
                index++;
            }
            ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, members[index]);
            fleetPoints -= ship.getFleetPointCost();

            if (fleetPoints < 0) {
                return;
            }
            data.addFleetMember(ship);
        }
    }
}
