package ex3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ex1.SymbolTable;
import ex1.Token;

public class SemanticTranslate {

	private static int offset = 0;
	public static Map<String, SymbolTable> symbolTable = new LinkedHashMap<String, SymbolTable>(); // 符号表
	private static Map<String, Declaration> declarMap = new LinkedHashMap<String, Declaration>(); // 记录声明语句中的属性
	private static Map<String, String> fuZhiMap = new LinkedHashMap<String, String>(); // 记录赋值语句中的属性
	private static List<String> temp = new ArrayList<String>(); // 临时变量
	public static List<Token> tokens = new ArrayList<Token>(); // 词法获得的token序列
//	private static StringBuilder console = new StringBuilder(); // 控制台打印
	private static List<SiYuanShi> code = new ArrayList<SiYuanShi>(); // 四元式结果
	private static int[] tokensArray;

	public static void trans(String g, int index) {
		if (g.equals("X->t")) {
			Declaration declar = new Declaration("X", "integer", 4);
			declarMap.put("X", declar);
		} else if (g.equals("X->r")) {
			Declaration declar = new Declaration("X", "real", 8);
			declarMap.put("X", declar);
		} else if (g.equals("D->Xi;")) {
			String name = getSTName(index, 2);
			symbolTable.get(name).setAttribute(declarMap.get("X").getType());
			symbolTable.get(name).setAddress((offset + ""));
			offset += declarMap.get("X").getWidth();
			setTokenArray(index, 2);
		} else if (g.equals("S->i=E;")) {
			String name = getSTName(index, 4);
			int r = lookupSymbolTable(name);
			if (r == -1) {
				System.err.println("符号表无该标识符，未定义就使用");
			} else {
				genCode("=", fuZhiMap.get("E"), "-", name);
				setTokenArray(index, 3);
			}
		} else if (g.equals("E->E+T")) {
			String t = newTemp();
			genCode("+", fuZhiMap.get("E"), fuZhiMap.get("T"), t);
			fuZhiMap.put("E", t);
			setTokenArray(index, 2);
		} else if (g.equals("E->T")) {
			fuZhiMap.put("E", fuZhiMap.get("T"));
		} else if (g.equals("T->T*F")) {
			String t = newTemp();
			genCode("*", fuZhiMap.get("T"), fuZhiMap.get("F"), t);
			fuZhiMap.put("T", t);
			setTokenArray(index, 2);
		} else if (g.equals("T->F")) {
			fuZhiMap.put("T", fuZhiMap.get("F"));
		} else if (g.equals("F->(E)")) {
			fuZhiMap.put("F", fuZhiMap.get("E"));
			setTokenArray(index, 2);
		} else if (g.equals("F->d")) {
			fuZhiMap.put("F", tokens.get(index - 1).getField());
		} else if (g.equals("F->i")) {
			String name = getSTName(index, 1);
			int r = lookupSymbolTable(name);
			if (r == -1) {
				System.err.println("符号表无该标识符，未定义就使用");
			} else {
				fuZhiMap.put("F", name);
			}
		} else if (g.equals("F->(E)")) {

		} else if (g.equals("F->(E)")) {

		} else if (g.equals("F->(E)")) {

		}
	}

	/**
	 * 生成一个新的临时变量t,返回t的地址
	 * 
	 * @return
	 */
	public static String newTemp() {
		String t = "t" + (temp.size() + 1);
		temp.add(t);
		return t;
	}

	/**
	 * 生成三地址指令
	 * 
	 * @param code
	 */
	public static void genCode(String op, String arg1, String arg2, String result) {
		SiYuanShi s = new SiYuanShi(op, arg1, arg2, result);
		code.add(s);
	}

	/**
	 * 从token序列中获得符号表的入口名字
	 * 
	 * @param index
	 * @return
	 */
	public static String getSTName(int index, int before) {
		while (before > 0) {
			if (tokensArray[--index] == 0) {
				before--;
			}
		}
		String field = tokens.get(index).getField(); // (1, STIndex: 1)
		return field.substring(field.indexOf(":") + 2);
	}

	/**
	 * 根据规约的产生式设置token的使用情况
	 * 
	 * @param index
	 * @param before
	 */
	private static void setTokenArray(int index, int before) {
		while (before > 0) {
			if (tokensArray[--index] != 1) {
				tokensArray[index] = 1;
				before--;
			}
		}
	}

	/**
	 * 返回符号表
	 * 
	 * @return
	 */
	public static List<SymbolTable> getSymbolTables() {
		List<SymbolTable> res = new ArrayList<SymbolTable>();
		for (String key : symbolTable.keySet()) {
			res.add(symbolTable.get(key));
		}
		return res;
	}

	/**
	 * 查找符号表
	 * 
	 * @param symbol
	 * @return 不存在该符号返回-1， 存在返回其index
	 */
	private static int lookupSymbolTable(String symbol) {
		if (symbolTable.containsKey(symbol)) {
			return Integer.valueOf(symbolTable.get(symbol).getIndex());
		}
		return -1;
	}

	/**
	 * 设置tokens
	 * 
	 * @param tokens
	 */
	public static void setTokens(List<Token> tokens) {
		SemanticTranslate.tokens.clear();
		SemanticTranslate.tokens.addAll(tokens);
		tokensArray = new int[tokens.size()];
	}

	/**
	 * 返回生成的四元式
	 * 
	 * @return
	 */
	public static String getCode() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < code.size(); i++) {
			res.append(code.get(i).toString() + "\n");
		}
		return res.toString();
	}
}
