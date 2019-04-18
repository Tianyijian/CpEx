package ex1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javafx.BianMa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class LexicalAnalysis {
	// 种别码， 按单词特性编码
	private List<String> zbm = Arrays.asList("error", "bsf", "uint", "ufloat", "bool", "str", "for", "do", "if", "else", "then", // 0~10
			"true", "false", "int", "float", "boolean", "while", "(", ")", "{", "}", "[", "]", ",", ";", "+", "-", "*", "=", ">", "<", "!", 
			"&", "|", ">=", "==", "<=", "!=", "+=", "-=", "*=", "++", "&&", "||", "--"); 

	private String content; // 要识别的内容
	private int index; // 读取到的下标
	private List<Integer> lineNum = new ArrayList<>(); // 存储每行的字符数
	private StringBuffer res = new StringBuffer(); // 存储结果
	private Map<String, String> fhb = new HashMap<String, String>();	// 符号表
	private Stack<Integer> xkhStack = new Stack<Integer>();
	private Stack<Integer> dkhStack = new Stack<Integer>();
	private Stack<Integer> zkhStack = new Stack<Integer>();
	
	public LexicalAnalysis(String content) {
		this.content = content;
		sortChar();
	}

	public LexicalAnalysis() {
		super();
	}

	public static void main(String[] args) {
		// String filename = "src/ex1/test1";
		String filename = "src/main/java/ex1/test3";
		new LexicalAnalysis().scan(filename);
	}


	private void scan(String filename) {
		try {
			File file = new File(filename);
			Long fileLength = file.length();
			FileInputStream in = new FileInputStream(file);
			byte[] bytes = new byte[fileLength.intValue()];
			in.read(bytes);
			in.close();
			content = new String(bytes);
			System.out.println("content: " + content);
			sortChar();
			System.out.println(res.toString());
			for (int n : lineNum) {
				System.out.print(n + " ");
			}
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFound: " + filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 单词分类模块
	 * 
	 * @param ch
	 */
	private void sortChar() {
		while (true) {
			if (index >= content.length()) {
				recordLine();
				checkClosed();
				break;
			}
			char ch = content.charAt(index);

			if (isLetter(ch)) {
				recogId(); // 识别标识符
			} else if (isDigit(ch)) {
				recogDig(); // 识别数字
			} else if (ch == '/') {
				recogCom(); // 识别注释
			} else if (ch == ' ') {
				index++;
			} else if (isDel(ch)) {
				recogDel(); // 识别界限符
			} else if (ch == '\r') { // 换行符
				recordLine(); // 记录行的字符数
			} else if (ch == '\n') {
				recordLine2();
			}
			else {
				error();
				index++; // 继续向后处理
			}
//            System.out.println(res.toString());
		}
	}

	/**
	 * 识别标识符
	 */
	private void recogId() {
		StringBuffer word = new StringBuffer();
		char ch = content.charAt(index);
		while (true) {
			if (isDigit(ch) || isLetter(ch)) {
				word.append(ch);
				index++;
			} else {
				break;
			}
			ch = content.charAt(index);
		}
		if (isGjz(word.toString())) {
			res.append("(" + zbm.indexOf(word.toString()) + ", 0)\n");
		} else {
			if (fhb.get(word.toString()) == null) {		//判断是否在符号表,不在则加入符号表
				fhb.put(word.toString(), "");
			} 
			res.append("(" + zbm.indexOf("bsf") + ", "+ word.toString() + "符号表入口)\n");
		}
	}

	/**
	 * 识别数字.
	 */
	private void recogDig() {
		char ch = content.charAt(index);
		StringBuffer word = new StringBuffer();
		while (true) {
			if (isDigit(ch) || ch == '.') {
				word.append(ch);
				index++;
			} else {
				break;
			}
			ch = content.charAt(index);
		}
		if (word.toString().contains(".")) {
			res.append("(" + zbm.indexOf("ufloat") + "," + word.toString() + ")\n");

		} else {
			res.append("(" + zbm.indexOf("uint") + "," + word.toString() + ")\n");
		}

	}

	/**
	 * 识别注释
	 */
	private void recogCom() {
		char ch = content.charAt(++index);
		if (ch != '*') {
			return;
		}
		StringBuffer sb = new StringBuffer();
		while (true) {
			index ++;
			if (index >= content.length()) {
				System.err.println("Comment not end: " + sb.toString());
				return;
			}
			ch = content.charAt(index);
			if (ch == '*') {
				if (index < content.length() - 1) {
					char ch1 = content.charAt(index + 1);
					if (ch1 == '/') {
						index += 2;
						System.out.println("Comment: " + sb.toString());
						return;
					}
				}
			}
			if (ch == '\n') {
				int sum = 0;
				for (int i = 0; i < lineNum.size(); i++) {
					sum += lineNum.get(i) + 1;
				}
				lineNum.add(index - sum);
			}
			sb.append(ch);
		}
	}

	/**
	 * 识别界限符.
	 */
	private void recogDel() {
		char ch = content.charAt(index);

		if (isDel(ch)) { // 单个界限符
			if (index < content.length() - 1) {
				String str = content.substring(index, index + 2);
				if (isDDel(str)) { // 双界限符
					res.append("(" + zbm.indexOf(str) + ", 0)\n");
					index += 2;
					return;
				}

			}
			switch (ch) {
			case '(':
				xkhStack.push(index);
				break;
			case ')':
				if (xkhStack.empty()) {
					index++;
					System.err.printf("括号未封闭:  (%d row, %d col)\n", (lineNum.size() + 1), getColNumByIndex(index));
					return;
				}
				xkhStack.pop();
				break;
			case '{':
				dkhStack.push(index);
				break;
			case '}':
				if (dkhStack.empty()) {
					index++;
					System.err.printf("括号未封闭:  (%d row, %d col)\n", (lineNum.size() + 1), getColNumByIndex(index));
					return;
				}
				dkhStack.pop();
				break;
			case '[':
				zkhStack.push(index);
				break;
			case ']':
				if (zkhStack.empty()) {
					index++;
					System.err.printf("括号未封闭:  (%d row, %d col)\n", (lineNum.size() + 1), getColNumByIndex(index));
					return;
				}
				zkhStack.pop();
				break;
			default:
				break;
			}
				
			
			if (ch == '!') {
				error();
			}
			res.append("(" + zbm.indexOf(ch + "") + ", 0)\n");
			index++;
		}

	}

	/**
	    *   检查括号封闭性
	 */
	private void checkClosed() {
		while (!xkhStack.empty()) {
			System.err.printf("括号未封闭:  (%d row, %d col)\n", (lineNum.size() + 1), getColNumByIndex(xkhStack.pop()));
		}
		while (!zkhStack.empty()) {
			System.err.printf("括号未封闭:  (%d row, %d col)\n", (lineNum.size() + 1), getColNumByIndex(zkhStack.pop()));
		}
		while (!dkhStack.empty()) {
			System.err.printf("括号未封闭:  (%d row, %d col)\n", (lineNum.size() + 1), getColNumByIndex(dkhStack.pop()));
		}
	}
	/**
	 * 判断是否是关键字
	 * 
	 * @param word
	 * @return
	 */
	private boolean isGjz(String word) {
		if (zbm.subList(zbm.indexOf("for"), zbm.indexOf("while") + 1).contains(word)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是单界限符
	 * 
	 * @return
	 */
	private boolean isDel(char ch) {
		if (zbm.subList(zbm.indexOf("("), zbm.indexOf("|") + 1).contains("" + ch)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是双界限符
	 * 
	 * @param str
	 * @return
	 */
	private boolean isDDel(String str) {
		if (zbm.subList(zbm.indexOf(">="), zbm.indexOf("--") + 1).contains(str)) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是数字
	 * 
	 * @param ch
	 * @return
	 */
	private boolean isDigit(char ch) {
		if (ch >= '0' && ch <= '9') {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是字母或者下划线
	 * 
	 * @param ch
	 * @return
	 */
	private boolean isLetter(char ch) {
		if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_') {
			return true;
		}
		return false;
	}


	/**
	 * 记录一行的字符数
	 */
	private void recordLine() {
		int sum = 0;
		for (int i = 0; i < lineNum.size(); i++) {
			sum += lineNum.get(i) + 2;
		}
		lineNum.add(index - sum);
		index += 2;
	}

	/**
	 * 记录一行的字符数
	 */
	private void recordLine2() {
		int sum = 0;
		for (int i = 0; i < lineNum.size(); i++) {
			sum += lineNum.get(i) + 1;
		}
		lineNum.add(index - sum);
		index += 1;
	}
	private int getColNumByIndex(int index) {
		int sum = 0;
		for (int i = 0; i < lineNum.size(); i++) {
			sum += lineNum.get(i) + 1;
		}
		return index - sum;
	}
	/**
	 * 	错误处理，输出错误字符
	 */
	private void error() {
		int line = lineNum.size() + 1;
		int sum = 0;
		for (int i = 0; i < lineNum.size(); i++) {
			sum += lineNum.get(i) + 1;
		}
		System.err.printf("illegal char: (%d row, %d col)\n", line, index + 1 - sum);
	}
	
	/** 返回种别码给界面
	 * @return
	 */
	public ObservableList<BianMa> getBianMa() {
		ObservableList<BianMa> res = FXCollections.observableArrayList();
		for (int i = 0; i < zbm.size(); i++) {
			BianMa bianMa = new BianMa(i+"", zbm.get(i));
			res.add(bianMa);
		}
		return res;
	}
	
	/** 返回分析结果
	 * @return
	 */
	public String getResult() {
		return res.toString(); 
	}
}
