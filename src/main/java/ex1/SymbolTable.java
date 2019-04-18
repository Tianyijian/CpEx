package ex1;

public class SymbolTable {

	private String index;		//入口下标
	private String name;	//标识符名字 abc
	private String type;	//符号种类	变量、数组、函数、形参
	private String attribute;	//类型	int、float
	private String value;	//值	100、1.2
	private String address;	//地址
	private String extend;	//扩展属性指针
	
	
	public SymbolTable() {
		super();
	}
	
	
	public SymbolTable(String index, String name, String type) {
		super();
		this.index = index;
		this.name = name;
		this.type = type;
	}


	public SymbolTable(String index, String name, String type, String attribute, String value, String address,
			String extend) {
		super();
		this.index = index;
		this.name = name;
		this.type = type;
		this.attribute = attribute;
		this.value = value;
		this.address = address;
		this.extend = extend;
	}


	public String getIndex() {
		return index;
	}


	public void setIndex(String index) {
		this.index = index;
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


	public String getAttribute() {
		return attribute;
	}


	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getExtend() {
		return extend;
	}


	public void setExtend(String extend) {
		this.extend = extend;
	}


	@Override
	public String toString() {
		return "SymbolTable [index=" + index + ", name=" + name + ", type=" + type + ", attribute=" + attribute
				+ ", value=" + value + ", address=" + address + ", extend=" + extend + "]";
	}
	
	

	
	
}
