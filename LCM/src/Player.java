import java.util.*;
import java.io.*;
import java.math.*;

// Class that interacts with Codingame
class Player{
	public static void main(String args[]){
		Scanner in = new Scanner(System.in);
		int turn = 0;
		while(true){
			Manager man = new Manager(in, turn);
			CommandGroup command = man.getCommand();
			System.out.println(command.toString());
			turn++;
		}
	}
}

// Class that makes turn decisions
class Manager{
	User myself, opp;
	int turn;
	ArrayList<Card> draftCards = new ArrayList<Card>();
	public Manager(Scanner in, int turn){
		myself = new User(in);
		opp = new User(in);
		this.turn = turn;
		int opponentHand = in.nextInt();
		int cardCount = in.nextInt();
		for(int i = 0; i < cardCount; i++){
			Card newCard = new Card(in, i);
			if(turn < 30){
				draftCards.add(newCard);
			}else{
				if(newCard.getType() == 0 && newCard.isMine()){ // CREATURE
					Creature newCreature = new Creature(newCard);
					myself.addCreature(newCreature);
				}else if(newCard.isMine()){ // ITEM
					Item newItem = new Item(newCard);
					myself.addItem(newItem);
				}else{ // OPPONENT
					if(newCard.getType() == 0){
						Creature newCreature = new Creature(newCard);
						opp.addCreature(newCreature);
					}else{
						Item newItem = new Item(newCard);
						opp.addItem(newItem);
					}
				}
			}
		}
	}
	public CommandGroup getCommand(){
		if(turn < 30){
			return getDraftCommand();
		}else{
			return getTurnCommand();
		}
	}
	public CommandGroup getDraftCommand(){
		CommandGroup toReturn = new CommandGroup();
		// CHOOSING DRAFT CARD
		Card toDraft = null;
		float bestScore = -10000000;
		for(int i = 0; i < draftCards.size(); i++) {
			float score = myself.scoreDraftCard(draftCards.get(i));
			if(score > bestScore) {
				toDraft = draftCards.get(i);
			}
		}
		Constants.draft.add(toDraft); // adding so can keep track
		// SUBMITTING COMMAND
		toReturn.addCommand(Constants.DRAFT, toDraft);
		return toReturn;
	}
	public CommandGroup getTurnCommand(){
		CommandGroup toReturn = new CommandGroup();
		// SUMMONING LOGIC
		for(int i = 0; i < myself.creaturesInHand.size(); i++) {
			if(myself.getMana() >= myself.creaturesInHand.get(i).getCost()) {
				toReturn.addCommand(Constants.SUMMON, myself.creaturesInHand.get(i));
				myself.changeMana(-myself.creaturesInHand.get(i).getCost());
			}
		}
		// ATTACK LOGIC
		Creature creatureToAttack = null; // stay null if want to attack opponent
		for(int i = 0; i < opp.creaturesOnBoard.size(); i++) {
			Creature opCreature = opp.creaturesOnBoard.get(i);
			if(opCreature.guard()) {
				creatureToAttack = opCreature;
			}
		}
		for(int i = 0; i < myself.creaturesOnBoard.size(); i++) {
			toReturn.addCommand(Constants.ATTACK, myself.creaturesOnBoard.get(i), creatureToAttack);
		}
		return toReturn;
	}
}

class CommandGroup{
	ArrayList<Command> commands = new ArrayList<Command>();
	String comment;
	public CommandGroup(){
		comment = "";
	}
	public String toString(){
		if(commands.size() == 0){
			return "PASS";
		}
		String toReturn = "";
		for(int i = 0; i < commands.size(); i++){
			if(i > 0){
				toReturn += ";";
			}
			toReturn += commands.get(i).toString();
		}
		return toReturn + comment;
	}
	public void addCommand(){
		Command c = new Command();
		commands.add(c);
	}
	public void addCommand(int type){
		Command c = new Command(type);
		commands.add(c);
	}
	public void addCommand(int type, Card a){
		Command c = new Command(type, a);
		commands.add(c);
	}
	public void addCommand(int type, Card a, Card b){
		Command c = new Command(type, a, b);
		commands.add(c);
	}
	public void addCommand(Command c){
		commands.add(c);
	}
}

class Command{
	int type;
	Card a = null, b = null;
	boolean attackOp = false;
	public Command(){ // CONSTRUCTOR FOR PASS
		this.type = Constants.PASS;
	}
	public Command(int type){
		this.type = type;
	}
	public Command(int type, Card a){
		this.type = type;
		this.a = a;
		if(this.type == Constants.ATTACK || this.type == Constants.USE){
			attackOp = true;
		}
	}
	public Command(int type, Card a, Card b){
		this.type = type;
		this.a = a;
		this.b = b;
		if(this.b == null) { // null Card b means to attack opponent
			attackOp = true;
		}
	}
	public String toString(){
		String toReturn = "";
		switch(type){
			case Constants.PASS:
				toReturn = "PASS";
				return toReturn;
			case Constants.ATTACK:
				toReturn = "ATTACK";
				break;
			case Constants.USE:
				toReturn = "USE";
				break;
			case Constants.DRAFT:
				toReturn = "PICK " + a.getDraftPos();
				return toReturn;
			case Constants.SUMMON:
				toReturn = "SUMMON";
				break;
			default:
				toReturn = "PASS";
				return toReturn;
		}
		toReturn += " " + a.getId();
		if(b == null){
			if(attackOp){
				toReturn += " -1";
			}
		}else{
			toReturn += " " + b.getId();
		}
		return toReturn;
	}
}

// Class that holds information for each "player" in the game
class User{
	private int health, mana, deck, rune;
	ArrayList<Creature> creaturesOnBoard = new ArrayList<Creature>();
	ArrayList<Creature> creaturesInHand = new ArrayList<Creature>();
	ArrayList<Item> itemsOnBoard = new ArrayList<Item>();
	ArrayList<Item> itemsInHand = new ArrayList<Item>();
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
	public void addCreature(Creature a){
		if(Math.abs(a.getLocation()) == 1){
			creaturesOnBoard.add(a);
		}else{
			creaturesInHand.add(a);
		}
	}
	public void addItem(Item a){
		if(Math.abs(a.getLocation()) == 1){
			itemsOnBoard.add(a);
		}else{
			itemsInHand.add(a);
		}
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
	public int getHealth(){
		return health;
	}
	public void setHealth(int health){
		this.health = health;
	}
	public int getDeck(){
		return deck;
	}
	public void setDeck(int deck){
		this.deck = deck;
	}
	public int getRune(){
		return rune;
	}
	public void setRune(int rune){
		this.rune = rune;
	}
	public float scoreDraftCard(Card b){
		int numDrafted = Constants.getNumDrafted();
		int numItems = Constants.getNumItemsDrafted();
		int numCreatures = Constants.getNumCreaturesDrafted();
		final int MAXITEMS = 5; // change this to change items to creatures ratio
		final int MAXCREATURES = 30 - MAXITEMS;
		if(b.isCreature()){
			Creature a = new Creature(b);
			float score = a.getDraftScore();
			if(numCreatures >= MAXCREATURES) {
				score -= 10000;
			}
			return score;
		}else{
			Item a = new Item(b);
			float score = a.getDraftScore();
			if(numItems >= MAXITEMS) {
				score -= 10000;
			}
			return score;
		}
	}
}

// Class for a card
class Card{
	private int number, id, location, type, cost, attack, defense, myHealthChange, opponentHealthChange, cardDraw,
			draftPos;
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
	public Card(Scanner in, int draftPos){
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
		this.draftPos = draftPos;
	}
	boolean isInHand(){
		return location == 0;
	}
	boolean isOnBoard(){
		return location == 1;
	}
	int getDraftPos(){
		return draftPos;
	}
	int getCost(){
		return cost;
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
	public int getNumber(){
		return number;
	}
	public void setNumber(int number){
		this.number = number;
	}
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	public int getMyHealthChange(){
		return myHealthChange;
	}
	public void setMyHealthChange(int myHealthChange){
		this.myHealthChange = myHealthChange;
	}
	public int getOpponentHealthChange(){
		return opponentHealthChange;
	}
	public void setOpponentHealthChange(int opponentHealthChange){
		this.opponentHealthChange = opponentHealthChange;
	}
	public int getCardDraw(){
		return cardDraw;
	}
	public void setCardDraw(int cardDraw){
		this.cardDraw = cardDraw;
	}
	public void setLocation(int location){
		this.location = location;
	}
	public void setType(int type){
		this.type = type;
	}
	public void setCost(int cost){
		this.cost = cost;
	}
	public void setAttack(int attack){
		this.attack = attack;
	}
	public void setDefense(int defense){
		this.defense = defense;
	}
	public void setAbilities(String abilities){
		this.abilities = abilities;
	}
	public boolean isMine(){
		return this.location >= 0;
	}
	public boolean breakthrough() {
		return this.getAbilities().contains("B");
	}
	public boolean charge() {
		return this.getAbilities().contains("C");
	}
	public boolean drain() {
		return this.getAbilities().contains("D");
	}
	public boolean guard() {
		return this.getAbilities().contains("G");
	}
	public boolean lethal() {
		return this.getAbilities().contains("L");
	}
	public boolean ward() {
		return this.getAbilities().contains("W");
	}
}

class Creature extends Card{
	public Creature(Card a){
		super(a.getNumber(), a.getId(), a.getLocation(), a.getType(), a.getCost(), a.getAttack(), a.getDefense(),
				a.getAbilities(), a.getMyHealthChange(), a.getOpponentHealthChange(), a.getCardDraw());
	}
	float getDraftScore(){
		float score = this.getAttack() + this.getDefense() - (this.getCost() * 2);
		if(this.breakthrough()) {
			score += Math.abs(score)*0.2;
		}
		if(this.charge()) {
			score += Math.abs(score)*0.2;
		}
		if(this.drain()) {
			score += Math.abs(score)*0.2;
		}
		if(this.guard()) {
			score += Math.abs(score)*0.4;
		}
		if(this.lethal()) {
			score += Math.abs(score)*0.2;
		}
		if(this.ward()) {
			score += Math.abs(score)*0.2;
		}
		return score;
	}
}

class Item extends Card{
	int type;
	public Item(Card a){
		super(a.getNumber(), a.getId(), a.getLocation(), a.getType(), a.getCost(), a.getAttack(), a.getDefense(),
				a.getAbilities(), a.getMyHealthChange(), a.getOpponentHealthChange(), a.getCardDraw());
		type = a.getType();
	}
	float getDraftScore(){
		if(type == Constants.GREEN){
			return this.getAttack() + this.getDefense() - this.getCost() * 2;
		}else if(type == Constants.RED){
			return -this.getAttack() - this.getDefense() - this.getCost() * 2 + this.getCardDraw();
		}else if(type == Constants.BLUE){
			return -this.getOpponentHealthChange() + this.getMyHealthChange() + this.getCardDraw() - this.getCost()*2;
		}
		return -9999999;
	}
}

class Constants{
	// POSSIBLE MOVES
	public static final int PASS = 0;
	public static final int SUMMON = 1;
	public static final int ATTACK = 2;
	public static final int USE = 3;
	public static final int DRAFT = 4;
	// ALL CARD TYPES
	public static final int CREATURE = 0;
	public static final int GREEN = 1;
	public static final int RED = 2;
	public static final int BLUE = 3;
	// DRAFTED CARDS
	public static ArrayList<Card> draft = new ArrayList<Card>();
	public static int getNumDrafted() {
		return draft.size();
	}
	public static int getNumItemsDrafted() {
		int count = 0;
		for(int i = 0; i < draft.size(); i++) {
			if(draft.get(i).isCreature() == false) {
				count++;
			}
		}
		return count;
	}
	public static int getNumCreaturesDrafted() {
		int count = 0;
		for(int i = 0; i < draft.size(); i++) {
			if(draft.get(i).isCreature()) {
				count++;
			}
		}
		return count;
	}
}