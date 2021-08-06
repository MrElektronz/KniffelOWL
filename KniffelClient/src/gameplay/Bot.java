package gameplay;

import java.util.ArrayList;
import java.util.Random;

import application.OfflineGameBoardController;
import javafx.application.Platform;

/**
 * 
 * @author KBeck
 * This class resembles bots
 */
public class Bot extends OfflinePlayer{

	private BotDifficulty difficulty;
	private OfflineGameBoardController ogbc;
	private String color;
	private ArrayList<Integer> alreadySet;
	public Bot(BotDifficulty difficulty,OfflineGameBoardController ogbc) {
		super("Bot["+difficulty.name()+"]");
		this.ogbc = ogbc;
		color = "#987267";
		alreadySet = new ArrayList<Integer>();
		this.difficulty = difficulty;
	}
	
	public Bot(BotDifficulty difficulty,OfflineGameBoardController ogbc, String color) {
		super("Bot["+difficulty.name()+"]");
		this.ogbc = ogbc;
		this.color = color;
		alreadySet = new ArrayList<Integer>();
		this.difficulty = difficulty;
	}
	
	public BotDifficulty getDifficulty() {
		return difficulty;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setDifficulty(BotDifficulty difficulty) {
		this.difficulty = difficulty;
	}
	
	//call to start the bot
	public void start() {
		LogicThread lT = new LogicThread();
		lT.start();
	}
	
	/**
	 * 
	 * @author KBeck
	 * This is used for the bot logic (a thread is useful so that the main thread doesnt freeze if we want to add a delay)
	 */
	private class LogicThread extends Thread{
		
		//Both are used to simulate the behaviour of a real player (e.g random delay time)
		private int delay,delayRatio;
		
		public LogicThread(int delay, int delayRatio) {
			this.delay = delay;
			this.delayRatio = delayRatio;
		}
		
		public LogicThread() {
			this.delay = 1500;
			this.delayRatio = 200; //newDelay() can sleep for a value between 1300 and 1700
		}
		
		//logic is specified in here
		@Override
		public void run() {
			//Sample code, roll 2 times, then add to the field with highest score
			rollDice();
			rollDice();
			ArrayList<Integer> scores = getScores(); //gets the scores
			//getting max index of scores
			int index = -1;
			int maxValue = 0;
			//set field of highest score
			for(int i = 0; i< scores.size();i++) {
				if(scores.get(i) >maxValue && !ogbc.isScoreAlreadySet(i) && !alreadySet.contains(i) && ogbc.isChoosableScoreID(i)) {
					maxValue = scores.get(i);
					index = i;
				}
			}
			//if no highest score field found, set next field
			if(index ==-1) {
				for(int i = 0; i< scores.size();i++) {
					if(ogbc.isChoosableScoreID(i)&&!alreadySet.contains(i)) {
						index = i;
						break;
					}
				}
			}
			
			chooseScore(index, scores); //choose aces
			
			
		}
		
		public ArrayList<Integer> getScores(){
			newDelay();
			return ogbc.getScores();
		}
		
		public void chooseScore(int id, ArrayList<Integer> scores) {
			newDelay();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					ogbc.chooseScore(id, scores);
					alreadySet.add(id);
				}
			});
		}
		
		public void rollDice() {
			newDelay();
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					ogbc.rollDice();
				}
			});
		}
		
		/**
		 * 
		 * sleeps for a value between delay-delayRatio and delay+delayRatio
		 */
		private void newDelay() {
			Random r = new Random();
			int pos = 1;
			if(r.nextBoolean()) {
				pos=-1;
			}
			 try {
				sleep(delay+(r.nextInt(delayRatio+1)*pos));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public enum BotDifficulty{
		EASY,MEDIUM,HARD;
	}
}
