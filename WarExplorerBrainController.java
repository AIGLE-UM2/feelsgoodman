package feelsgoodman;

import java.util.ArrayList;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarResource;
import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarExplorerBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarExplorerBrainController extends WarExplorerBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask() {
		String exec(WarBrain bc) {
			return "";
		}
	};

	static WTask returnFoodTask = new WTask() {
		String exec(WarBrain bc) {
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			if (me.isBagEmpty()) {
				me.setHeading(me.getHeading() + 180);

				me.ctask = getFoodTask;
				return (null);
			}

			me.setDebugString("Returning Food");

			if (me.isBlocked())
				me.setRandomHeading();

			ArrayList<WarAgentPercept> basePercepts = (ArrayList<WarAgentPercept>) me
					.getPerceptsAlliesByType(WarAgentType.WarBase);

			// Si je ne vois pas de base
			if (basePercepts == null | basePercepts.size() == 0) {

				WarMessage m = me.getMessageFromBase();
				// Si j'ai un message de la base je vais vers elle
				if (m != null)
					me.setHeading(m.getAngle());

				// j'envoie un message aux bases pour savoir où elle sont..
				me.broadcastMessageToAgentType(WarAgentType.WarBase,
						"Where are you?", (String[]) null);

				return (MovableWarAgent.ACTION_MOVE);

			} else {// si je vois une base
				WarAgentPercept base = basePercepts.get(0);

				if (base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE) {
					me.setHeading(base.getAngle());
					return (MovableWarAgent.ACTION_MOVE);
				} else {
					me.setIdNextAgentToGive(base.getID());
					return (MovableWarAgent.ACTION_GIVE);
				}

			}

		}
	};

	static WTask getFoodTask = new WTask() {
		String exec(WarBrain bc) {
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			if (me.isBagFull()) {

				me.ctask = returnFoodTask;
				return (null);
			}

			if (me.isBlocked())
				me.setRandomHeading();

			me.setDebugString("Searching food");

			ArrayList<WarAgentPercept> foodPercepts = (ArrayList<WarAgentPercept>) me
					.getPercepts();

			// Si il y a de la nouriture
			if (foodPercepts != null && foodPercepts.size() > 0) {
				// WarAgentPercept foodP = foodPercepts.get(0); //le 0 est le
				// plus proche normalement
				WarAgentPercept foodP = null;
				for (WarAgentPercept war : foodPercepts) {
					if (war.getType() == WarAgentType.WarFood && foodP == null)
						foodP = war;
				}

				if (foodP != null) {
					if (foodP.getDistance() > WarResource.MAX_DISTANCE_TAKE) {
						me.setHeading(foodP.getAngle());
						return (MovableWarAgent.ACTION_MOVE);
					} else {
						return (MovableWarAgent.ACTION_TAKE);
					}
				} else {
					return (MovableWarAgent.ACTION_MOVE);
				}
			} else {
				return (MovableWarAgent.ACTION_MOVE);
			}
		}
	};

	public WarExplorerBrainController() {
		super();
		ctask = getFoodTask; // initialisation de la FSM
	}

	@Override
	public String action() {

		// Develop behaviour here

		String toReturn = ctask.exec(this); // le run de la FSM

		if (toReturn == null) {
			if (isBlocked())
				setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}

	private WarMessage getMessageAboutFood() {
		for (WarMessage m : getMessages()) {
			if (m.getMessage().equals("foodHere"))
				return m;
		}
		return null;
	}

	private WarMessage getMessageFromBase() {
		for (WarMessage m : getMessages()) {
			if (m.getSenderType().equals(WarAgentType.WarBase))
				return m;
		}

		broadcastMessageToAgentType(WarAgentType.WarBase, "Where are you?", "");
		return null;
	}

	/*
	 * public WarExplorerBrainController() { super();
	 * 
	 * }
	 * 
	 * /*@Override public String action() {
	 * 
	 * if (isBlocked()) setRandomHeading(); else if(!isBagFull()){
	 * List<WarAgentPercept> foods= getPercepts(); if(!foods.isEmpty()){ double
	 * dist = 10000;
	 * 
	 * //Trouve la plus proche for(WarAgentPercept wap : foods){
	 * if(wap.getType() == WarAgentType.WarFood && dist > wap.getDistance()){
	 * dist = wap.getDistance(); if(dist <= WarFood.MAX_DISTANCE_TAKE) return
	 * take(); setHeading(wap.getAngle()); } }
	 * 
	 * } } else setDebugString("I'm full !"); return WarExplorer.ACTION_MOVE; }
	 */

}
