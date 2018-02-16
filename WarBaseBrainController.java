package feelsgoodman;

import java.util.ArrayList;

import edu.warbot.agents.agents.WarBase;
import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarBaseBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarBaseBrainController extends WarBaseBrain {

	WTask ctask;
	
    private boolean _alreadyCreatedLight;  //1ere phase de prod
    private boolean _alreadyCreatedEngi;	//2e phase de prod
    private boolean _alreadyCreatedDouble;	//3e phase de prod
    private boolean _inDanger;			//Base en danger
    private WarAgentType _cible; //Type de menace en cas de danger
    private double _angleRocket;
    private boolean _defCreated; //Défenseur rapide créé
    private boolean _help; //Messages d'aide envoyés
    private int _timer;		//Attente d'une reponse
    private int _rocketTimer;
    private int _engisDone;		//Ingénieurs ayant fini leur travail
    
  //Comptes des productions
    private int _explos;
    private int _lights;
    private int _engis;
    private int _double;
    

    public WarBaseBrainController() {
        super();

        _alreadyCreatedLight = false;
        _alreadyCreatedEngi = false;
        _alreadyCreatedDouble = false;
        _inDanger = false;
        _defCreated = false;
        _angleRocket = 0;
        _help = false;
        _explos = 0;
        _lights = 0;
        _engis = 0;
        _double = 0;
        _cible = null;
        _timer = 0;
        _engisDone = 0;
        _rocketTimer = 0;
        
        ctask = createPatrouille;
    }

    static WTask handleMsgs = new WTask() {
		String exec(WarBrain bc) {
			return "";
		}
	};
	
	static WTask createPatrouille = new WTask() {
    	String exec(WarBrain bc){
    		WarBaseBrainController me = (WarBaseBrainController) bc;
    		
    		WarAgentType typeAnti = me.alarme();
    		
    		if(typeAnti != null){
    			me.ctask = danger;
    			return ACTION_EAT;
    		}
    		
    		if (!me._alreadyCreatedLight) {
    			if(me._explos < 4){
    				me.setNextAgentToCreate(WarAgentType.WarExplorer);
                    me._explos++;
    			}
    			else{
    				me.setNextAgentToCreate(WarAgentType.WarLight);
    				me._lights++;
    			}
    			
                if(me._lights >= 8) me._alreadyCreatedLight = true;
                return WarBase.ACTION_CREATE;
            }
    		
    		//Si on a fini de créer la patrouille
    		else {
    			me.ctask = eat;
    			return(WarBase.ACTION_EAT);
    		}
    	}
    };
    
    static WTask createEngi = new WTask() {
    	String exec(WarBrain bc){
    		WarBaseBrainController me = (WarBaseBrainController) bc;
    		
    		me.broadcastMessageToAgentType(WarAgentType.WarEngineer, "finish?", "");
    		
    		WarAgentType typeAnti = me.alarme();
    		
    		if(typeAnti != null){
    			me.ctask = danger;
    			return ACTION_EAT;
    		}
    		
    		if (!me._alreadyCreatedEngi) {
    			
    				me.setNextAgentToCreate(WarAgentType.WarEngineer);
    				me._engis++;
    			
                if(me._engis >= 3) me._alreadyCreatedEngi = true;
                return WarBase.ACTION_CREATE;
            }
    		
    		//Si on a fini de créer les ingénieurs
    		else {
    			me.ctask = eat;
    			return(WarBase.ACTION_EAT);
    		}
    	}
    };
    
    static WTask createDouble = new WTask() {
    	String exec(WarBrain bc){
    		WarBaseBrainController me = (WarBaseBrainController) bc;
    		
    		me.broadcastMessageToAgentType(WarAgentType.WarEngineer, "finish?", "");
    		
    		WarAgentType typeAnti = me.alarme();
    		
    		if(typeAnti != null){
    			me.ctask = danger;
    			return ACTION_EAT;
    		}
    		
    		if (!me._alreadyCreatedDouble) {
    			
    			if(me._double < 2){	
    				me.setNextAgentToCreate(WarAgentType.WarHeavy);
    				me._double++;
    			}
    			else{
    				me.setNextAgentToCreate(WarAgentType.WarRocketLauncher);
    				me._double++;
    			}
    			
                if(me._double >= 4) me._alreadyCreatedDouble = true;
                return WarBase.ACTION_CREATE;
            }
    		
    		//Si on a fini de créer les ingénieurs
    		else {
    			me.ctask = eat;
    			return(WarBase.ACTION_EAT);
    		}
    	}
    };
    
    static WTask eat = new WTask() {
    	String exec(WarBrain bc){
    		WarBaseBrainController me = (WarBaseBrainController) bc;
    		
    		if(me._alreadyCreatedEngi) me.broadcastMessageToAgentType(WarAgentType.WarLight, "fin de patrouille", "");
    		
    		WarAgentType typeAnti = me.alarme();
    		
    		if(typeAnti != null){
    			me.ctask = danger;
    			return ACTION_EAT;
    		}
    		
    		if(me.getHealth() == me.getMaxHealth()) {
    			if(me._alreadyCreatedDouble){
    				me._double = 0;
    				me._alreadyCreatedDouble = false;
    				me.ctask = createDouble;
    			}
    			else if(me._alreadyCreatedEngi) me.ctask = createDouble;
    			else if(me._alreadyCreatedLight) me.ctask = createEngi;
    			
    			return WarBase.ACTION_IDLE;
    		}
    		else {
    			return WarBase.ACTION_EAT;
    		}
    	}
    };
    
    static WTask danger = new WTask() {
    	String exec(WarBrain bc){
    		WarBaseBrainController me = (WarBaseBrainController) bc;
    		
    		ArrayList<WarAgentPercept> heavyPercepts = (ArrayList<WarAgentPercept>) 
        			me.getPerceptsEnemiesByType(WarAgentType.WarHeavy);
    		
    		if(!heavyPercepts.isEmpty()) {
    			String ang = "";
    			ang += heavyPercepts.get(0).getAngle();
    			String dis = "";
    			dis += heavyPercepts.get(0).getDistance();
    			me.setDebugString("" + heavyPercepts.get(0).getDistance());
    		
    			String[] content = new String[]{dis, ang};
    			
    			me.broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, "danger here", content);
    		}
    		
    		ArrayList<WarAgentPercept> lightPercepts = (ArrayList<WarAgentPercept>) 
        			me.getPerceptsEnemiesByType(WarAgentType.WarLight);
    		
    		if(!lightPercepts.isEmpty()) {
    			String ang = "";
    			ang += lightPercepts.get(0).getAngle();
    			String dis = "";
    			dis += lightPercepts.get(0).getDistance();
    			me.setDebugString("" + lightPercepts.get(0).getDistance());
    		
    			String[] content = new String[]{dis, ang};
    			
    			me.broadcastMessageToAgentType(WarAgentType.WarHeavy, "danger here", content);
    		}
    		
    		ArrayList<WarAgentPercept> rocketPercepts = (ArrayList<WarAgentPercept>) 
        			me.getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
    		
    		if(!rocketPercepts.isEmpty()) {
    			String ang = "";
    			ang += rocketPercepts.get(0).getAngle();
    			String dis = "";
    			dis += rocketPercepts.get(0).getDistance();
    			me.setDebugString("" + rocketPercepts.get(0).getDistance());
    		
    			String[] content = new String[]{dis, ang};
    			me.broadcastMessageToAgentType(WarAgentType.WarLight, "danger here", content);
    		}
    		
    		//Possible doublon de liste
    		ArrayList<WarAgentPercept> ciblePercepts = (ArrayList<WarAgentPercept>) 
        			me.getPerceptsEnemiesByType(me._cible);
    		
    		
    		
    		if(me._help){
    			me._timer--;
    			for(WarMessage m : me.getMessages()){
    				if(m.getMessage().equals("I am coming now")){
    					me._defCreated = true;
    				}
    			}
    			
    			if(!me._defCreated && me._timer <= 0){
    				me._defCreated = true;
    				if(me._cible.equals(WarAgentType.WarLight))
    					me.setNextAgentToCreate(WarAgentType.WarHeavy);
        			else if(me._cible.equals(WarAgentType.WarRocketLauncher))
        				me.setNextAgentToCreate(WarAgentType.WarLight);
        			else if(me._cible.equals(WarAgentType.WarHeavy))
        				me.setNextAgentToCreate(WarAgentType.WarRocketLauncher);
        			else
        				me.setNextAgentToCreate(WarAgentType.WarHeavy);

                    return WarBase.ACTION_CREATE;
    			}
    		}
    		else me._timer = 5;
    		
    		if(!ciblePercepts.isEmpty()){
    			
    			me.setDebugString("Danger !");
    			
    			String ang = "";
    			ang += ciblePercepts.get(0).getAngle();
    			String dis = "";
    			dis += ciblePercepts.get(0).getDistance();
    			me.setDebugString("" + ciblePercepts.get(0).getDistance());
    		
    			String[] content = new String[]{dis, ang};
    		
    			if(me._cible.equals(WarAgentType.WarLight))
    				me.broadcastMessageToAgentType(WarAgentType.WarHeavy, "help me now", content);
    			else if(me._cible.equals(WarAgentType.WarRocketLauncher))
    				me.broadcastMessageToAgentType(WarAgentType.WarLight, "help me now", content);
    			else if(me._cible.equals(WarAgentType.WarHeavy))
    				me.broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, "help me now", content);
    			else
    				me.broadcastMessageToAgentType(WarAgentType.WarHeavy, "help me now", content);
    			
    			me._help = true;
    			
    			for(WarMessage m : me.getMessages()){
        			if(m.getMessage().equals("j'y suis")){
        				if(me.defCompatible(m.getSenderType(), me._cible))
        					me.reply(m, "là", content);
        				else if(m.getSenderType().equals(WarAgentType.WarLight)){
        					me._cible = WarAgentType.WarRocketLauncher;
        					return ACTION_EAT;
        				}
        				else if(m.getSenderType().equals(WarAgentType.WarRocketLauncher)){
        					me._cible = WarAgentType.WarHeavy;
        					return ACTION_EAT;
        				}
        				else if(m.getSenderType().equals(WarAgentType.WarHeavy)){
        					me._cible = WarAgentType.WarLight;
        					return ACTION_EAT;
        				}
        			}
        		}
    		}
    		else{
    			WarAgentType test = me.alarme();
    			if(me._angleRocket != 0){
    				System.out.println("help/ " + me._rocketTimer);
    				me._help = true;
    				String[] content = {""+150, ""+me._angleRocket};
    				me.broadcastMessageToAgentType(WarAgentType.WarLight, "help me now", content);
    				return ACTION_EAT;
    			}
    			else if(test == null){
    				me.broadcastMessageToAll("I am safe", "");
    				me.ctask = eat;
    				me.setDebugString("Safe !");
    				me._help = false;
    				me._defCreated = false;
    				return ACTION_EAT;
    			}
    		}
    		
    		System.out.println("eat2");
    		return ACTION_EAT;
    	}
    };
    
    public WarAgentType alarme(){
    
    	if(_rocketTimer <= 0)
    		this._angleRocket = 0;
    	else
    		_rocketTimer--;
    	
    	ArrayList<WarAgentPercept> enemiesPercepts = (ArrayList<WarAgentPercept>) 
    			this.getPerceptsEnemies();
    	
    	if(!enemiesPercepts.isEmpty()){
			
			int enemiesL = 0;
			int enemiesH = 0;
			int enemiesR = 0;
			int enemiesRocket = 0;
			
			for(WarAgentPercept wp : enemiesPercepts){
				if(!wp.getType().equals(WarAgentType.WarExplorer)){
					this._inDanger = true;
					switch(wp.getType()){
						case WarLight : enemiesL++; break;
						case WarHeavy : enemiesH++; break;
						case WarRocketLauncher : enemiesR++; break;
						case WarRocket : enemiesRocket++; this._angleRocket = wp.getAngle(); break;
						default : break;
					}
				}
			}
			
			//Si on détecte un attaquant -> en danger !
			if(this._inDanger){
				
				if(enemiesL > enemiesH){
					if(enemiesL > enemiesR){ //Si plus de light -> creer Heavy
						this.setCible(WarAgentType.WarLight);
						return WarAgentType.WarHeavy;
					}
					else{ //Si plus de RocketLauncher -> creer Light
						this.setCible(WarAgentType.WarRocketLauncher);
						return WarAgentType.WarLight;
					}
				}
				else if(enemiesH > enemiesR){ //Si plus de Heavy -> creer RocketLauncher
					this.setCible(WarAgentType.WarHeavy);
					return WarAgentType.WarRocketLauncher;
				}
				else if(enemiesR > enemiesH){ //Si plus de RocketLauncher -> creer light
					this.setCible(WarAgentType.WarRocketLauncher);
					return WarAgentType.WarLight;
				}
				else if(enemiesRocket > 0){ //Si égalité -> vérifier si des roquettes sont détéctées ou creer par defaut Heavy
					if(_rocketTimer == 1 || _rocketTimer == 0) _rocketTimer = 10;
					this.setCible(WarAgentType.WarRocketLauncher);
					return WarAgentType.WarLight;
				}
				else{
					this.setCible(null);
					return WarAgentType.WarHeavy;
				}
			}
		}
    	
    	
    	
    	return null;
    }
    
    public boolean defCompatible(WarAgentType def, WarAgentType enem){
    	if(enem.equals(WarAgentType.WarLight) && def.equals(WarAgentType.WarHeavy)) return true;
    	else if(enem.equals(WarAgentType.WarHeavy) && def.equals(WarAgentType.WarRocketLauncher)) return true;
    	else if(enem.equals(WarAgentType.WarRocketLauncher) && def.equals(WarAgentType.WarLight)) return true;
    	
    	return false;
    }

    @Override
    public String action() {
    	
        for (WarMessage message : getMessages()) {
            if (message.getMessage().equals("Where are you?"))
                reply(message, "I'm here");
            if(message.getMessage().equals("I'm done"))
            	_engisDone++;
        }
        
        if(_engisDone >= 3) {
        	broadcastMessageToAgentType(WarAgentType.WarLight, "Patrouille done", "");
        	broadcastMessageToAll("Attack!", "");
        }
        
        
    	String toReturn = ctask.exec(this);

    	if (toReturn == null) {
			return WarExplorer.ACTION_IDLE;
		} else {
			return toReturn;
		}
    }
    
    public void setCible(WarAgentType c){
    	this._cible = c;
    }

}
