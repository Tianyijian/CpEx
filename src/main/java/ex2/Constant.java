package ex2;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.BianMa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Constant {

	/**
	 * 词法分析的种别码表
	 * 
	 */
	public static List<String> zbm = Arrays.asList("error", "id", "uint", "ureal", "bool", "str", "integer", "real",
			"boolean", "record", "if", "else", "then", "do", "while", "true", "false", "and", "not", "or", "(", ")",
			"{", "}", "[", "]", ",", ";", "=", "+", "-", "*", ">", "<", ">=", "==", "<=", "!=");

	/**
	 * 将文法中的终结符转换为单字符表示的终结符
	 * 
	 */
	@SuppressWarnings("serial")
	public static Map<String, String> grammarTable = new LinkedHashMap<String, String>() {
		{
			put("id", "i");
			put("digit", "d");
			put("integer", "t");
			put("real", "r");
			put("boolean", "o"); //
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
		}
	};
	
	/**
	 * 返回种别码给界面
	 * 
	 * @return
	 */
	public static ObservableList<BianMa> getBianMa() {
		ObservableList<BianMa> res = FXCollections.observableArrayList();
		for (int i = 1; i < zbm.size(); i++) {
			BianMa bianMa = new BianMa(i + "", zbm.get(i));
			res.add(bianMa);
		}
		return res;
	}

	/**
	 * 返回grammarTable给界面
	 * 
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
