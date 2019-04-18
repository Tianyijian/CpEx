package ex2;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.BianMa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Constant {

	/**将文法中的终结符转换为单字符表示的终结符
	 * 
	 */
	@SuppressWarnings("serial")
	public  static Map<String, String> grammarTable = new LinkedHashMap<String, String>(){{
		put("id", "i");
		put("digit", "d");
		put("integer", "t");
		put("real", "r");
		put("record", "s");
		put("proc", "p");
		put("num", "n");
		put("if", "f");
		put("else", "e");
		put("then", "g");
		put("do", "v");
		put("while", "w");
		put("or", "x");
		put("and", "y");
		put("not", "z");
		put("true", "a");
		put("false", "b");
		put("relop", "c");
		put("<=", "j");
		put(">=", "k");
		put("==", "l");
		put("!=", "m");
	}}; 
	
	/**返回grammarTable给界面
	 * @return
	 */
	public static ObservableList<BianMa> getGrammarTable() {
		ObservableList<BianMa> res = FXCollections.observableArrayList();
		for (String key : grammarTable.keySet()) {
			BianMa bianMa = new BianMa(key, grammarTable.get(key));
			res.add(bianMa);
		}
		return res;
	}
}
