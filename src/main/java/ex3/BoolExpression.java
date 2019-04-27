package ex3;

import java.util.List;

public class BoolExpression {

	private List<Integer> trueList;
	private List<Integer> falseList;
	
	public BoolExpression() {
		super();
	}
	
	
	public BoolExpression(List<Integer> trueList, List<Integer> falseList) {
		super();
		this.trueList = trueList;
		this.falseList = falseList;
	}

	public List<Integer> getTrueList() {
		return trueList;
	}
	public void setTrueList(List<Integer> trueList) {
		this.trueList = trueList;
	}
	public List<Integer> getFalseList() {
		return falseList;
	}
	public void setFalseList(List<Integer> falseList) {
		this.falseList = falseList;
	}

	
	
}
