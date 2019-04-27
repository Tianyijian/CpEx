package javafx;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import ex1.Token;
import ex2.AnalysisState;
import ex2.Constant;
import ex2.SyntaxAnalysis;
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

public class YFPageController implements Initializable{

    @FXML
    private TableView<AnalysisState> tv_result;

    @FXML
    private TableColumn<AnalysisState, String> tc_state,  tc_action, tc_sign, tc_buffer;
    
    @FXML
    private TableColumn<AnalysisState, Integer> tc_step;
    
    @FXML
    private TableView<BianMa> tv_grammar_table;
    
    @FXML
    private TableColumn<BianMa, String> tc_grammar_b;
    

    @FXML
    private TableColumn<BianMa, String> tc_grammar_a;
    
    @FXML
    private Button bt_analysis;

    @FXML
    private Button bt_sta;

    @FXML
    private Button bt_read_input;
    
    @FXML
    private Button bt_yf_clear;
    
    @FXML
    private Button bt_cf_input;
    
    @FXML
    private TextArea ta_first;

    @FXML
    private TextArea ta_items;

    @FXML
    private Button bt_read_grammar;

    @FXML
    private TextArea ta_yf_input;

    @FXML
    private TextArea ta_yf_result;
    
    @FXML
    private TextArea ta_sta;

    @FXML
    private TextArea ta_grammar;

    @FXML
    private TextArea ta_zgrammar;
    
    private SyntaxAnalysis sa;
    
    public static List<Token> tokens = new ArrayList<Token>();
    
    @FXML
    void constructSTA(ActionEvent event) {
//    	System.out.println(tokens.size());
    	sa.readGrammarFromContent(ta_grammar.getText());
    	sa.clearConsole();
    	sa.analysis(sa);
    	ta_zgrammar.setText(sa.printG());
    	ta_first.setText(sa.printFirst() + sa.printFollow());
    	ta_items.setText(sa.printItem());
    	ta_sta.setText(sa.printSTA());
    	tv_grammar_table.setItems(Constant.getGrammarTable());
    	ta_yf_result.setText(sa.printConsole());
    }
    
    @FXML
    void analysis(ActionEvent event) {
    	SemanticAnalysis.init();
    	SemanticAnalysis.clear();
    	sa.setTokens(tokens);
    	String input = ta_yf_input.getText();
    	if (input != null) {
    		if (input.startsWith("Token")) {	//对token文件进行处理
    			input = SyntaxAnalysis.readToken(input.substring(input.indexOf("(")));
//        		ta_yf_input.setText(ta_yf_input.getText() +"\n\n" +  input);
    		}
    		input = input.replaceAll("\n", "").replaceAll("\\s+", "");
//    		System.out.println(input);
        	sa.run(input);
    		List<AnalysisState> res = sa.getAnalysisStates();
    		tv_result.setItems(FXCollections.observableArrayList());
    		tv_result.setItems(FXCollections.observableList(res));
    		ta_yf_result.setText(sa.printConsole());
    	}

    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		tc_step.setCellValueFactory(new PropertyValueFactory<AnalysisState, Integer>("step"));
		tc_sign.setCellValueFactory(new PropertyValueFactory<AnalysisState, String>("sign"));
		tc_state.setCellValueFactory(new PropertyValueFactory<AnalysisState, String>("state"));
		tc_buffer.setCellValueFactory(new PropertyValueFactory<AnalysisState, String>("buffer"));
		tc_action.setCellValueFactory(new PropertyValueFactory<AnalysisState, String>("action"));
		tc_grammar_a.setCellValueFactory(CellData -> CellData.getValue().name());
		tc_grammar_b.setCellValueFactory(CellData -> CellData.getValue().id());
		sa = new SyntaxAnalysis();
	}
	

    @FXML
    void getInputFromCF(ActionEvent event) {
//    	System.out.println(tokens.size());
    }

    @FXML
    void yfClear(ActionEvent event) {
    	ta_zgrammar.clear();;
    	ta_first.clear();
    	ta_items.clear();;
    	ta_sta.clear();
    	ta_yf_input.clear();
    	ta_grammar.clear();
    	tv_result.setItems(FXCollections.observableArrayList());
    	tv_grammar_table.setItems(FXCollections.observableArrayList());
    	ta_yf_result.clear();
    	sa = new SyntaxAnalysis();
    }
}

