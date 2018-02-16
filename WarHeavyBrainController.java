package feelsgoodman;

import java.util.ArrayList;

import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarHeavyBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarHeavyBrainController extends  WarHeavyBrain {

	WTask ctask;
	private double _dist;
	private int _defTimer;
	
    public WarHeavyBrainController() {
        super();
        ctask = huntTask;
        _dist = 0;
        _defTimer = 0;
    }

    static WTask defTask = new WTask(){
		String exec(WarBrain bc){
			WarHeavyBrainController me = (WarHeavyBrainController) bc;
				
			for(WarMessage m : me.getMessages()){
				if(m.getMessage().equals("I am safe")){
					me.ctask = huntTask;
					me.setHeading(me.getHeading() + 180);
					return ACTION_MOVE;
				}
				if(m.getMessage().equals("danger here")) {
						
					me.setHeading(me.getIndirectPositionOfAgentWithMessage(m).getAngle());
				}
			}
				
			for (WarAgentPercept wp : me.getPerceptsEnemies()) {
                if (wp.getType().equals(WarAgentType.WarLight)) {

                	return me.attack(wp);
                }
            }
			
			
        	if (me.isBlocked())
                me.setRandomHeading();
        	return ACTION_MOVE;
		}
	};
	
	static WTask godefTask = new WTask(){
		String exec(WarBrain bc){
			WarHeavyBrainController me = (WarHeavyBrainController) bc;
			
			for(WarMessage m : me.getMessages()){
				if(m.getMessage().equals("I am safe")){ 
					me.ctask = huntTask;
					me.setHeading(me.getHeading() + 180);
					return ACTION_MOVE;
				}
				if(m.getMessage().equals("danger here")) {
					
					me.setDebugString("coucou " + me.getIndirectPositionOfAgentWithMessage(m).getAngle());
					
					me.setHeading(me.getIndirectPositionOfAgentWithMessage(m).getAngle());
					if(me.getIndirectPositionOfAgentWithMessage(m).getDistance() < 100) {
						me.broadcastMessageToAgentType(WarAgentType.WarBase, "j'y suis", "");
						me.ctask = defTask;
					}
				}
			}
			
			if (me.isBlocked())
                me.setRandomHeading();
			
			me.setDebugString("si");
			return ACTION_MOVE;
		}
	};
	
	static WTask huntTask = new WTask(){
		String exec(WarBrain bc){
			WarHeavyBrainController me = (WarHeavyBrainController) bc;
			
			for (WarAgentPercept wp : me.getPerceptsEnemies()) {
				
				if(wp.getType().equals(WarAgentType.WarBase)){
					
					String ang = "";
	    			ang += wp.getAngle();
	    			String dis = "";
	    			dis += wp.getDistance();
	    		
	    			String[] content = new String[]{ang, dis};
					me.broadcastMessageToAll("Base found !", content);
				}
				if (!wp.getType().equals(WarAgentType.WarBase) && !wp.getType().equals(WarAgentType.WarFood)) {
					if(wp.getType().equals(WarAgentType.WarRocketLauncher)){
						
						String ang = "";
		    			ang += wp.getAngle();
		    			String dis = "";
		    			dis += wp.getDistance();
		    		
		    			String[] content = new String[]{ang, dis};
						
						if(wp.getType().equals(WarAgentType.WarRocketLauncher)){
							me.broadcastMessageToAll("rocket enemi here", content);
						}
		    			
						me.setHeading(me.getHeading() - 60);
					}
					else{
						return me.attack(wp);
					}
				}
			}
			
			for(WarMessage m : me.getMessages()){
				if(m.getMessage().equals("help me now") && m.getDistance() < 200){
					me.reply(m, "I am coming now", "");
					me._dist = m.getDistance();
					me.ctask = godefTask;
					me.setHeading(m.getAngle());
					return ACTION_IDLE;
				}
				else if(m.getMessage().equals("help me now") && m.getDistance() < 300){
					me.reply(m, "I am coming", "");
					me._dist = m.getDistance();
					me.ctask = godefTask;
					me.setHeading(m.getAngle());
				}
				else if(m.getMessage().equals("turret enemi here") && m.getDistance() < 500) {
					me.setHeading(m.getAngle());
					return ACTION_MOVE;
				}
				else if(m.getMessage().equals("light enemi here") && m.getDistance() < 200) {
					me.setHeading(m.getAngle());
				}
				else if(m.getMessage().equals("Attack!")) {
					me.ctask = finalAssaultTask;
				}
			}
			
			
			if (me.isBlocked())
				me.setRandomHeading();
        	return ACTION_MOVE;
		}
	};
	
	static WTask finalAssaultTask = new WTask() {
		String exec(WarBrain bc) {
			WarHeavyBrainController me = (WarHeavyBrainController) bc;
			
			for (WarAgentPercept wp : me.getPerceptsEnemies()) {
				if(wp.getType().equals(WarAgentType.WarBase)){
					
					String ang = "";
	    			ang += wp.getAngle();
	    			String dis = "";
	    			dis += wp.getDistance();
	    		
	    			String[] content = new String[]{dis, ang};
					me.broadcastMessageToAll("Base found !", content);
					
					ArrayList<WarAgentPercept> alliesPercepts = (ArrayList<WarAgentPercept>) me.getPerceptsAllies();
					
					/*if(!alliesPercepts.isEmpty()){
						for(WarAgentPercept wpAllie : alliesPercepts){
							if(me.getHeading() == wpAllie.getAngle()){
								System.out.println("ne tire pas");
								return ACTION_IDLE;
							}
						}
					}*/
					
					if (me.isReloaded())
                        return ACTION_FIRE;
                    else if (me.isReloading()) {
                    	me.setHeading(me.getHeading() + 60);
                    	return ACTION_IDLE;
                    }
                    else
                        return ACTION_RELOAD;
				}
				
				if (!wp.getType().equals(WarAgentType.WarBase) && !wp.getType().equals(WarAgentType.WarFood)) {
					if(wp.getType().equals(WarAgentType.WarHeavy) || wp.getType().equals(WarAgentType.WarTurret)){
						
						String ang = "";
		    			ang += wp.getAngle();
		    			String dis = "";
		    			dis += wp.getDistance();
		    		
		    			String[] content = new String[]{ang, dis};
						
						if(wp.getType().equals(WarAgentType.WarTurret)){
							me.broadcastMessageToAll("turret enemi here", content);
						}
						else me.broadcastMessageToAll("heavy enemi here", content);
		    			
						
						me.setHeading(me.getHeading() - 60);
					}
					else{
						return me.attack(wp);
					}
				}
			}
			
			for(WarMessage m : me.getMessages()) {
				if(m.getMessage().equals("Base found !")) {
					me.setHeading(m.getAngle() + 10);
				}
			}
			
			if (me.isBlocked())
				me.setRandomHeading();
        	return ACTION_MOVE;
		}
	};
	
	public String attack(WarAgentPercept enemi){

		//Combien de tick avant de toucher l'enemi
		int timing = (int) enemi.getDistance()/10;
		if(enemi.getDistance()%10 > 0) timing++;

		//Determine la distance parcourue par l'enemi avant que le tire touche
		double distancePrevision = 0;
		if(enemi.getType().equals(WarAgentType.WarLight)){
			distancePrevision = 1.8 * timing;
		}
		else if(enemi.getType().equals(WarAgentType.WarHeavy)){
			distancePrevision = 0.8 * timing;
		}
		else if(enemi.getType().equals(WarAgentType.WarRocketLauncher)){
			distancePrevision = 1.0 * timing;
		}
		else if(enemi.getType().equals(WarAgentType.WarKamikaze)){
			distancePrevision = 1.0 * timing;
		}
		else if(enemi.getType().equals(WarAgentType.WarExplorer)){
			distancePrevision = 2.0 * timing;
		}
		else if(enemi.getType().equals(WarAgentType.WarEngineer)){
			distancePrevision = 1.0 * timing;
		}
		
		//Viser là où sera l'enemi
		Vector2 v = new Vector2();
		Double[] d = v.getDist(enemi.getDistance(), enemi.getAngle(), distancePrevision, enemi.getHeading());
		this.setHeading(d[1]);

		if (this.isReloaded())
			return ACTION_FIRE;
		else if (this.isReloading())
			return ACTION_MOVE;
		else
			return ACTION_RELOAD;
	}
	
    @Override
    public String action() {
    	
    	String toReturn = ctask.exec(this);

        if(toReturn == null){
        	for (WarAgentPercept wp : getPerceptsEnemies()) {
        		
                if (!wp.getType().equals(WarAgentType.WarBase) && !wp.getType().equals(WarAgentType.WarFood)) {

                    setHeading(wp.getAngle());
                    this.setDebugString("Attaque");
                    if (isReloaded())
                        return ACTION_FIRE;
                    else if (isReloading())
                        return ACTION_IDLE;
                    else
                        return ACTION_RELOAD;
                }
            }
        	if (isBlocked())
                setRandomHeading();
        	return ACTION_MOVE;
        }
        else{
        	if (isBlocked())
                setRandomHeading();
        	return toReturn;
        }
    }

}
