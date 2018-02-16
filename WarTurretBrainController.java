package feelsgoodman;

import java.util.List;

import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.brains.WarTurretBrain;

public abstract class WarTurretBrainController extends WarTurretBrain {

    private int _sight;

    public WarTurretBrainController() {
        super();

        _sight = 0;
    }

    @Override
    public String action() {
    	
    	if(getHealth() <= 2000)
    		broadcastMessageToAgentType(WarAgentType.WarEngineer, "I need repair", "");
    	
    	
        List <WarAgentPercept> percepts = getPerceptsEnemies();
        
        if(!percepts.isEmpty()){
        	for (WarAgentPercept p : percepts) {
            	if(!p.getType().equals(WarAgentType.WarFood)){
            		sendMessages(p);
                    return attack(p);
            	}
            }
        }
        
        _sight += 90;
        if (_sight == 360) {
            _sight = 0;
        }
        setHeading(_sight);
        
        return ACTION_IDLE;
    }
    
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
			System.out.println("engi");
			distancePrevision = 1.0 * timing;
		}
		
		//Viser là où sera l'enemi
		Vector2 v = new Vector2();
		Double[] d = v.getDist(enemi.getDistance(), enemi.getAngle(), distancePrevision, enemi.getHeading());
		this.setHeading(d[1]);

		if (this.isReloaded()){
			return ACTION_FIRE;
		}
			
		else if (this.isReloading()){
			return ACTION_IDLE;
		}
			
		else{
			return ACTION_RELOAD;
		}
			
	}
    
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
}
