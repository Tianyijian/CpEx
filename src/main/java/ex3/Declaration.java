package ex3;

public class Declaration {
	private String name;
	private String type;
	private int width;
	
	
	public Declaration(String name, String type, int width) {
		super();
		this.name = name;
		this.type = type;
		this.width = width;
	}
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	
	
}
