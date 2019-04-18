package ex2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class SyntaxAnalysis {

	private LinkedList<String> G = new LinkedList<String>(); // 文法
	private List<Character> VN = new ArrayList<Character>(); // 非终结符
	private List<Character> VT = new ArrayList<Character>(); // 终结符
	private Map<Character, Set<Character>> First = new HashMap<Character, Set<Character>>();// 非终结符的First集
	private List<List<String>> Clo = new ArrayList<List<String>>(); // 闭包
	private Map<String, String> Go = new LinkedHashMap<String, String>();// 转移函数
	private String[][] ACTION;
	private int[][] GOTO;
	private List<AnalysisState> analysisStates = new ArrayList<AnalysisState>();	//记录语法分析状态
	/**
	 * 从文件读入文法
	 * 
	 * @param path
	 */
	private void readGrammar(String path) {
		try {
			File file = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] str = gVT(line).replaceAll("\\s+", "").split("\\|");
				G.add(str[0]);
				for (int i = 1; i < str.length; i++) {
					G.add(str[0].substring(0, 3) + str[i]);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**对文法的终结符进行转换,多个字符转换为单字符
	 * @return
	 */
	private String gVT(String content) {
		for (String key : Constant.grammarTable.keySet()) {
			content = content.replaceAll(key, Constant.grammarTable.get(key));
		}
		return content;
	}
	/**
	 * 获取终结符和非终结符
	 */
	private void getVTVN() {
		for (int i = 0; i < G.size(); i++) {
			String g = G.get(i).replace("->", "");
			for (int j = 0; j < g.length(); j++) {
				char ch = g.charAt(j);
				if (ch >= 'A' && ch <= 'Z') { // 非终结符
					if (!VN.contains(ch)) {
						VN.add(ch);
					}
				} else {
					if (!VT.contains(ch)) {
						VT.add(ch);
					}
				}
			}
		}
		VT.add('#');
	}

	/**
	 * 获取非终结符的First集
	 * 
	 */
	private void getFirst() {
		for (int i = 0; i < VN.size(); i++) {
			First.put(VN.get(i), new HashSet<Character>());
		}
		for (Character ch : VT) {
			First.put(ch, new HashSet<Character>() {
				{
					add(ch);
				}
			});
		}
		int hash;
		do {
			hash = First.hashCode(); // 记录改变前的Fisrt集hashcode
			for (int i = 0; i < G.size(); i++) {
				char[] ch = G.get(i).toCharArray();
				int j = 3;
				while (j < ch.length) {
					if (VT.contains(ch[j])) { // 首字符是终结符
						First.get(ch[0]).add(ch[j]);
						break;
					} else { // 首字符是非终结符
						if (!First.get(ch[j]).contains('ε')) { // ε 不在这个非终结符的First集中
							First.get(ch[0]).addAll(First.get(ch[j]));
							break;
						} else { // ε 在这个非终结符的First集中
							Set<Character> set = new HashSet<Character>();
							set.addAll(First.get(ch[j]));
							set.remove('ε');
							First.get(ch[0]).addAll(set); // 将First-ε加入
							j++; // 取下一个非终结符
						}
					}
				}
				if (j == ch.length) {
					First.get(ch[0]).add('ε');
				}
			}
		} while (First.hashCode() != hash); // 判断是否有 First 集进行了改变
	}

	/**
	 * 获得产生式的项目集闭包
	 * 
	 */
	private void getClosure(List<String> set) {
		Queue<String> quene = new LinkedList<String>();
		quene.addAll(set);
		while (!quene.isEmpty()) {
			String str = quene.poll(); // I中的每一项 A->α.Bβ,a
			int j = str.indexOf(".") + 1; // 找到B
			char ch = str.charAt(j);
			if (VN.contains(ch)) { // B是非终结符
				String βa = str.substring(j + 1).replace(",", ""); // 取出βa
				Set<Character> βaFirst = getStrFirst(βa);
				for (int k = 0; k < G.size(); k++) {
					if (G.get(k).startsWith(ch + "")) { // 找到B起始的产生式
						for (Character c : βaFirst) {
							String s = ch + "->." + G.get(k).substring(3) + "," + c; // 构造B→.η,b
							if (!set.contains(s)) {
								set.add(s); // 加入I闭包中
								quene.add(s);
							}

						}
					}
				}
			}
		}
	}

	/**
	 * 获取字符串的First集
	 * 
	 * @param str
	 */
	private Set<Character> getStrFirst(String str) {
		Set<Character> res = new HashSet<Character>();
		char[] ch = str.toCharArray();
		int j = 0;
		while (j < ch.length) {
			if (VT.contains(ch[j])) { // 首字符是终结符
				res.add(ch[j]);
				break;
			} else { // 首字符是非终结符
				if (!First.get(ch[j]).contains('ε')) { // ε 不在这个非终结符的First集中
					res.addAll(First.get(ch[j]));
					break;
				} else { // ε 在这个非终结符的First集中
					Set<Character> set = new HashSet<Character>();
					set.addAll(First.get(ch[j]));
					set.remove('ε');
					res.addAll(set); // 将First-ε加入
					j++; // 取下一个非终结符
				}
			}
		}
		if (j == ch.length) { // 每个字符的first集都有ε
			res.add('ε');
		}
		return res;
	}

	/**
	 * 获取项目集规范族
	 * 
	 */
	private void getClo() {
		for (int i = 0; i < Clo.size(); i++) {
			for (char x : getGoX(i)) {
				List<String> set = getGoXNew(i, x); // 获得新产生式
				getClosure(set); // 获得其闭包
				if (!set.isEmpty()) {
					int r = listContain(set);
					if (r == -1) { // 项目集族中无该项目集
						Clo.add(set);
						Go.put("(I" + i + ", " + x + ")", "I" + (Clo.size() - 1));
					} else {
						Go.put("(I" + i + ", " + x + ")", "I" + r);
					}

				}
			}
		}

	}

	/**
	 * 判断项目集族中是否已经存在该项目集
	 * 
	 * @param set
	 * @return 若包含，返回下标，若不包含，返回-1
	 */
	private int listContain(List<String> set) {
		for (int i = 0; i < Clo.size(); i++) {
			if (Clo.get(i).size() == set.size() && Clo.get(i).containsAll(set)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获得GO(I,X)中的X字符集
	 * 
	 */
	private List<Character> getGoX(int I) {
		List<Character> res = new ArrayList<Character>();
		for (String s : Clo.get(I)) {
			int j = s.indexOf(".") + 1;
			if (s.charAt(j) != ',' && !res.contains(s.charAt(j))) { // 是非终结符
				res.add(s.charAt(j));
			}
		}
		res.remove(new Character('ε'));
//		res.re
		return res;
	}

	/**
	 * 获得Go(I,X)的新的产生式，即。后移一位
	 * 
	 * @return
	 */
	private List<String> getGoXNew(int I, char x) {
		List<String> res = new ArrayList<String>();
		for (String s : Clo.get(I)) {
			int j = s.indexOf(".") + 1;
			if (s.charAt(j) == x) {
				String str = s.substring(0, j - 1) + x + "." + s.substring(j + 1);
				if (!res.contains(str)) {
					res.add(str);
				}
			}
		}
		return res;
	}

	/**
	 * 获得分析表
	 * 
	 */
	private void getSTA() {
		ACTION = new String[Clo.size()][VT.size()];
		GOTO = new int[Clo.size()][VN.size()];
		for (int k = 0; k < Clo.size(); k++) {
			List<String> Ik = Clo.get(k);
			for (String s : Go.keySet()) { // (I0, S)=I1 (I11, R)=I13
				if (Integer.valueOf(s.substring(2, s.indexOf(","))) == k) {
					char x = s.charAt(s.lastIndexOf(")") - 1);
					if (VT.contains(x)) { // 终结符
						ACTION[k][VT.indexOf(x)] = "s" + Go.get(s).substring(1);
					} else { // 非终结符
						int j = Integer.valueOf(Go.get(s).substring(1));
						int a = VN.indexOf(x);
						GOTO[k][a] = j;
					}
				}
			}
			for (String s : Ik) {
				String str = s.substring(0, s.indexOf("."));
				if (G.contains(str)) {
					ACTION[k][VT.indexOf(s.charAt(s.length() - 1))] = "r" + G.indexOf(str);
				}
			}
			if (Ik.contains(G.get(0) + ".,#")) {
				ACTION[k][VT.indexOf('#')] = "acc";
			}

		}
	}

	/**
	 * 根据分析表进行语法分析
	 * 
	 * @param content
	 */
	public void run(String content) {
		Stack<Integer> stateStack = new Stack<Integer>(); // 状态栈
		Stack<Character> signStack = new Stack<Character>(); // 符号栈
		Queue<Character> buffer = new LinkedList<Character>(); // 缓冲区
		for (char c : content.toCharArray()) {
			buffer.add(c);
		}
		buffer.add('#');
		stateStack.push(0);
		signStack.push('#');
		int step=1;
		while (true) {
			int s = stateStack.peek(); // 栈顶状态
			char a = buffer.peek(); // 缓冲区首字符
			String op = ACTION[s][VT.indexOf(a)];
			AnalysisState state = new AnalysisState(step, stateStack.toString(), signStack.toString(), buffer.toString(), op);
			analysisStates.add(state);
			if (op == null) {
				System.err.println("分析出搓");
				break;
			}
			if (op.equals("acc")) { // 分析完成
				break;
			}
			int t = Integer.valueOf(op.substring(1)); // 下一个状态 st、rt
			if (op.contains("s")) { // 移进状态
				signStack.push(a);
				stateStack.push(t);
				buffer.poll();
			} else if (op.contains("r")) {
				String g = G.get(t); // 产生式 A→β
				int n = g.length() - 3;
				while (n-- > 0) { // 从栈顶弹出 β个字符
					signStack.pop();
					stateStack.pop();
				}
				char A = g.charAt(0);
				stateStack.push(GOTO[stateStack.peek()][VN.indexOf(A)]); // GOTO[t', A]入栈
				signStack.push(A); // A入栈
			}
			step++;
		}
	}

	/**
	 * 从图形界面的textArea读入文件
	 * 
	 */
	public void readGrammarFromContent(String content) {
		String[] line = gVT(content).split("\n");
		for (int j = 0; j < line.length; j++) {
			String[] str = line[j].replaceAll("\\s+", "").split("\\|");
			G.add(str[0]);
			for (int i = 1; i < str.length; i++) {
				G.add(str[0].substring(0, 3) + str[i]);
			}
		}
	}

	/**打印消息到控制台
	 * 
	 */
	private void printList() {
		System.out.println(printG());
		
		System.out.println("\nVN: ");
		for (int i = 0; i < VN.size(); i++) {
			System.out.print(VN.get(i) + " ");
		}
		System.out.println("\n\nVT: ");
		for (int i = 0; i < VT.size(); i++) {
			System.out.print(VT.get(i) + " ");
		}

		System.out.println("\n\nFirst:");
		System.out.println(printFirst());

		System.out.println("\n\nClosure: ");
		System.out.println(printItem());

		System.out.println("\n\nGo: " + Go.size());
		System.out.println(Go.toString());

		System.out.println(printSTA());
		
		for (int i =0; i < analysisStates.size();i++) {
			System.out.println(analysisStates.get(i));
		}
	}

	/**打印文法
	 * @return
	 */
	public String printG() {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < G.size(); i++) {
			res.append(i + ":\t" + G.get(i) + "\n");
		}
		return res.toString();
	}
	/**
	 * 打印First集
	 * 
	 * @return
	 */
	public String printFirst() {
		StringBuilder res = new StringBuilder();
		for (Character ch : First.keySet()) {
			res.append(ch + ": \t" + First.get(ch).toString() + "\n");
		}
		return res.toString();
	}

	/**
	 * 打印项目集
	 * 
	 * @return
	 */
	public String printItem() {
		List<List<String>> Clo1 = new ArrayList<List<String>>();
		for (int i = 0; i < Clo.size(); i++) {
			Clo1.add(new ArrayList<String>());
			List<String> css = new ArrayList<String>();
			String[] hjf = new String[Clo.get(i).size()];
			for (String s : Clo.get(i)) {
				String str = s.substring(0, s.indexOf(","));
				if (css.contains(str)) {
					hjf[css.indexOf(str)] += ("/" + s.substring(s.length()-1));
				} else {
					css.add(str);
					hjf[css.size()-1] = s.substring(s.length()-1);
				}
			}
			for (int j = 0; j < css.size();j++) {
				Clo1.get(i).add(css.get(j) + ", " + hjf[j]);
			}
		}
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < Clo1.size(); i++) {
			res.append(i + ":  ");
			for (String s : Clo1.get(i)) {
				res.append(s + "  ");
			}
			res.append("\n");
		}
		return res.toString();
	}

	/**
	 * 打印分析表
	 * 
	 * @return
	 */
	public String printSTA() {
		StringBuilder res = new StringBuilder();
		String st = "";
		for (int i = 1; i < VT.size(); i++) {
			st += ("\t\t");
		}
		res.append("\t\t\t\tACTION" + st + "GOTO\nI\t\t");
		for (int i = 0; i < VT.size(); i++) {
			res.append(VT.get(i) + "\t\t");
		}
		for (int i = 0; i < VN.size(); i++) {
			res.append(VN.get(i) + "\t\t");
		}
		res.append("\n");
		for (int i = 0; i < ACTION.length; i++) {
			res.append(i + "\t\t");
			for (int j = 0; j < ACTION[i].length; j++) {
				String str = ACTION[i][j] == null ? " " : ACTION[i][j];
				res.append(str + "\t\t");
			}
			for (int j = 0; j < GOTO[i].length; j++) {
				String str = GOTO[i][j] == 0 ? " " : GOTO[i][j] + "";
				res.append(str + "\t\t");
			}
			res.append("\n");
		}
		return res.toString();
	}
	
	/**返回语法分析的状态
	 * @return
	 */
	public List<AnalysisState> getAnalysisStates() {
		return analysisStates;
	}
	
	/**读入文法之后，可用来建分析表
	 * @param sa
	 */
	public void analysis(SyntaxAnalysis sa) {
		sa.init();
		sa.getVTVN();
		sa.getFirst();
		sa.getClosure(sa.Clo.get(0));
		sa.getClo();
		sa.getSTA();
	}

	/**对文法进行增广，初始化项目集I0
	 * 
	 */
	private void init() {
//		 判断是否需要增广文法
		String s = G.get(0).substring(0, 1);	//取出文法开始符号
		int count = 0 ;
		for (String str : G) {
			if (str.startsWith(s)) {
				count++;
			}
		}
		if (count>1) {	//文法开始符号出现在多个产生式左部,进行增广
			G.addFirst("A->"+s);
			Clo.add(new ArrayList<String>() {{add("A->."+s+",#");}});
		} else {
			Clo.add(new ArrayList<String>() {{add(s+"->." + G.get(0).substring(3)+",#");}});
		}

//		//全部增广
//		String s = G.get(0).substring(0, 1);	//取出文法开始符号
//		G.addFirst("A->"+s);
//		Clo.add(new ArrayList<String>() {{add("A->."+s+",#");}});

//		Clo.add(new ArrayList<String>() {
//			{
//				add("S->.E,#");
//			}
//		});
//		Clo.add(new ArrayList<String>() {{add("A->.S,#");}});
	}

	public static void main(String[] args) {
		SyntaxAnalysis sa = new SyntaxAnalysis();
//		sa.readGrammar("src/main/java/ex2/grammar");
//		sa.readGrammar("src/main/java/ex2/test");
//		sa.readGrammar("src/main/java/ex2/test3");
//		sa.readGrammar("src/main/java/ex2/fuzhi");
//		sa.readGrammar("src/main/java/ex2/kongzhi");
//		sa.readGrammar("src/main/java/ex2/shengming");
		sa.init();
		sa.getVTVN();
		sa.getFirst();
		sa.getClosure(sa.Clo.get(0));
		sa.getClo();
		sa.getSTA();
//		sa.run("i=i*d;");
//		sa.run("fambgae");
//		sa.run("ti;");
		sa.printList();
		
	}
}
