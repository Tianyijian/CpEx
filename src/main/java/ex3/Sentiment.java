package ex3;

import java.util.ArrayList;
import java.util.List;

public class Sentiment {

	private List<Integer> nextList;

	
	public Sentiment() {
		super();
		this.nextList = new ArrayList<Integer>();
	}


	public Sentiment(List<Integer> nextList) {
		super();
		this.nextList = nextList;
	}


	public List<Integer> getNextList() {
		return nextList;
	}


	public void setNextList(List<Integer> nextList) {
		this.nextList = nextList;
	}
	
	
}
