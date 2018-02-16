package feelsgoodman;

import java.util.ArrayList;

import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarEngineerBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarEngineerBrainController extends WarEngineerBrain {

	WTask ctask;
	private double _dist;
	private double _angle;

    public WarEngineerBrainController() {
        super();
        this.ctask = waitWork;
        this._dist = 0;
        this._angle = 0;
    }

    static WTask waitWork = new WTask() {
    	String exec(WarBrain bc){
    		WarEngineerBrainController me = (WarEngineerBrainController) bc;
    		
    		//Il cherche le spot le plus loin
    		for(WarMessage m : me.getMessages()) {
    			if(m.getMessage().equals("spot here") && m.getDistance() > me._dist) {
    				me._dist = m.getDistance();
    				me.setHeading(m.getAngle());
    				me._angle = m.getAngle();
    				
    				//me.reply(m, "I come to work", "");
    			}
    		}
    		
    		if(me._dist > 0){
    			me.ctask = workTask;
    			if (me.isBlocked())
    				me.setRandomHeading();
    			return ACTION_MOVE;
    		}
    		
    		//Tant que personne ne lui donne de travail
    		return ACTION_IDLE;
    	}
    };
    
    static WTask workTask = new WTask() {
    	String exec(WarBrain bc){
    		WarEngineerBrainController me = (WarEngineerBrainController) bc;
    		
    		ArrayList<WarAgentPercept> enemiesPercepts = (ArrayList<WarAgentPercept>) me
					.getPerceptsEnemies();
    		
    		if(!enemiesPercepts.isEmpty()){
    			for(WarAgentPercept wp : enemiesPercepts){
    				me.sendMessages(wp);
    			}
    		}
    		
    		if(me._dist <= 0) {
    			me.setNextBuildingToBuild(WarAgentType.WarTurret);
    			me.ctask = returnTask;
    			me.broadcastMessageToAgentType(WarAgentType.WarBase, "Where are you?", "");
    			return ACTION_BUILD;
    		}
    		else {
    			me.setHeading(me._angle);
    			if (me.isBlocked())
    				me.setRandomHeading();
    			else
    				me._dist -= me.getSpeed();
    			return ACTION_MOVE;
    		}
    	}
    };
    
    static WTask returnTask = new WTask() {
    	String exec(WarBrain bc){
    		WarEngineerBrainController me = (WarEngineerBrainController) bc;
    		
    		ArrayList<WarAgentPercept> enemiesPercepts = (ArrayList<WarAgentPercept>) me
					.getPerceptsEnemies();
    		
    		if(!enemiesPercepts.isEmpty()){
    			for(WarAgentPercept wp : enemiesPercepts){
    				me.sendMessages(wp);
    			}
    		}
    		
    		ArrayList<WarAgentPercept> alliesPercepts = (ArrayList<WarAgentPercept>) me
					.getPerceptsAllies();
    		
    		for(WarMessage m : me.getMessages()) {
    			if(m.getMessage().equals("I'm here")) {
    				me.setHeading(m.getAngle());
    				me._angle = m.getAngle();
    				if (me.isBlocked())
        				me.setRandomHeading();
    				return ACTION_MOVE;
    			}
    		}
    		
    		for(WarAgentPercept wp : alliesPercepts) {
    			if(wp.getType().equals(WarAgentType.WarBase)) {
    				me.broadcastMessageToAgentType(WarAgentType.WarBase, "I'm done", "");
    				me.ctask = repairTask;
    				return ACTION_IDLE;
    			}
    		}
    		
    		me.setHeading(me._angle);
    		return ACTION_MOVE;
    	}
    };
    
    static WTask repairTask = new WTask() {
    	String exec(WarBrain bc){
    		WarEngineerBrainController me = (WarEngineerBrainController) bc;
    		
    		ArrayList<WarAgentPercept> enemiesPercepts = (ArrayList<WarAgentPercept>) me
					.getPerceptsEnemies();
    		
    		if(!enemiesPercepts.isEmpty()){
    			for(WarAgentPercept wp : enemiesPercepts){
    				me.sendMessages(wp);
    			}
    		}
    		
    		ArrayList<WarAgentPercept> alliesPercepts = (ArrayList<WarAgentPercept>) me
					.getPerceptsAllies();
    		
    		for(WarAgentPercept wp : alliesPercepts) {
    			if(wp.getType().equals(WarAgentType.WarTurret)) {
    				if(wp.getHealth() < wp.getMaxHealth()){
    					me.setIdNextBuildingToRepair(wp.getID());
    					return ACTION_REPAIR;
    				}
    				else{
    					me.broadcastMessageToAgentType(WarAgentType.WarBase, "Where are you?", "");
    					me.ctask = returnTask;
    				}
    			}
    		}
    		
    		for(WarMessage m : me.getMessages()) {
    			if(m.getMessage().equals("I need repair") && m.getDistance() < 200) {
    				me.setHeading(m.getAngle());
    				return ACTION_MOVE;
    			}
    		}
    		
    		me.setHeading(me.getHeading() + 150);
    		
    		return ACTION_IDLE;
    	}
    };
    
    public void sendMessages(WarAgentPercept enemi){
    	
    	String ang = "";
		ang += enemi.getAngle();
		String dis = "";
		dis += enemi.getDistance();
		String[] content = new String[]{dis, ang};
    	
    	switch(enemi.getType()){
    		case WarLight:
    			this.broadcastMessageToAll("light enemi here", content);
    			break;
    		case WarRocketLauncher:
    			this.broadcastMessageToAll("rocket enemi here", content);
    			break;
    		case WarHeavy:
    			this.broadcastMessageToAll("heavy enemi here", content);
    			break;
    		case WarExplorer:
    			this.broadcastMessageToAll("explorer enemi here", content);
    			break;
    		case WarEngineer:
    			this.broadcastMessageToAll("engi enemi here", content);
    			break;
    		case WarKamikaze:
    			this.broadcastMessageToAll("kamikaze enemi here", content);
    			break;
    			
    		default:
    			break;
    	}
    }
    
    @Override
    public String action() {

    	String toReturn = ctask.exec(this); // le run de la FSM

		if (toReturn == null) {
			if (isBlocked())
				setRandomHeading();
			return WarExplorer.ACTION_IDLE;
		} else {
			return toReturn;
		}
    }
}
