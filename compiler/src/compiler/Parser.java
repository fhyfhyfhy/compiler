package compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class Parser {
	private HashSet<String> T;
	private HashSet<String> NT;
	private String S;
	private HashSet<String> P;
	private HashMap<String,HashSet<String>> first;
	private HashMap<String,HashSet<String>> follow;
	private HashMap<String,HashSet<String>> select;
	private HashMap<String,Integer> map;
	private HashMap<Integer,String> pMap;
	private int[][] table;
	private Lexer lex;
	private String word;
	private HashMap<String,String> propertyMap;		//ÿ���﷨���Ź���������
	private LinkedList<String> SEM;		//����ջ
	private LinkedList<String> ILOC;	//��Ԫ��
	private int registerNum;			//ȫ�ּĴ������
	private int blockNum;				//��������
	private int level;					//���
	
	//��õ�ǰ���ŵ�first��
	private HashSet<String> getFirst(String beta) {
		if(first.get(beta)==null)
			System.out.println("not find:"+beta);
		return first.get(beta);
	}
	
	//��õ�ǰ���ŵ�follow��
	private HashSet<String> getFollow(String beta) {
		if(follow.get(beta)==null)
			System.out.println("not find:"+beta);
		return follow.get(beta);
	}
	
	//��õ�ǰ���ŵ�select��
	private HashSet<String> getSelect(String p) {
		if(select.get(p)==null)
			System.out.println("not find:"+p);
		return select.get(p);
	}
	
	//����һ��û��Epsilon�ļ���
	private HashSet<String> removeEpsilon(HashSet<String> s) {
		HashSet<String> newS=new HashSet<String>();
		if(!s.isEmpty()) {
			Iterator<String> it = s.iterator();  
	        while(it.hasNext()){ 
	        	String n=it.next();
	            if(!n.equals("#"))
	                newS.add(n);  
	        }
        }
        return newS;
	}
	
	//��tableԪ��
	private void putTable(String nt,String t,String p) {
		if(map.get(nt)==null)
			System.out.println("not find nt:"+nt+"!");
		if(map.get(t)==null)
			System.out.println("not find nt:"+t+"!");
		table[map.get(nt)][map.get(t)]=map.get(p);
	}

	//��ȡ����Ԫ��
	private int getTable(String nt,String t) {
		if(map.get(nt)==null||map.get(t)==null)
			return -1;
		return table[map.get(nt)][map.get(t)];
	}
	
	//��first������������ݹ飩
	private void buildFirst() {
		for(String t:T) {
			HashSet<String> f=new HashSet<>();
			f.add(t);
			first.put(t, f);
		}
		for(String nt:NT) {
			HashSet<String> f=new HashSet<>();
			first.put(nt,f);
		}
		HashSet<HashMap<String,HashSet<String>>> temp=new HashSet<>();
		while(!temp.contains(first)) {
			temp.clear();
			temp.add(first);
			HashSet<String> rhs=new HashSet<String>();
			int i;
			for(String p:P) {
				String A=p.substring(0,p.indexOf(":"));
				if(!NT.contains(A))
					System.out.print("wrong NT/P");
				String[] beta=p.substring(p.indexOf(":")+1,p.length()).split(" "); //ÿ���Ƴ�ʽ��ÿ���ַ���ɵ�����
				rhs.clear();
				rhs.addAll(removeEpsilon(getFirst(beta[0])));
				for(i=0;getFirst(beta[i]).contains("#")&&i<beta.length-1;i++)
					rhs.addAll(removeEpsilon(getFirst(beta[i+1])));
				if(i==beta.length-1&&getFirst(beta[beta.length-1]).contains("#"))
					rhs.add("#");
				if(!first.containsKey(p)) {
					HashSet<String> v=new HashSet<String>();
					first.put(p, v);
				}
				first.get(p).addAll(rhs);
				getFirst(A).addAll(rhs);
			}
		}
	}
	
	//��follow������ֹ��ݹ飩
	private void buildFollow() {
		for(String nt:NT) {
			HashSet<String> flw=new HashSet<>();
			follow.put(nt, flw);
		}
		HashSet<String> eof=new HashSet<>();
		eof.add("EOF");
		follow.put(S,eof);
		HashSet<HashMap<String,HashSet<String>>> temp=new HashSet<>();
		HashSet<String> trailer=new HashSet<>();
		while(!temp.contains(follow)) {
			temp.clear();
			temp.add(follow);
			for(String p:P) {
				String A=p.substring(0,p.indexOf(":"));
				if(!NT.contains(A))
					System.out.print("wrong NT/P");
				trailer.clear();
				trailer.addAll(getFollow(A));
				//p.replaceAll("\\sO_(.)*\\s"," ");
				String[] beta=p.substring(p.indexOf(":")+1,p.length()).split(" "); //ÿ���Ƴ�ʽ��ÿ���ַ�
				for(int i=beta.length-1;i>=0;i--) {
					if(NT.contains(beta[i])) {
						getFollow(beta[i]).addAll(trailer);
						if(getFirst(beta[i]).contains("#"))
							trailer.addAll(removeEpsilon(getFirst(beta[i])));
						else {
							trailer.clear();
							trailer.addAll(getFirst(beta[i]));
						}
					}
					else {
						trailer.clear();
						trailer.addAll(getFirst(beta[i]));
					}
				}
			}
		}	
	}
	
	//��select��
	private void buildSelect() {
		for(String p:P) {
			if(getFirst(p).contains("#")) {
				HashSet<String> se=new HashSet<>();
				se.addAll(getFirst(p));
				se.addAll(getFollow(p.substring(0,p.indexOf(":"))));
				select.put(p,se);
			}
			else
				select.put(p, getFirst(p));
		}
	}
	
	//Ϊÿһ��T��NT��P��Ӧһ������
	private void buildMap() {
		int i=0,j=0,k=0;
		for(String t:T) {
			if(!t.equals("#")) {
				map.put(t, i);
				i++;
			}
		}
		map.put("EOF", i);
		for(String nt:NT) {
			map.put(nt, j);
			j++;
		}
		for(String p:P) {
			map.put(p, k);
			pMap.put(k, p);
			k++;
		}
		map.put("error", -1);
		pMap.put(-1, "error");
	}
	
	//����LL(1)��
	private void buildTable() {
		table=new int[NT.size()][T.size()];
		for(String nt:NT) {
			for(String t:T) {
				if(!t.equals("#"))
					putTable(nt,t,"error");
			}
			putTable(nt,"EOF","error");
		}
		for(String p:P) {
			for(String w:getSelect(p)) {
				if(!w.equals("#"))
					putTable(p.substring(0,p.indexOf(":")),w,p);
			}
		}
	}

	//��ʼ����ʼ��
	private void addS(String s) {
		S=s;
	}
	
	//��ʼ���Ƶ�����
	private void addP(String p) {
		String[] pArr;	//��$�ָ����ַ�������
		String[] pA;	//��|�ָ����ַ�������
		String[] wA;	//�Կո�ָ����ַ�������
		String thisA;
		pArr=p.split("\\$");
		for(String s:pArr) {
			thisA=s.substring(0,s.indexOf(":"));
			NT.add(thisA);
			pA=s.substring(s.indexOf(":")+1,s.length()).split("\\|");
			for(String ss:pA) {
				wA=ss.split(" ");
				for(String word:wA) {
					if(!(T.contains(word)||NT.contains(word))){
						if(Character.isUpperCase(word.charAt(0))) {
							if(isOperation(word)) 
								P.add(word+":#");
							NT.add(word);
						}
						else
							T.add(word);
					}
				}
				P.add(thisA+":"+ss);
			}
		}
	}
	
	//�ж��Ƿ���һ����������
	private boolean isOperation(String s) {
		return s.substring(0,2).equals("O_");
	}
	
	//����Ĵ���
	private String assignReg() {
		registerNum++;
		return "t_"+registerNum;
	}
	
	//������
	private String assignTab() {
		blockNum++;
		return "L_"+blockNum;
	}
	
	//�﷨�Ƶ�����
	private void operate(String symbol) {
		String r2;
		String iloc;
		String reg;
		switch(symbol) {
		case "intNum":
			iloc="loadI,"+lex.getLexeme()+", ,"+"r_"+lex.getLexeme();
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			SEM.addFirst("r_"+lex.getLexeme());
			break;
		case "identifier":
			SEM.addFirst("r_"+lex.getLexeme());
			break;
		case "{":
			level++;
			break;
		case "}":
			level--;
			break;
		case "O_add":
		case "O_sub":
		case "O_mult":
		case "O_div":
			r2=SEM.removeFirst();
			reg=assignReg();
			iloc=symbol.substring(2,symbol.length())+","+SEM.removeFirst()+","+r2+","+reg;
			ILOC.addFirst(iloc);
			SEM.addFirst(reg);
			System.out.println("<"+iloc+">");
			break;
		case "O_equel":
			iloc="load,"+SEM.removeFirst()+", ,"+SEM.removeFirst();
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			break;
		case "O_LookUp":
			System.out.println("�ڷ��ű��в�ѯ"+lex.getLexeme());
			break;
		case "O_Insert":
			System.out.println("�ڷ��ű��в�ѯ"+lex.getLexeme()+",����ѯʧ������룬��ѯ�ɹ��򱨴�\"�ظ��������\"");
			break;
		case "O_Type":
			System.out.println("Type.value="+lex.getLexeme()+"    Type.value��propertyMap�д���");
			break;
		case "O_While":
		case "O_If":
			String L1=assignTab(),L2=assignTab();
			System.out.println("��ϵ������δʵ�֣���comp,r_x,r_y,flag");
			iloc="cbr_xx,flag,"+L1+","+L2;
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			if(symbol.equals("O_While")) {
				propertyMap.put("WhileCmp_"+level,"��ϵ������δʵ�֣���comp,r_x,r_y,flag");
				propertyMap.put("WhileCbr_"+level,iloc);
			}
			iloc=L1+", , , ";
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			propertyMap.put(symbol.substring(2,symbol.length())+"_"+level,L2);
			break;
		case "O_Else":
			iloc=propertyMap.get("If_"+level)+", , , ";
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			break;
		case "O_EndWhile":
			iloc=propertyMap.get("WhileCmp_"+level);
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			iloc=propertyMap.get("WhileCbr_"+level);
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			iloc=propertyMap.get("While_"+level)+", , , ";
			ILOC.addFirst(iloc);
			System.out.println("<"+iloc+">");
			break;
		}
	}
	
	//��������LL(1)�﷨����
	private boolean beginParse() {
		buildFirst();
		buildFollow();
		buildSelect();
		buildMap();
		buildTable();
		LinkedList<String> stack=new LinkedList<>();		//ջ
		word=lex.nextWord();
		if(word.equals("EOF"))
			return true;
		stack.addFirst("EOF");
		stack.addFirst(S);
		String focus=stack.getFirst();
		while(true) {
			if(focus.equals("EOF")&&word.equals("EOF")) {
				//System.out.println("success");
				return true;
			}
			else if((focus.equals(";")&&word.equals(";"))||(focus.equals("}")&&word.equals("}"))) {
				//������Ҫ�����﷨�Ƶ�����
				operate(word);
				if(stack.get(1).equals("EOF")) {
					//System.out.println("success2");
					return true;
				}
				else {
					stack.removeFirst();
					word=lex.nextWord();
				}
					
			}
			else if(T.contains(focus)||focus.equals("EOF")) {
				if(focus.equals(word)) {
					//������Ҫ�����﷨�Ƶ�����
					operate(word);
					stack.removeFirst();
					word=lex.nextWord();
				}
				else {
					System.out.println("Error: "+word+" in line "+lex.getLine()+" Need: "+focus);
					return false;
				}
			}
			else {
				//��Ҫ�����﷨�Ƶ�����
				if(isOperation(focus)) {
					operate(focus);
					stack.removeFirst();
				}
				else if(getTable(focus,word)!=-1) {
					stack.removeFirst();
					String p=pMap.get(getTable(focus,word));
					String[] beta=p.substring(p.indexOf(":")+1,p.length()).split(" ");
					for(int i=beta.length-1;i>=0;i--) {
						if(!beta[i].equals("#"))
							stack.addFirst(beta[i]);
					}
				}
				else {
					System.out.println("Error: "+word+" in line "+lex.getLine()+" Need: "+focus);
					return false;
				}
			}
			focus=stack.getFirst();
		}
	}
	
	//����ʼ������ȷ��ʽ��CFG�����﷨������
	public Parser(String s,String p) {
		T=new HashSet<>();
		NT=new HashSet<>();
		S="";
		P=new HashSet<>();
		first=new HashMap<>();
		follow=new HashMap<>();
		select=new HashMap<>();
		map=new HashMap<>();
		pMap=new HashMap<>();
		lex=new Lexer("test1.txt");
		word="";
		propertyMap=new HashMap<>();
		SEM=new LinkedList<>();
		ILOC=new LinkedList<>();
		registerNum=-1;
		blockNum=-1;
		level=0;
		addS(s);
		addP(p);
	}
		
	//�﷨����
	public void grmAnalysis() {
		while(true) {
			if(!beginParse()) {
				int lineNum=lex.getLine();
				while(!(word.equals(";")||word.equals("EOF")||lex.getLine()>lineNum))
					word=lex.nextWord();
			}
			if(word.equals("EOF")) {
				System.out.println("EOF");
				break;
			}
		}
	}
	
	//���first��
	public void printFirst() {
		buildFirst();
		//first.forEach((k,v)->System.out.println("first("+k+")="+v));
		for(String p:P) {
			String[] betaArr=p.substring(p.indexOf(":")+1,p.length()).split("\\|");
			for(String s:betaArr) {
				System.out.print("first( "+p.substring(0,p.indexOf(":"))+":"+s+" )={ ");
				for(String f:first.get(p.substring(0,p.indexOf(":"))+":"+s))
					System.out.print(f+" ");
				System.out.println("}");
			}
		}
	}
	
	//���follow��
	public void printFollow() {
		buildFirst();
		buildFollow();
		for(String nt:NT) {
			System.out.print("follow("+nt+")={ ");
			for(String s:follow.get(nt))
				System.out.print(s+" ");
			System.out.println("}");
		}
	}
	
	//���select��
	public void printSelect() {
		buildFirst();
		buildFollow();
		buildSelect();
		select.forEach((k,v)->System.out.println("select("+k+")="+v));
	}
	
	//���LL(1)��
	public void printTable() {
		buildFirst();
		buildFollow();
		buildSelect();
		buildMap();
		buildTable();
		for(String p:P)
			System.out.println(map.get(p)+" : "+p);
		System.out.println();
		for(String t:T) {
			if(!t.equals("#")) {
				if(t.length()>5)
					t=t.substring(0,5);
				System.out.print("\t"+t);
			}
		}
		System.out.print("\tEOF");
		System.out.println();
		for(String nt:NT) {
			System.out.print(nt);
			for(String t:T) {
				if(!t.equals("#"))
					System.out.print("\t"+getTable(nt,t));
			}
			System.out.print("\t"+getTable(nt,"EOF"));
			System.out.println();
		}
	}
}
