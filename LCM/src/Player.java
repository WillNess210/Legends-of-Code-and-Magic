import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse the standard input
 * according to the problem statement.
 **/
class Player{

	public static void main(String args[]){
		Scanner in = new Scanner(System.in);
		int gameTurn = 0;
		// game loop
		while(true){
			User[] players = new User[2];
			for(int i = 0; i < 2; i++){
				players[i] = new User(in);
			}
			User myself = players[0];
			int opponentHand = in.nextInt();
			int cardCount = in.nextInt();
			ArrayList<Card> draftCards = new ArrayList<Card>();
			ArrayList<Card> myCreatures = new ArrayList<Card>();
			ArrayList<Card> myItems = new ArrayList<Card>();
			ArrayList<Card> opCreatures = new ArrayList<Card>();
			for(int i = 0; i < cardCount; i++){
				Card newCard = new Card(in);
				draftCards.add(newCard);
				if(newCard.isCreature()){
					if(newCard.getLocation() == -1){
						opCreatures.add(newCard);
					}else{
						myCreatures.add(newCard);
					}
				}else if(newCard.getLocation() >= 0){
					myItems.add(newCard);
				}
			}
			if(gameTurn < 30){
				// DRAFT LOGIC
				float bestScore = -10000000;
				int bestPosition = -1;
				for(int i = 0; i < draftCards.size(); i++){
					float score = -1000;
					Card optionCard = draftCards.get(i);
					if(optionCard.isCreature()){
						score = optionCard.getAttack() + optionCard.getDefense() - (optionCard.getCost() * 2);
						if(optionCard.getAbilities().contains("G")){
							score *= 1.2;
						}
						if(optionCard.getAbilities().contains("B")){
							score *= 1.1;
						}
					}
					if(score > bestScore){
						bestScore = score;
						bestPosition = i;
					}
				}
				System.out.println("PICK " + bestPosition + " picking");
			}else{
				System.err.println("TRYING TO PLAY");
				String command = "";
				// FIRST SUMMON EVERYTHING I CAN
				for(int i = 0; i < myCreatures.size(); i++){
					Card thisCreature = myCreatures.get(i);
					if(thisCreature.isInHand() && thisCreature.getCost() <= myself.getMana()){
						if(command.length() > 0){
							command += ";";
						}
						command += "SUMMON " + thisCreature.getID();
						myself.changeMana(-thisCreature.getCost());
					}
				}
				System.err.println("CP 1");
				// ATTACK EVERYTHING I CAN
				int idToAttack = -1;
				for(int i = 0; i < opCreatures.size(); i++){
					if(opCreatures.get(i).getAbilities().contains("G")){
						idToAttack = opCreatures.get(i).getID();
					}
				}
				System.err.println("CP 2");
				for(int i = 0; i < myCreatures.size(); i++){
					if(myCreatures.get(i).isOnBoard()){
						if(command.length() > 0){
							command += ";";
						}
						command += "ATTACK " + myCreatures.get(i).getID() + " " + idToAttack;
					}
				}
				// SENDING COMMAND
				System.err.println("COMMAND: -" + command + "-");
				if(command.isEmpty()){
					System.out.println("PASS");
				}else{
					System.out.println(command);
				}
			}
			gameTurn++;
		}
	}
}

class User{
	private int health, mana, deck, rune;

	public User(int health, int mana, int deck, int rune){
		this.health = health;
		this.mana = mana;
		this.deck = deck;
		this.rune = rune;
	}

	public User(Scanner in){
		this.health = in.nextInt();
		this.mana = in.nextInt();
		this.deck = in.nextInt();
		this.rune = in.nextInt();
	}

	int getMana(){
		return mana;
	}

	void setMana(int mana){
		this.mana = mana;
	}

	void changeMana(int mana){
		setMana(this.mana + mana);
	}
}

class Card{
	private int number, id, location, type, cost, attack, defense, myHealthChange, opponentHealthChange, cardDraw;
	String abilities;

	public Card(int number, int id, int location, int type, int cost, int attack, int defense, String abilities,
			int myHealthChange, int opponentHealthChange, int cardDraw){
		this.number = number;
		this.id = id;
		this.location = location;
		this.type = type;
		this.cost = cost;
		this.attack = attack;
		this.defense = defense;
		this.abilities = abilities;
		this.myHealthChange = myHealthChange;
		this.opponentHealthChange = opponentHealthChange;
		this.cardDraw = cardDraw;
	}

	public Card(Scanner in){
		this.number = in.nextInt();
		this.id = in.nextInt();
		this.location = in.nextInt();
		this.type = in.nextInt();
		this.cost = in.nextInt();
		this.attack = in.nextInt();
		this.defense = in.nextInt();
		this.abilities = in.next();
		this.myHealthChange = in.nextInt();
		this.opponentHealthChange = in.nextInt();
		this.cardDraw = in.nextInt();
	}

	boolean isInHand(){
		return location == 0;
	}

	boolean isOnBoard(){
		return location == 1;
	}

	int getCost(){
		return cost;
	}

	int getID(){
		return id;
	}

	int getLocation(){
		return location;
	}

	int getType(){
		return type;
	}

	boolean isCreature(){
		return type == 0;
	}

	String getAbilities(){
		return abilities;
	}

	int getAttack(){
		return attack;
	}

	int getDefense(){
		return defense;
	}
}