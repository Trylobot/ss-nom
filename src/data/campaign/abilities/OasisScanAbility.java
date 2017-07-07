package data.campaign.abilities;

import com.fs.starfarer.api.EveryFrameScript;
import java.awt.Color;
import java.util.EnumSet;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.trylobot.TrylobotUtils;
import data.scripts.world.armada.api.CampaignArmadaAPI;
import org.lazywizard.lazylib.campaign.MessageUtils;


public class OasisScanAbility extends BaseToggleAbility {
  
  public static final String COMMODITY_ID = Commodities.DRUGS;
	public static final float COMMODITY_PER_DAY = 0.142857f; // about 1/7th, or 1 drug per week.
	
	// does not affect the fleet's sensor profile
  public static final float DETECTABILITY_PERCENT = 0f;
  
  private CampaignArmadaAPI nomad_armada_controller = null;
	
	
	@Override
	protected String getActivationText() {
    // gotta have some drugs, yo
		if (COMMODITY_ID != null && getFleet() != null && getFleet().getCargo().getCommodityQuantity(COMMODITY_ID) <= 0 &&
				!Global.getSettings().isDevMode()) {
			return null;
		}
		return "Entering induced deep-dream state.";
	}
	
	@Override
	protected String getDeactivationText() {
		return null;
	}


	@Override
	protected void activateImpl() {

	}

	@Override
	public boolean showProgressIndicator() {
		return false;
	}
	
	@Override
	public boolean showActiveIndicator() {
		return isActive();
	}

	
	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
		Color bad = Misc.getNegativeHighlightColor();
		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		
		String status = " (off)";
		if (turnedOn) {
			status = " (on)";
		}
		
		LabelAPI title = tooltip.addTitle(spec.getName() + status);
		title.highlightLast(status);
		title.setHighlightColor(gray);

		float pad = 10f;
		
    tooltip.addPara(
      "Artifially induces a dream state in one of the crew, which when combined with " +
      "a low dosage of recreational drugs, allows for locating objects in hyperspace.", pad);
    
    tooltip.addPara("Consumes 1 unit of recreational drugs per week.", pad, highlight);
    
		if (getFleet() != null && !getFleet().isInHyperspace()) {
			tooltip.addPara("Can only function in hyperspace.", bad, pad);
		} else {
			tooltip.addPara("Can only function in hyperspace.", pad);
		}
    
    if (getFleet().getCargo().getCommodityQuantity("oasis_locator") < 1)
      tooltip.addPara("Requires Hallucinomagnetic Locator.", bad, pad);
		
		//tooltip.addPara("Disables the transponder when activated.", pad);
		addIncompatibleToTooltip(tooltip, expanded);
	}

	public boolean hasTooltip() {
		return true;
	}
	
	@Override
	public EnumSet<CampaignEngineLayers> getActiveLayers() {
		return EnumSet.of(CampaignEngineLayers.ABOVE);
	}


	@Override
	public void advance(float amount) {
		super.advance(amount);
	}
	

	private float phaseAngle;
	//private GraviticScanData data = null;
  private float[] data = null;
  
	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		//if (level < 1) level = 0;
		
		fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, "Oasis Deep-Dream");

		float days = Global.getSector().getClock().convertToDays(amount);
		phaseAngle += days * 360f * 10f;
		phaseAngle = Misc.normalizeAngle(phaseAngle);
    
    //for (SectorEntityToken ent : Global.getSector().getEntitiesWithTag("NOMAD_COLONY_FLEET")) {
    //  oasis = ent.getLocationInHyperspace();
    //}
    if (nomad_armada_controller == null) {
      for (EveryFrameScript script : Global.getSector().getScripts()) {
        if (script instanceof CampaignArmadaAPI) {
          nomad_armada_controller = (CampaignArmadaAPI) script;
        }
      }
    }
    CampaignFleetAPI oasis_fleet = nomad_armada_controller.getLeaderFleet();
    if (oasis_fleet != null) {
      //
      Vector2f origin = fleet.getLocation();
      Vector2f target = oasis_fleet.getLocationInHyperspace();
      //
      float angle = Misc.getAngleInDegrees(origin, target);
      float dist = Misc.getDistance(origin, target);
      float arc = Misc.computeAngleSpan(250f, dist);
      if (arc < 15f) arc = 15f;
      if (arc > 150f) arc = 150f;
      // set a value for each degree of 360, so that render() knows the intensity of the field response
      data = new float[360];
      float half = (float) Math.ceil(0.5f * arc);
      for (float i = -half; i <= half; i++) {
        float intensity = 1f - Math.abs(i / half);
        data[getIndex(angle + i)] = intensity * intensity * 200f;
      }
    } else {
      data = null;
    }
    // force (off) if run out of commodity
		if (COMMODITY_ID != null) {
			float cost = days * COMMODITY_PER_DAY;
			if (fleet.getCargo().getCommodityQuantity(COMMODITY_ID) > 0 || Global.getSettings().isDevMode()) {
				fleet.getCargo().removeCommodity(COMMODITY_ID, cost);
			} else {
				CommoditySpecAPI spec = getCommodity();
				fleet.addFloatingText("Out of " + spec.getName().toLowerCase(), Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
				deactivate();
			}
		}
		// force (off) if leaving hyperspace
		if (!fleet.isInHyperspace()) {
			deactivate();
		}
    // force (off) if the "Hallucinomagnetic Locator" quest item is removed
    if (fleet.getCargo().getCommodityQuantity("oasis_locator") < 1)
      deactivate();
	}
  
	public int getIndex(float angle) {
		angle = Misc.normalizeAngle(angle);
		int index = (int)Math.floor(angle);
		return index;
	}

  float getFieldStrengthAt(float angle) {
    if (data == null)
      return 0f;
    //
    return data[getIndex(angle)];
  }
	
	public CommoditySpecAPI getCommodity() {
		return Global.getSettings().getCommoditySpec(COMMODITY_ID);
	}
	
	@Override
	public boolean isUsable() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return false;
		
		return isActive() || fleet.isInHyperspace();
	}
	

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}
	
	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
		
		fleet.getStats().getDetectedRangeMod().unmodify(getModId());
		//data = null;
	}

  public float getRingRadius() {
		return getFleet().getRadius() + 25f;
	}
  
  ///////////
  
	transient private SpriteAPI texture;
	@Override
	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		
		//if (data == null) return;
		
		float level = getProgressFraction();
		if (level <= 0) return;
		if (getFleet() == null) return;
		if (!getFleet().isPlayerFleet()) return;
		
		float alphaMult = viewport.getAlphaMult() * level;
		
//		float x = getFleet().getLocation().x;
//		float y = getFleet().getLocation().y;
//		
//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		Misc.renderQuad(30, 30, 100, 100, Color.green, alphaMult * level);
//		
//		
//		GL11.glPopMatrix();
	
		
		//float noiseLevel = data.getNoiseLevel();
		
		float bandWidthInTexture = 256;
		float bandIndex;
		
		float radStart = getRingRadius();
		float radEnd = radStart + 50f;
		
		float circ = (float) (Math.PI * 2f * (radStart + radEnd) / 2f);
		//float pixelsPerSegment = 10f;
		float pixelsPerSegment = circ / 360f;
		//float pixelsPerSegment = circ / 720;
		float segments = Math.round(circ / pixelsPerSegment);
		
//		segments = 360;
//		pixelsPerSegment = circ / segments;
		//pixelsPerSegment = 10f;
		
		float startRad = (float) Math.toRadians(0);
		float endRad = (float) Math.toRadians(360f);
		float spanRad = Math.abs(endRad - startRad);
		float anglePerSegment = spanRad / segments;
		
		Vector2f loc = getFleet().getLocation();
		float x = loc.x;
		float y = loc.y;

		
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		
		//float zoom = viewport.getViewMult();
		//GL11.glScalef(zoom, zoom, 1);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		if (texture == null) texture = Global.getSettings().getSprite("abilities", "neutrino_detector");
		texture.bindTexture();
		
		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		boolean outlineMode = false;
		//outlineMode = true;
		if (outlineMode) {
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
		
		float thickness = (radEnd - radStart) * 1f;
		float radius = radStart;

		float texProgress = 0f;
		float texHeight = texture.getTextureHeight();
		float imageHeight = texture.getHeight();
		float texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness;
		
		texPerSegment *= 1f;
		
		float totalTex = Math.max(1f, Math.round(texPerSegment * segments));
		texPerSegment = totalTex / segments;
		
		float texWidth = texture.getTextureWidth();
		float imageWidth = texture.getWidth();
		
		
		
		Color color = new Color(234,214,124,255);
		//Color color = new Color(255,25,255,155);
		
		
		for (int iter = 0; iter < 2; iter++) {
			if (iter == 0) {
				bandIndex = 1;
			} else {
				//color = new Color(255,215,25,255);
				//color = new Color(25,255,215,255);
				bandIndex = 0;
				texProgress = segments/2f * texPerSegment;
				//GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			if (iter == 1) {
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			//bandIndex = 1;
			
			float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
			float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;
			
			GL11.glBegin(GL11.GL_QUAD_STRIP);
			for (float i = 0; i < segments + 1; i++) {
				
				float segIndex = i % (int) segments;
				
				//float phaseAngleRad = (float) Math.toRadians(phaseAngle + segIndex * 10) + (segIndex * anglePerSegment * 10f);
				float phaseAngleRad;
				if (iter == 0) {
					phaseAngleRad = (float) Math.toRadians(phaseAngle) + (segIndex * anglePerSegment * 29f);
				} else { //if (iter == 1) { 
					phaseAngleRad = (float) Math.toRadians(-phaseAngle) + (segIndex * anglePerSegment * 17f);
				}
				
				
				float angle = (float) Math.toDegrees(segIndex * anglePerSegment);
				//if (iter == 1) angle += 180;
				
				
				float pulseSin = (float) Math.sin(phaseAngleRad);
				float pulseMax = thickness * 0.5f;

				pulseMax = thickness * 0.2f;
				pulseMax = 10f;
				
				//pulseMax *= 0.25f + 0.75f * noiseLevel;
				
				float pulseAmount = pulseSin * pulseMax;
				//float pulseInner = pulseAmount * 0.1f;
				float pulseInner = pulseAmount * 0.1f;
				
				float r = radius;

//				float thicknessMult = delegate.getAuroraThicknessMult(angle);
//				float thicknessFlat = delegate.getAuroraThicknessFlat(angle);
				
				float theta = anglePerSegment * segIndex;;
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);
				
				float rInner = r - pulseInner;
				//if (rInner < r * 0.9f) rInner = r * 0.9f;
				
				//float rOuter = (r + thickness * thicknessMult - pulseAmount + thicknessFlat);
				float rOuter = r + thickness - pulseAmount;
				
				
				//rOuter += noiseLevel * 25f;
				
				float grav = getFieldStrengthAt(angle);
				//if (grav > 500) System.out.println(grav);
				//if (grav > 300) grav = 300;
				if (grav > 1000f) grav = 1000f; // 750f;
				grav *= 250f / 750f;
				grav *= level;
				//grav *= 0.5f;
				//rInner -= grav * 0.25f;
				
				//rInner -= grav * 0.1f;
				rOuter += grav;
//				rInner -= grav * 3f;
//				rOuter -= grav * 3f;
				//System.out.println(grav);
				
				float alpha = alphaMult;
				alpha *= 0.10f + Math.min(grav / 100, 0.90f);
				//alpha *= 0.75f;
				
//			
//				
//				
//				phaseAngleWarp = (float) Math.toRadians(phaseAngle - 180 * iter) + (segIndex * anglePerSegment * 1f);
//				float warpSin = (float) Math.sin(phaseAngleWarp);
//				rInner += thickness * 0.5f * warpSin;
//				rOuter += thickness * 0.5f * warpSin;
				
				
				
				float x1 = cos * rInner;
				float y1 = sin * rInner;
				float x2 = cos * rOuter;
				float y2 = sin * rOuter;
				
				x2 += (float) (Math.cos(phaseAngleRad) * pixelsPerSegment * 0.33f);
				y2 += (float) (Math.sin(phaseAngleRad) * pixelsPerSegment * 0.33f);
				
				
				GL11.glColor4ub((byte)color.getRed(),
						(byte)color.getGreen(),
						(byte)color.getBlue(),
						(byte)((float) color.getAlpha() * alphaMult * alpha));
				
				GL11.glTexCoord2f(leftTX, texProgress);
				GL11.glVertex2f(x1, y1);
				GL11.glTexCoord2f(rightTX, texProgress);
				GL11.glVertex2f(x2, y2);
				
				texProgress += texPerSegment * 1f;
			}
			GL11.glEnd();
			
			//GL11.glRotatef(180, 0, 0, 1);
		}
		GL11.glPopMatrix();
		
		if (outlineMode) {
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}
	


	
	
	
	
}





