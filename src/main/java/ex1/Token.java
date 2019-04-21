package ex1;

public class Token {
	
	private int type;	//种别
	private String field;	//属性值
	private int row;	//行
	private int col;	//列
	
	public Token() {
		super();
	}

	public Token(int type, String field, int row, int col) {
		super();
		this.type = type;
		this.field = field;
		this.row = row;
		this.col = col;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", field=" + field + ", row=" + row + ", col=" + col + "]";
	}
	
	
}
