package ex2;

public class AnalysisState {

	private int step;	//步骤
	private String state;		//状态栈
	private String sign;		//符号栈
	private String buffer;			//缓冲区
	private String action;			//执行动作
	
	
	public AnalysisState() {
		super();
	}

	
	public AnalysisState(int step, String state, String sign, String buffer) {
		super();
		this.step = step;
		this.state = state;
		this.sign = sign;
		this.buffer = buffer;
	}


	public AnalysisState(int step, String state, String sign, String buffer, String action) {
		super();
		this.step = step;
		this.state = state;
		this.sign = sign;
		this.buffer = buffer;
		this.action = action;
	}
	
	
	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getBuffer() {
		return buffer;
	}

	public void setBuffer(String buffer) {
		this.buffer = buffer;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "AnalysisState [step=" + step + ", state=" + state + ", sign=" + sign + ", buffer=" + buffer
				+ ", action=" + action + "]";
	}
	
}
