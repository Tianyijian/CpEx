package javafx;

import java.net.URL;
import java.util.ResourceBundle;

import ex1.LexicalAnalysis;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

public class CFPageController implements Initializable{

	@FXML
	private Button btn_read_from_file, btn_cffx, btn_cffx1, btn_cffx2, btn_clear;
	
    @FXML
    private TextArea ta_console;

    @FXML
    private TableView<?> tv_fhb;
    
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
	}
	@FXML
	public void clear() {
		ta_input.clear();
		ta_result.clear();
	}
}
