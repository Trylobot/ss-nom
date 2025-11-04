package data.scripts.world.armada;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.util.List;

public class CampaignArmadaEscortFleetAI implements CampaignFleetAIAPI {
    private final CampaignFleetAPI escort;
    private final CampaignFleetAPI leader;
    private final CampaignFleetAIAPI vanilla_AI;

    public CampaignArmadaEscortFleetAI(CampaignFleetAPI escort, CampaignFleetAPI leader, CampaignFleetAIAPI vanilla_AI) {
        this.escort = escort;
        this.leader = leader;
        this.vanilla_AI = vanilla_AI;
    }

    @Override
    public boolean wantsToJoin(BattleAPI battle, boolean playerInvolved) {
        for (CampaignFleetAPI fleet : battle.getBothSides()) {
            if (fleet == leader) return true;
        }
        return vanilla_AI.wantsToJoin(battle, playerInvolved);
    }
	
	@Override
	public List<FleetAssignmentDataAPI> getAssignmentsCopy() {
		return vanilla_AI.getAssignmentsCopy();
	}

	@Override
	public void setActionTextProvider(com.fs.starfarer.api.campaign.FleetActionTextProvider provider) {
		vanilla_AI.setActionTextProvider(provider);
	}

	@Override
	public com.fs.starfarer.api.campaign.FleetActionTextProvider getActionTextProvider() {
		return vanilla_AI.getActionTextProvider();
	}

	@Override
	public void removeAssignment(FleetAssignmentDataAPI assignment) {
		vanilla_AI.removeAssignment(assignment);
	}

    @Override
    public void advance(float amount) {
        vanilla_AI.advance(amount);
    }

    @Override
    public boolean isHostileTo(CampaignFleetAPI other) {
        return vanilla_AI.isHostileTo(other);
    }

    @Override
    public EncounterOption pickEncounterOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet) {
        return vanilla_AI.pickEncounterOption(context, otherFleet);
    }

    @Override
    public EncounterOption pickEncounterOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet, boolean pureCheck) {
        return vanilla_AI.pickEncounterOption(context, otherFleet, pureCheck);
    }

    @Override
    public PursuitOption pickPursuitOption(FleetEncounterContextPlugin context, CampaignFleetAPI otherFleet) {
        return vanilla_AI.pickPursuitOption(context, otherFleet);
    }

    @Override
    public InitialBoardingResponse pickBoardingResponse(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CampaignFleetAPI otherFleet) {
        return vanilla_AI.pickBoardingResponse(context, toBoard, otherFleet);
    }

    @Override
    public List<FleetMemberAPI> pickBoardingTaskForce(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CampaignFleetAPI otherFleet) {
        return vanilla_AI.pickBoardingTaskForce(context, toBoard, otherFleet);
    }

    @Override
    public BoardingActionDecision makeBoardingDecision(FleetEncounterContextPlugin context, FleetMemberAPI toBoard, CrewCompositionAPI maxAvailable) {
        return vanilla_AI.makeBoardingDecision(context, toBoard, maxAvailable);
    }

    @Override
    public void performCrashMothballingPriorToEscape(FleetEncounterContextPlugin context, CampaignFleetAPI playerFleet) {
        vanilla_AI.performCrashMothballingPriorToEscape(context, playerFleet);
    }

    @Override
    public void reportNearbyAction(ActionType type, SectorEntityToken actor, SectorEntityToken target, String responseVariable) {
        vanilla_AI.reportNearbyAction(type, actor, target, responseVariable);
    }

    @Override
    public String getActionTextOverride() {
        return vanilla_AI.getActionTextOverride();
    }

    @Override
    public void setActionTextOverride(String actionTextOverride) {
        vanilla_AI.setActionTextOverride(actionTextOverride);
    }

    @Override
    public FleetAssignmentDataAPI getCurrentAssignment() {
        return vanilla_AI.getCurrentAssignment();
    }

    @Override
    public void addAssignmentAtStart(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion) {
        vanilla_AI.addAssignmentAtStart(assignment, target, maxDurationInDays, onCompletion);
    }

    @Override
    public void addAssignmentAtStart(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion) {
        vanilla_AI.addAssignmentAtStart(assignment, target, maxDurationInDays, actionText, onCompletion);
    }

    @Override
    public void removeFirstAssignment() {
        vanilla_AI.removeFirstAssignment();
    }

    @Override
    public void removeFirstAssignmentIfItIs(FleetAssignment assignment) {
        vanilla_AI.removeFirstAssignmentIfItIs(assignment);
    }

    @Override
    public boolean isCurrentAssignment(FleetAssignment assignment) {
        return vanilla_AI.isCurrentAssignment(assignment);
    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, Script onCompletion) {
        vanilla_AI.addAssignment(assignment, target, maxDurationInDays, onCompletion);
    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays, String actionText, Script onCompletion) {
        vanilla_AI.addAssignment(assignment, target, maxDurationInDays, actionText, onCompletion);
    }

    @Override
    public void addAssignment(FleetAssignment assignment, SectorEntityToken target, float maxDurationInDays,
                              String actionText, boolean allowOverride, Script onStart, Script onCompletion) {
        vanilla_AI.addAssignment(assignment, target, maxDurationInDays, actionText, allowOverride, onStart, onCompletion);
    }

    @Override
    public boolean isFleeing() {
        return vanilla_AI.isFleeing();
    }

    @Override
    public void clearAssignments() {
        vanilla_AI.clearAssignments();
    }

    @Override
    public void dumpResourcesIfNeeded() {
        vanilla_AI.dumpResourcesIfNeeded();
    }

    @Override
    public void notifyInteractedWith(CampaignFleetAPI otherFleet) {
        vanilla_AI.notifyInteractedWith(otherFleet);
    }

    @Override
    public FleetAssignment getCurrentAssignmentType() {
        return vanilla_AI.getCurrentAssignmentType();
    }

    @Override
    public void doNotAttack(SectorEntityToken other, float durDays) {
        vanilla_AI.doNotAttack(other, durDays);
    }

    @Override
    public boolean isMaintainingContact() {
        return vanilla_AI.isMaintainingContact();
    }
}
