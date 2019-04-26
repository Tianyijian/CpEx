package ex1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ex2.Constant;

public class LexicalAnalysis {
	// 种别码， 按单词特性编码
	private List<String> zbm = Constant.zbm;

	private String content; // 要识别的内容
	private int index; // 读取到的下标
	private List<Integer> lineNum = new ArrayList<>(); // 存储每行的字符数
	private StringBuffer res = new StringBuffer(); // 存储结果
	private StringBuilder console = new StringBuilder(); // 存储打印信息
	private Map<String, SymbolTable> symbolTable = new LinkedHashMap<String, SymbolTable>(); // 符号表
	private List<Token> tokens = new ArrayList<Token>(); // 存储token序列

	/**
	 * 传入待分析的代码串，并进行分析
	 * 
	 * @param content
	 */
	public LexicalAnalysis(String content) {
		this.content = content;
		sortChar();
	}

	public LexicalAnalysis() {
		super();
	}

	/**
	 * 用于直接测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "src/main/java/ex1/cftest.txt";
		new LexicalAnalysis().scan(filename);

	}

	/**
	 * 从文件读入待分析代码
	 * 
	 * @param filename
	 */
	private void scan(String filename) {
		try {
			File file = new File(filename);
			Long fileLength = file.length();
			FileInputStream in = new FileInputStream(file);
			byte[] bytes = new byte[fileLength.intValue()];
			in.read(bytes);
			in.close();
			content = new String(bytes);
			System.out.println("content: \n\n" + content);
			sortChar(); // 开始分析
			System.out.println("result: \n\n" + getResult()); // 打印结果
			System.out.println("console: \n\n" + getConsole()); // 打印控制台信息
		} catch (FileNotFoundException e) {
			System.err.println("FileNotFound: " + filename);
		} catch (IOException e) {
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
				break;
			}
			char ch = content.charAt(index);

			if (isLetter(ch)) {
				recogId(); // 识别标识符
			} else if (isDigit(ch)) {
				recogDig(); // 识别数字
			} else if (ch == '/') {
				recogCom(); // 识别注释
			} else if (ch == ' ') { // 空格
				index++;
			} else if (isDel(ch)) {
				recogDel(); // 识别界限符
			} else if (ch == '\r') { // 换行符 \r\n
				recordLine(); // 记录行的字符数
			} else if (ch == '\n') { // 换行符 \n
				recordLine2();
			} else { // 错误字符，向后处理
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
		Token token = new Token(zbm.indexOf(word.toString()), "0", lineNum.size() + 1, getColNumByIndex(index)- word.length() +1);
		if (isGjz(word.toString())) {
			res.append("(" + zbm.indexOf(word.toString()) + ", 0)\n");
		} else {
			token.setType(zbm.indexOf("id"));
			int r = lookupSymbolTable(word.toString());
			if (r == -1) { // 判断是否在符号表,不在则加入符号表
				SymbolTable st = new SymbolTable(symbolTable.size() + 1 + "", word.toString(), "变量");
				symbolTable.put(word.toString(), st);
				res.append("(" + zbm.indexOf("id") + ", STIndex: " + word.toString() + ")\n");
				token.setField("STIndex: " + word.toString());
			} else {
				res.append("(" + zbm.indexOf("id") + ", STIndex: " + word.toString() + ")\n");
				token.setField("STIndex: " + word.toString());
			}
			
		}
		tokens.add(token);
	}

	/**
	 * 查找符号表
	 * 
	 * @param symbol
	 * @return 不存在该符号返回-1， 存在返回其index
	 */
	private int lookupSymbolTable(String symbol) {
		if (symbolTable.containsKey(symbol)) {
			return Integer.valueOf(symbolTable.get(symbol).getIndex());
		}
		return -1;
	}

	/**
	 * 识别数字.
	 */
	private void recogDig() {
		char ch = content.charAt(index);
		StringBuffer word = new StringBuffer();
		while (true) {
			if (isDigit(ch) || isFloat(ch)) {
				word.append(ch);
				index++;
			} else {
				break;
			}
			ch = content.charAt(index);
		}
		if (word.toString().matches("[0-9]+\\.?[0-9]*(e[+-]?[0-9]+)?")) {
			Token token = new Token(0, word.toString(), lineNum.size() + 1, getColNumByIndex(index) -  word.length() + 1);
			if (word.toString().contains(".")) {
				res.append("(" + zbm.indexOf("ureal") + "," + word.toString() + ")\n");
				token.setType(zbm.indexOf("ureal"));
			} else {
				res.append("(" + zbm.indexOf("uint") + "," + word.toString() + ")\n");
				token.setType(zbm.indexOf("uint"));
			}
			tokens.add(token);
		} else {
			console.append("Digit error in line " + (lineNum.size() + 1) + ": " + word.toString() + "\n");
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
			index++;
			if (index >= content.length()) {
				console.append("Comment not end in line " + (lineNum.size() + 1) + ": " + sb.toString() + "\n");
				return;
			}
			ch = content.charAt(index);
			if (ch == '*') {
				if (index < content.length() - 1) {
					char ch1 = content.charAt(index + 1);
					if (ch1 == '/') {
						index += 2;
						console.append("Comment in line " + (lineNum.size() + 1) + ": " + sb.toString() + "\n");
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
	 * 识别界符以及运算符
	 */
	private void recogDel() {
		char ch = content.charAt(index);

		if (isDel(ch)) { // 单个界符或者运算符
			Token token = new Token(0, "0", lineNum.size() + 1, getColNumByIndex(index) + 1);
			if (index < content.length() - 1) {
				String str = content.substring(index, index + 2);
				if (isDDel(str)) { // 双运算符
					res.append("(" + zbm.indexOf(str) + ", 0)\n");
					token.setType(zbm.indexOf(str));
					tokens.add(token);
					index += 2;
					return;
				}

			}
			if (ch == '!') { // ‘!=’为合法运算符，‘!’为不合法字符
				error();
			}
			res.append("(" + zbm.indexOf(ch + "") + ", 0)\n");
			token.setType(zbm.indexOf(ch + ""));
			tokens.add(token);
			index++;
		}

	}

	/**
	 * 判断是否是关键字
	 * 
	 * @param word
	 * @return
	 */
	private boolean isGjz(String word) {
		if (zbm.subList(zbm.indexOf("integer"), zbm.indexOf("or") + 1).contains(word)) {
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
		if (zbm.subList(zbm.indexOf("("), zbm.indexOf("<") + 1).contains("" + ch) || ch == '!') {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否是双运算符
	 * 
	 * @param str
	 * @return
	 */
	private boolean isDDel(String str) {
		if (zbm.subList(zbm.indexOf(">="), zbm.indexOf("!=") + 1).contains(str)) {
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
	 * 判断是否是浮点数或者科学计数法
	 * 
	 * @param ch
	 * @return
	 */
	private boolean isFloat(char ch) {
		if (ch == '.' || ch == 'e' || ch == 'E' || ch == '+' || ch == '-') {
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
	 * 记录一行的字符数 换行符为 \r\n
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
	 * 记录一行的字符数 换行符为 \n
	 */
	private void recordLine2() {
		lineNum.add(getColNumByIndex(index));
		index += 1;
	}

	/**
	 * 通过index 获得当前列号，换行符为 \n
	 * 
	 * @param index
	 * @return
	 */
	private int getColNumByIndex(int index) {
		int sum = 0;
		for (int i = 0; i < lineNum.size(); i++) {
			sum += lineNum.get(i) + 1;
		}
		return index - sum;
	}

	/**
	 * 错误处理，输出错误字符的位置
	 */
	private void error() {
		int line = lineNum.size() + 1;
		int sum = 0;
		for (int i = 0; i < lineNum.size(); i++) {
			sum += lineNum.get(i) + 1;
		}
		console.append(String.format("illegal char: (%d row, %d col)\n", line, index + 1 - sum));
	}

	/**
	 * 返回符号表
	 * 
	 * @return
	 */
	public List<SymbolTable> getSymbolTables() {
		List<SymbolTable> res = new ArrayList<SymbolTable>();
		for (String key : symbolTable.keySet()) {
			res.add(symbolTable.get(key));
		}
		return res;
	}

	/**
	 * 返回分析结果
	 * 
	 * @return
	 */
	public String getResult() {
		return res.toString();
	}

	/**
	 * 返回控制台打印信息
	 * 
	 * @return
	 */
	public String getConsole() {
		return console.toString();
	}

	/**
	 * 返回token的输出结果
	 * 
	 * @return
	 */
	public String getTokenResult() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			res.append("(" + token.getType() + ", " + token.getField() + ")\n");
//			res.append(tokens.get(i).toString()+"\n");
		}
		return res.toString();
	}

	/**
	 * 清除tokens
	 */
	public void cleanTokens() {
		tokens.clear();
	}
	/**
	 * 返回tokens
	 * 
	 * @return
	 */
	public List<Token> getTokens() {
		return tokens;
	}
	
	/**返回symbolTable
	 * @return
	 */
	public Map<String, SymbolTable> getSymbolTable() {
		return symbolTable;
	}
}
