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
	private int _fuite;
	
	public WarExplorerBrainController() {
		super();
		ctask = getFoodTask;
		this._fuite = 0;
	}

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
				return null;
			}
			
			for (WarAgentPercept wp : me.getPerceptsEnemies()) {
        		
                if (!wp.getType().equals(WarAgentType.WarExplorer) && !wp.getType().equals(WarAgentType.WarFood)
                		&& !wp.getType().equals(WarAgentType.WarEngineer)) {
                	me.ctask = escape;
                	me._fuite = 6;
                	me.setHeading(me.getHeading() + 180);
                	return ACTION_MOVE;
                }
			}

			//me.setDebugString("Searching food");

			ArrayList<WarAgentPercept> foodPercepts = (ArrayList<WarAgentPercept>) me
					.getPercepts();

			// Si il y a des percepts
			if (foodPercepts != null && foodPercepts.size() > 0) {
				//Nourriture la plus proche normalement
				WarAgentPercept foodP = null;
				for (WarAgentPercept war : foodPercepts) {
					if (war.getType() == WarAgentType.WarFood && foodP == null)
						foodP = war;
				}
				
				//Si on trouve de la nourriture
				if (foodP != null) {
					me.broadcastMessageToAgentType(WarAgentType.WarExplorer, "I get food !", "");
					me.broadcastMessageToAgentType(WarAgentType.WarLight, "I get food !", "");
					if (foodP.getDistance() > WarResource.MAX_DISTANCE_TAKE) {
						me.setHeading(foodP.getAngle());
						return (MovableWarAgent.ACTION_MOVE);
					}	
					else return (MovableWarAgent.ACTION_TAKE);
				} 
				//Si on ne trouve pas de nourrriture mais si un autre agent en a trouvé
				else if(me.getMessageAboutFood() != null){
					me.setHeading(me.getMessageAboutFood().getAngle());
					return (MovableWarAgent.ACTION_MOVE);
				}
				else {
					return (MovableWarAgent.ACTION_MOVE);
				}
			} 
			//Pas de percepts mais d'autres agents ont trouvé de la nourriture
			else if(me.getMessageAboutFood() != null){
				me.setHeading(me.getMessageAboutFood().getAngle());
				return (MovableWarAgent.ACTION_MOVE);
			}
			else return (MovableWarAgent.ACTION_MOVE);
		}
	};
	
	static WTask escape = new WTask() {
		String exec(WarBrain bc) {
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			
			me._fuite--;
			if(me._fuite == 0) {
				me.setHeading(me.getHeading() + 90);
				me.ctask = getFoodTask;
			}
			
			if(me.isBlocked())
				me.setRandomHeading();
			return ACTION_MOVE;
		}
	};

	@Override
	public String action() {

		for (WarAgentPercept wp : getPerceptsEnemies()) {
    		
            if (wp.getType().equals(WarAgentType.WarBase)){
            	String ang = "";
    			ang += wp.getAngle();
    			String dis = "";
    			dis += wp.getDistance();
    		
    			String[] content = new String[]{ang, dis};
    			
            	broadcastMessageToAll("Base found !", content);
            }
		}
		

		String toReturn = ctask.exec(this); // le run de la FSM

		if (toReturn == null) {
			if (isBlocked())
				setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			if(isBlocked())
				setRandomHeading();
			return toReturn;
		}
	}

	private WarMessage getMessageAboutFood() {
		if(!this.getMessages().isEmpty()){
			WarMessage message = this.getMessages().get(0);
			for( WarMessage wm : this.getMessages()){
				if(wm.getMessage().equals("I get food !") && wm.getDistance() <= message.getDistance()){
					message = wm;
				}
			}
			if(message.getMessage().equals("I get food !")){
				return message;
			}
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
}
