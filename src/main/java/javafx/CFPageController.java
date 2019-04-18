package javafx;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ex1.LexicalAnalysis;
import ex1.SymbolTable;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

public class CFPageController implements Initializable{

	@FXML
	private Button btn_read_from_file, btn_cffx, btn_cffx1, btn_cffx2, btn_clear;
	
    @FXML
    private TextArea ta_console;

    @FXML
    private TableView<SymbolTable> tv_fhb;
    
    @FXML
    private TableColumn<SymbolTable, String> tc_symbol_index, tc_symbol_value, tc_symbol_type, tc_symbol_attribute,tc_symbol_extend, tc_symbol_name, tc_symbol_addr;

	@FXML
	private TextArea ta_input;
	
	
	@FXML
	private TextArea ta_result;
	
	@FXML
	private TableView<BianMa> tv_bm;
	
	@FXML
	private TableColumn<BianMa, String> tc_lbbm;
	
	@FXML
	private TableColumn<BianMa, String> tc_dcmc;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tc_lbbm.setCellValueFactory(CellData -> CellData.getValue().id());
		tc_dcmc.setCellValueFactory(CellData -> CellData.getValue().name());
		tc_symbol_index.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("index"));
		tc_symbol_name.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("name"));
		tc_symbol_value.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("value"));
		tc_symbol_addr.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("address"));
		tc_symbol_extend.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("extend"));
		tc_symbol_type.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("type"));
		tc_symbol_attribute.setCellValueFactory(new PropertyValueFactory<SymbolTable, String>("attribute"));
		tv_bm.setItems(new LexicalAnalysis().getBianMa());
	}

	@FXML
	public void cffx(ActionEvent event) {
		if (ta_input.getText().equals("")) {
			ta_console.setText("输入不能为空！");
			return;
		}
		
		String content = ta_input.getText();
//		System.out.println("content:" + content+ "--");
		LexicalAnalysis la = new LexicalAnalysis(content);
		String res = la.getResult();
		ta_result.setText(res);
		ta_console.setText(la.getConsole());
		List<SymbolTable> symbolTables = la.getSymbolTables();
		tv_fhb.setItems(FXCollections.observableList(symbolTables));
	}
	@FXML
	public void clear() {
		ta_input.clear();
		ta_result.clear();
		ta_console.clear();
		tv_fhb.setItems(FXCollections.observableArrayList());
	}
}
