package de.kniffel.server.utils;

import java.util.ArrayList;

/**
 * Helps to determine the score of the rolled dice.
 * 
 * @author KBeck
 *
 */
public class YahtzeeHelper {

	
	private static YahtzeeHelper instance;
	
	
	private YahtzeeHelper() {}
	
	/**
	 * 
	 * @return One and only instance of YahtzeeHelper
	 */
	public static YahtzeeHelper getInstance() {
		if(instance == null) {
			instance = new YahtzeeHelper();
		}
		
		return instance;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @return if 0, then no three of a kind
	 */
	public int isThreeOfAKind(ArrayList<Integer> allDice) {
		int i1=0,i2=0,i3=0,i4=0,i5=0,i6=0;
		int sum=0;
		for(int num : allDice) {
			sum+=num;
			switch(num) {
			case 1:
				i1++;break;
			case 2:
				i2++;break;
			case 3:
				i3++;break;
			case 4:
				i4++;break;
			case 5:
				i5++;break;
			case 6:
				i6++;break;
			default: 
				break;
			}
		}
		if(i1>2||i2>2||i3>2||i4>2||i5>2||i6>2) {
			return sum;			
		}
		return 0;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @param toCount number to be counted
	 * @return how often toCount is found inside our allDice array
	 */
	public int countEquals(ArrayList<Integer> allDice, int toCount) {
		
		int count = 0;
		for(int i = 0; i<allDice.size();i++) {
			if(allDice.get(i) == toCount) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @return if 0, then no four of a kind
	 */
	public int isFourOfAKind(ArrayList<Integer> allDice) {
		int i1=0,i2=0,i3=0,i4=0,i5=0,i6=0;
		int sum=0;
		for(int num : allDice) {
			sum+=num;
			switch(num) {
			case 1:
				i1++;break;
			case 2:
				i2++;break;
			case 3:
				i3++;break;
			case 4:
				i4++;break;
			case 5:
				i5++;break;
			case 6:
				i6++;break;
			default: 
				break;
			}
		}
		if(i1>3||i2>3||i3>3||i4>3||i5>3||i6>3) {
			return sum;			
		}
		return 0;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @return if 0, then no full house
	 */
	public int isFullHouse(ArrayList<Integer> allDice) {
		int[] arr = new int[6];
		for(int num : allDice) {
			arr[num-1]=arr[num-1]+1;
		}
		boolean b1=false,b2=false;
		for(int i : arr) {
			if(i>2) {
				b2=true;
			}else if(i>1) {
				b1=true;
			}
		}
		//If both conditions are true
		if(b1&&b2) {
			return 25;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @return 30 if allDice is LowStraight or 0 if not
	 */
	public int isLowStraight(ArrayList<Integer> allDice) {
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		a1.add(1);
		a1.add(2);
		a1.add(3);
		a1.add(4);
		a1.add(5);
		a1.add(6);
		//contains either 1,2,3,4/2,3,4,5 or 3,4,5,6
		if(allDice.containsAll(a1.subList(0, 4))||allDice.containsAll(a1.subList(1, 5))||allDice.containsAll(a1.subList(2, 6))){
			return 30;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @return 40 if allDice is HighStraight or 0 if not
	 */
	public int isHighStraight(ArrayList<Integer> allDice) {
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		a1.add(1);
		a1.add(2);
		a1.add(3);
		a1.add(4);
		a1.add(5);
		a1.add(6);
		//contains either 1,2,3,4,5/2,3,4,5,6 or 3,4,5,6
		if(allDice.containsAll(a1.subList(0, 5))||allDice.containsAll(a1.subList(1, 6))){
			return 40;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param allDice list of dice numbers to be calculated from
	 * @return if 5 of a kind return 50, otherwise 0
	 */
	public int isYahtzee(ArrayList<Integer> allDice) {
		int i1=0,i2=0,i3=0,i4=0,i5=0,i6=0;
		for(int num : allDice) {
			switch(num) {
			case 1:
				i1++;break;
			case 2:
				i2++;break;
			case 3:
				i3++;break;
			case 4:
				i4++;break;
			case 5:
				i5++;break;
			case 6:
				i6++;break;
			default: 
				break;
			}
		}
		if(i1>4||i2>4||i3>4||i4>4||i5>4||i6>4) {
			return 50;			
		}
		return 0;
	}
	
	
}
