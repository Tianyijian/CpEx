package javafx;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ex1.SymbolTable;
import ex3.SemanticAnalysis;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

public class YYPageController implements Initializable{


    @FXML
    private Button bt_yy_analysis;
    
    @FXML
    private TextArea ta_yy_console;
    
    @FXML
    private TableView<SymbolTable> tv_fhb;
    
    @FXML
    private TableColumn<SymbolTable, String> tc_symbol_index, tc_symbol_value, tc_symbol_type, tc_symbol_attribute,tc_symbol_extend, tc_symbol_name, tc_symbol_addr;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tc_symbol_index.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("index"));
		tc_symbol_name.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("name"));
		tc_symbol_value.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("value"));
		tc_symbol_addr.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("address"));
		tc_symbol_extend.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("extend"));
		tc_symbol_type.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("type"));
		tc_symbol_attribute.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("attribute"));
	}
	

    @FXML
    void yyAnalysis(ActionEvent event) {
		List<SymbolTable> symbolTables = SemanticAnalysis.getSymbolTables();
//		System.out.println("yyAnalysis: " + symbolTables.size());
		tv_fhb.setItems(FXCollections.observableList(symbolTables));
		ta_yy_console.setText(SemanticAnalysis.getCode());
    }
}
