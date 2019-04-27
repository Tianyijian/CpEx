package ex3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ex1.SymbolTable;
import ex1.Token;
import ex2.Constant;

public class SemanticTranslate {

	private static int offset = 0;
	public static Map<String, SymbolTable> symbolTable = new LinkedHashMap<String, SymbolTable>(); // 符号表
	private static Map<String, Declaration> declarMap = new LinkedHashMap<String, Declaration>(); // 记录声明语句中的属性
	private static Map<String, Stack<String>> fuZhiMap = new LinkedHashMap<String, Stack<String>>(); // 记录赋值语句中的属性
	private static Map<String, Stack<BoolExpression>> boolMap = new LinkedHashMap<String, Stack<BoolExpression>>();	//记录布尔表达式的属性
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
				genCode("=", fuZhiMap.get("E").pop(), "-", name);
				setTokenArray(index, 3);
			}
		} else if (g.equals("E->E+T")) {
			String E = fuZhiMap.get("E").pop();
			String T = fuZhiMap.get("T").pop();
			String t = newTemp(E, T);
			genCode("+", E, T, t);
			fuZhiMap.get("E").push(t);
			setTokenArray(index, 2);
		} else if (g.equals("E->T")) {
			fuZhiMap.get("E").push(fuZhiMap.get("T").pop());
		} else if (g.equals("T->T*F")) {
			String T = fuZhiMap.get("T").pop();
			String F = fuZhiMap.get("F").pop();
			String t = newTemp(T, F);
			genCode("*", T, F, t);
			fuZhiMap.get("T").push(t);
			setTokenArray(index, 2);
		} else if (g.equals("T->F")) {
			fuZhiMap.get("T").push(fuZhiMap.get("F").pop());
		} else if (g.equals("F->(E)")) {
			fuZhiMap.get("F").push(fuZhiMap.get("E").pop());
			setTokenArray(index, 2);
		} else if (g.equals("F->d")) {
			Token token = tokens.get(index - 1);
			String t = newConstantTemp(token.getType(), token.getField());
			fuZhiMap.get("F").push(t);
		} else if (g.equals("F->i")) {
			String name = getSTName(index, 1);
			int r = lookupSymbolTable(name);
			if (r == -1) {
				System.err.println("符号表无该标识符，未定义就使用");
			} else {
				fuZhiMap.get("F").push(name);
			}
		} else if (g.equals("F->(E)")) {

		} else if (g.equals("C->a")) {
			BoolExpression C = new BoolExpression();
			C.setTrueList(makeList(code.size()));
			boolMap.get("C").push(C);
			genCode("j", "-", "-", "-");
		} else if (g.equals("C->b")) {
			BoolExpression C = new BoolExpression();
			C.setFalseList(makeList(code.size()));
			boolMap.get("C").push(C);
			genCode("j", "-", "-", "-");
		} else if (g.equals("C->(B)")) {
			BoolExpression B = boolMap.get("B").pop();
			BoolExpression C = new BoolExpression(B.getTrueList(), B.getFalseList());
			boolMap.get("C").push(C);
		} else if (g.equals("C->zC")) {
			BoolExpression zC = boolMap.get("C").pop();
			BoolExpression C = new BoolExpression(zC.getFalseList(), zC.getTrueList());
			boolMap.get("C").push(C);
		} else if (g.equals("C->ERE")) {
			BoolExpression C = new BoolExpression();
			C.setTrueList(makeList(code.size()));
			C.setFalseList(makeList(code.size()+1));
			String e2addr = fuZhiMap.get("E").pop();
			String e1addr = fuZhiMap.get("E").pop();
			boolMap.get("C").push(C);
			genCode("jrelop", e1addr, e2addr, "-");
			genCode("j", "-", "-", "-");
		} else if (g.equals("A->C")) {
			BoolExpression C = boolMap.get("C").pop();
			BoolExpression A = new BoolExpression(C.getTrueList(), C.getFalseList());
			boolMap.get("A").push(A);
		} else if (g.equals("B->A")) {
			BoolExpression A = boolMap.get("A").pop();
			BoolExpression B = new BoolExpression(A.getTrueList(), A.getFalseList());
			boolMap.get("B").push(B);
		} else if (g.equals("B->BxA")) {
			
		} else if (g.equals("A->AyC")) {
			
		}
	}
	
	public static void init() {
		String[] fzN = new String[] {"F", "T", "E"};
		for (int i = 0; i < fzN.length;i++) {
			fuZhiMap.put(fzN[i], new Stack<String>());
		}
		String[] boolN = new String[] {"B", "A", "C"};
		for (int i = 0; i < boolN.length;i++) {
			boolMap.put(boolN[i], new Stack<BoolExpression>());
		}
	}

	/**创建一个只包含i的列表，i是跳转指令的标号
	 * @param i
	 * @return 新列表的指针
	 */
	private static List<Integer> makeList(int i) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(i);
		return list;
	}
	
	/** 合并两个列表
	 * @param l1
	 * @param l2
	 * @return 新列表的指针
	 */
	private static List<Integer> merge(List<Integer> l1, List<Integer> l2) {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(l1);
		list.addAll(l2);
		return list;
	}
	
	/**将i作为目标标号插入到p所指列表中的各指令中
	 * @param list
	 * @param i
	 */
	private static void backPactch(List<Integer> list, Integer i) {
		for (int j = 0; j < list.size();j++) {
			code.get(list.get(j)).setResult(i+"");
		}
	}
	/**生成一个用于存放标号的新的临时变量L，返回变量地址
	 * @return
	 */
	private static String newLabel() {
		String L = "L";
		return L;
	}
	
	/**将下一条三地址指令的标号赋给L
	 * @param L
	 */
	private static void label(String L) {
		
	}
	
	/**为常数新建一个临时变量
	 * @param type 种别码值
	 * @param field 值
	 * @return
	 */
	public static String newConstantTemp(int type, String field) {
		String t = "t" + (temp.size() + 1);
		temp.add(t);
		if (type == Constant.zbm.indexOf("uint")) {
			SymbolTable st = new SymbolTable(symbolTable.size()+ 1 +"", t, "临时变量", "integer", field, offset + "", "");
			symbolTable.put(t, st);
			offset += 4;
		} else {
			SymbolTable st = new SymbolTable(symbolTable.size()+ 1 +"", t, "临时变量", "real", field, offset + "", "");
			symbolTable.put(t, st);
			offset += 8;
		}
		return t;
	}
	/**
	 * 生成一个新的临时变量t,返回t的地址
	 * 
	 * @return
	 */
	public static String newTemp(String e1, String e2) {
		String attr1 = symbolTable.get(e1).getAttribute();		//计算该新建的临时变量的类型
		String attr2 = symbolTable.get(e2).getAttribute();
		String t = "t" + (temp.size() + 1);
		temp.add(t);
		if (attr1.equals("real") || attr2.equals("real")) {
			SymbolTable st = new SymbolTable(symbolTable.size()+ 1 +"", t, "临时变量", "real", "", offset + "", "");
			symbolTable.put(t, st);
			offset += 8;
		} else {
			SymbolTable st = new SymbolTable(symbolTable.size()+ 1 +"", t, "临时变量", "integer", "", offset + "", "");
			symbolTable.put(t, st);
			offset += 4;
		}
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
