package javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BianMa {

	private StringProperty id;
	private StringProperty name;
	
	public BianMa() {
		super();
		this.id = new SimpleStringProperty();
		this.name = new SimpleStringProperty();
	}
	
	
	public BianMa(String id, String name) {
		super();
		this.id = new SimpleStringProperty();
		this.name = new SimpleStringProperty();
		this.id.set(id);;
		this.name.set(name);
	}


	public StringProperty name() {
		return name;
	}

	public String getName() {
		return name.get();
	}

	public void setSid(String sid) {
		this.name.set(sid);
	}
	
	public StringProperty id() {
		return id;
	}
	
	public String getId() {
		return id.get();
	}
	
	public void setId(String id) {
		this.id.set(id);
	}
}
