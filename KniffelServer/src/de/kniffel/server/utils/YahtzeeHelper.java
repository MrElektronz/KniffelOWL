package de.kniffel.server.utils;

import java.util.ArrayList;

public class YahtzeeHelper {

	
	private static YahtzeeHelper instance;
	
	
	private YahtzeeHelper() {}
	
	public static YahtzeeHelper getInstance() {
		if(instance == null) {
			instance = new YahtzeeHelper();
		}
		
		return instance;
	}
	
	
	/**
	 * 
	 * @param allDices
	 * @return if 0, then no three of a kind
	 */
	public int isThreeOfAKind(ArrayList<Integer> allDices) {
		int i1=0,i2=0,i3=0,i4=0,i5=0,i6=0;
		int sum=0;
		for(int num : allDices) {
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
	

	public int isFourOfAKind(ArrayList<Integer> allDices) {
		int i1=0,i2=0,i3=0,i4=0,i5=0,i6=0;
		int sum=0;
		for(int num : allDices) {
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
	 * @param allDices
	 * @return if 0, then no three of a kind
	 */
	public int isFullHouse(ArrayList<Integer> allDices) {
		int[] arr = new int[6];
		for(int num : allDices) {
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
	 * @param allDices
	 * @return 30 if allDices is LowStraight or 0 if not
	 */
	public int isLowStraight(ArrayList<Integer> allDices) {
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		a1.add(1);
		a1.add(2);
		a1.add(3);
		a1.add(4);
		a1.add(5);
		a1.add(6);
		//contains either 1,2,3,4/2,3,4,5 or 3,4,5,6
		if(allDices.containsAll(a1.subList(0, 4))||allDices.containsAll(a1.subList(1, 5))||allDices.containsAll(a1.subList(2, 6))){
			return 30;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param allDices
	 * @return 40 if allDices is HighStraight or 0 if not
	 */
	public int isHighStraight(ArrayList<Integer> allDices) {
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		a1.add(1);
		a1.add(2);
		a1.add(3);
		a1.add(4);
		a1.add(5);
		a1.add(6);
		//contains either 1,2,3,4,5/2,3,4,5,6 or 3,4,5,6
		if(allDices.containsAll(a1.subList(0, 5))||allDices.containsAll(a1.subList(1, 6))){
			return 40;
		}
		return 0;
	}
	
	
	public int isYahtzee(ArrayList<Integer> allDices) {
		int i1=0,i2=0,i3=0,i4=0,i5=0,i6=0;
		for(int num : allDices) {
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
