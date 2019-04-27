package ex3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ex1.SymbolTable;
import ex1.Token;
import ex2.Constant;

public class SemanticAnalysis {

	private static int offset = 0;	//空间分配偏移量
	public static Map<String, SymbolTable> symbolTable = new LinkedHashMap<String, SymbolTable>(); // 符号表
	private static Map<String, Declaration> declarMap = new LinkedHashMap<String, Declaration>(); // 记录声明语句中的属性
	private static Map<String, Stack<String>> fuZhiMap = new LinkedHashMap<String, Stack<String>>(); // 记录赋值语句中的属性
	private static Map<String, Stack<BoolExpression>> boolMap = new LinkedHashMap<String, Stack<BoolExpression>>(); // 记录布尔表达式的属性
	private static Stack<Integer> mStack = new Stack<Integer>(); // 布尔表达式中的 M
	private static Map<String, Stack<Sentiment>> controlMap = new LinkedHashMap<String, Stack<Sentiment>>(); // 控制流语句中的属性
	private static List<String> temp = new ArrayList<String>(); // 临时变量
	public static List<Token> tokens = new ArrayList<Token>(); // 词法获得的token序列
	private static StringBuilder console = new StringBuilder(); // 控制台打印
	private static List<SiYuanShi> code = new ArrayList<SiYuanShi>(); // 四元式结果
	private static int[] tokensArray;

	/**主分析函数，根据产生式执行相应的语义动作
	 * @param g
	 * @param index
	 */
	public static void trans(String g, int index) {
		if (g.equals("X->t")) { // 声明语句的翻译
			Declaration declar = new Declaration("X", "integer", 4);
			declarMap.put("X", declar);
		} else if (g.equals("X->r")) {
			Declaration declar = new Declaration("X", "real", 8);
			declarMap.put("X", declar);
		} else if (g.equals("D->Xi;")) {
			String name = getSTName(index, 2);
			SymbolTable st = symbolTable.get(name);
			if (st.getAttribute() == null) {
				symbolTable.get(name).setAttribute(declarMap.get("X").getType());
				symbolTable.get(name).setAddress((offset + ""));
				offset += declarMap.get("X").getWidth();
			} else { // 变量重复声明
				Token token = getTokens(index, 2);
				String error = String.format("Error at Line %d Col %d: re-declare(%s)\n", token.getRow(),
						token.getCol(), name);
				console.append(error);
			}
			setTokenArray(index, 2);
		} else if (g.equals("S->i=E;")) { // 赋值语句的翻译
			String E = fuZhiMap.get("E").pop();
			String name = getSTName(index, 4);
			int r = lookupSymbolTable(name);
			if (r == -1 || symbolTable.get(name).getAttribute() == null) { // 变量引用前未声明
				Token token = getTokens(index, 4);
				String error = String.format("Error at Line %d Col %d: Not declare before reference(%s)\n",
						token.getRow(), token.getCol(), name);
				console.append(error);
			} else if (symbolTable.get(name).getAttribute().equals("integer") && symbolTable.get(E).getAttribute().equals("real")) {
				//检查赋值语句是否出现类型不匹配，比如real 赋给integer
				Token token = getTokens(index, 4);
				String error = String.format("Error at Line %d Col %d: Cannot convert from real to integer(%s)\n",
						token.getRow(), token.getCol(), name);
				console.append(error);
			}
			genCode("=", E, "-", name); // 错误处理策略：仅做错误提示，仍可继续分析
			setTokenArray(index, 3);
			controlMap.get("S").push(new Sentiment());
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
			Token token = tokens.get(index - 1); // TODO
			String t = newConstantTemp(token.getType(), token.getField());
			fuZhiMap.get("F").push(t);
		} else if (g.equals("F->i")) {
			String name = getSTName(index, 1);
			int r = lookupSymbolTable(name);
			if (r == -1 || symbolTable.get(name).getAttribute() == null) { // 变量引用前未声明
				Token token = getTokens(index, 1);
				String error = String.format("Error at Line %d Col %d: Not declare before reference(%s)\n",
						token.getRow(), token.getCol(), name);
				console.append(error);
			}
			fuZhiMap.get("F").push(name); // 错误处理策略：仅做错误提示，仍可继续分析
		} else if (g.equals("C->a")) { // 布尔表达式的翻译
			BoolExpression C = new BoolExpression();
			C.setTrueList(makeList(nextQuad()));
			boolMap.get("C").push(C);
			genCode("j", "-", "-", "-");
		} else if (g.equals("C->b")) {
			BoolExpression C = new BoolExpression();
			C.setFalseList(makeList(nextQuad()));
			boolMap.get("C").push(C);
			genCode("j", "-", "-", "-");
		} else if (g.equals("C->(B)")) {
			BoolExpression B = boolMap.get("B").pop();
			BoolExpression C = new BoolExpression(B.getTrueList(), B.getFalseList());
			boolMap.get("C").push(C);
			setTokenArray(index, 2);
		} else if (g.equals("C->zC")) {
			BoolExpression zC = boolMap.get("C").pop();
			BoolExpression C = new BoolExpression(zC.getFalseList(), zC.getTrueList());
			boolMap.get("C").push(C);
			setTokenArray(index, 1);
		} else if (g.equals("C->ERE")) {
			BoolExpression C = new BoolExpression();
			C.setTrueList(makeList(nextQuad()));
			C.setFalseList(makeList(nextQuad() + 1));
			String e2addr = fuZhiMap.get("E").pop();
			String e1addr = fuZhiMap.get("E").pop();
			boolMap.get("C").push(C);
			String relop = Constant.zbm.get(getTokens(index, 2).getType());
			genCode("j" + relop, e1addr, e2addr, "-");
			genCode("j", "-", "-", "-");
			setTokenArray(index, 2);
		} else if (g.equals("A->C")) {
			BoolExpression C = boolMap.get("C").pop();
			BoolExpression A = new BoolExpression(C.getTrueList(), C.getFalseList());
			boolMap.get("A").push(A);
		} else if (g.equals("B->A")) {
			BoolExpression A = boolMap.get("A").pop();
			BoolExpression B = new BoolExpression(A.getTrueList(), A.getFalseList());
			boolMap.get("B").push(B);
		} else if (g.equals("B->BxMA")) {
			BoolExpression B = new BoolExpression();
			BoolExpression B1 = boolMap.get("B").pop();
			BoolExpression A = boolMap.get("A").pop();
			backPactch(B1.getFalseList(), mStack.pop());
			B.setTrueList(merge(B1.getTrueList(), A.getTrueList()));
			B.setFalseList(A.getFalseList());
			boolMap.get("B").push(B);
			setTokenArray(index, 2);
		} else if (g.equals("A->AyMC")) {
			BoolExpression A = new BoolExpression();
			BoolExpression A1 = boolMap.get("A").pop();
			BoolExpression C = boolMap.get("C").pop();
			backPactch(A1.getTrueList(), mStack.pop());
			A.setTrueList(C.getTrueList());
			A.setFalseList(merge(A1.getFalseList(), C.getFalseList()));
			boolMap.get("A").push(A);
			setTokenArray(index, 2);
		} else if (g.equals("M->ε")) {
			mStack.push(nextQuad());
		} else if (g.equals("S->fBgMSNeMS")) { // 控制流语句的回填
			Sentiment S = new Sentiment();
			Sentiment S2 = controlMap.get("S").pop();
			Sentiment S1 = controlMap.get("S").pop();
			Sentiment N = controlMap.get("N").pop();
			Integer M2 = mStack.pop();
			Integer M1 = mStack.pop();
			BoolExpression B = boolMap.get("B").pop();
			backPactch(B.getTrueList(), M1);
			backPactch(B.getFalseList(), M2);
			S.setNextList(merge(merge(S1.getNextList(), N.getNextList()), S2.getNextList()));
			controlMap.get("S").push(S);
			setTokenArray(index, 5);
		} else if (g.equals("S->wMBvMS")) {
			Sentiment S = new Sentiment();
			Integer M2 = mStack.pop();
			Integer M1 = mStack.pop();
			BoolExpression B = boolMap.get("B").pop();
			backPactch(controlMap.get("S").pop().getNextList(), M1);
			backPactch(B.getTrueList(), M2);
			S.setNextList(B.getFalseList());
			genCode("j", "-", "-", M1 + "");
			controlMap.get("S").push(S);
			setTokenArray(index, 3);
		} else if (g.equals("N->ε")) {
			Sentiment N = new Sentiment();
			N.setNextList(makeList(nextQuad()));
			genCode("j", "-", "-", "-");
			controlMap.get("N").push(N);
		} else if (g.equals("P->SMP")) {
			backPactch(controlMap.get("S").pop().getNextList(), mStack.pop());
		}
	}

	public static void init() {
		String[] fzN = new String[] { "F", "T", "E" };
		for (int i = 0; i < fzN.length; i++) {
			fuZhiMap.put(fzN[i], new Stack<String>());
		}
		String[] boolN = new String[] { "B", "A", "C" };
		for (int i = 0; i < boolN.length; i++) {
			boolMap.put(boolN[i], new Stack<BoolExpression>());
		}
		String[] controlN = new String[] { "S", "N", "P" };
		for (int i = 0; i < controlN.length; i++) {
			controlMap.put(controlN[i], new Stack<Sentiment>());
		}
	}

	/**
	 * 创建一个只包含i的列表，i是跳转指令的标号
	 * 
	 * @param i
	 * @return 新列表的指针
	 */
	private static List<Integer> makeList(int i) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(i);
		return list;
	}

	/**
	 * 合并两个列表
	 * 
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

	/**
	 * 将i作为目标标号插入到p所指列表中的各指令中
	 * 
	 * @param list
	 * @param i
	 */
	private static void backPactch(List<Integer> list, Integer i) {
		for (int j = 0; j < list.size(); j++) {
			code.get(list.get(j) - 1).setResult(i + "");
		}
	}

	/**
	 * 为常数新建一个临时变量
	 * 
	 * @param type  种别码值
	 * @param field 值
	 * @return
	 */
	public static String newConstantTemp(int type, String field) {
		String t = "t" + (temp.size() + 1);
		temp.add(t);
		if (type == Constant.zbm.indexOf("uint")) {
			SymbolTable st = new SymbolTable(symbolTable.size() + 1 + "", t, "常数", "integer", field, offset + "", "");
			symbolTable.put(t, st);
			offset += 4;
		} else {
			SymbolTable st = new SymbolTable(symbolTable.size() + 1 + "", t, "常数", "real", field, offset + "", "");
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
		String attr1 = symbolTable.get(e1).getAttribute(); // 计算该新建的临时变量的类型
		String attr2 = symbolTable.get(e2).getAttribute();
		String t = "t" + (temp.size() + 1);
		temp.add(t);
		if (attr1.equals("real") || attr2.equals("real")) {
			SymbolTable st = new SymbolTable(symbolTable.size() + 1 + "", t, "临时变量", "real", "", offset + "", "");
			symbolTable.put(t, st);
			offset += 8;
		} else {
			SymbolTable st = new SymbolTable(symbolTable.size() + 1 + "", t, "临时变量", "integer", "", offset + "", "");
			symbolTable.put(t, st);
			offset += 4;
		}
		return t;
	}

	/**
	 * 返回下一个代码式标号
	 * 
	 * @return
	 */
	public static int nextQuad() {
		return code.size() + 1;
	}

	/**
	 * 生成三地址指令
	 * 
	 * @param code
	 */
	public static void genCode(String op, String arg1, String arg2, String result) {
		SiYuanShi s = new SiYuanShi(nextQuad(), op, arg1, arg2, result);
		code.add(s);
	}

	/**
	 * 从token序列中获得符号表的入口名字
	 * 
	 * @param index
	 * @return
	 */
	public static String getSTName(int index, int before) {
		Token token = getTokens(index, before);
		String field = token.getField(); // (1, STIndex: 1)
		return field.substring(field.indexOf(":") + 2);
	}

	/**
	 * 从token序列中获得某个token
	 * 
	 * @param index
	 * @param before
	 * @return
	 */
	public static Token getTokens(int index, int before) {
		while (before > 0) {
			if (tokensArray[--index] == 0) {
				before--;
			}
		}
		return tokens.get(index);
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
		SemanticAnalysis.tokens.clear();
		SemanticAnalysis.tokens.addAll(tokens);
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

	/**
	 * 清空
	 */
	public static void clear() {
		SemanticAnalysis.code.clear();
		SemanticAnalysis.console.delete(0, console.length());
		SemanticAnalysis.offset = 0;
	}

	/**
	 * 返回控制台信息
	 * 
	 * @return
	 */
	public static String getConsole() {
		return console.toString();
	}
}
