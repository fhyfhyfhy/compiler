package compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

class Lexer{
	private String lexeme;
	private LinkedList<Integer> stack=new LinkedList<>();		//栈
	private Input i;
	private int state;                                          //当前状态
	private Map<Integer,String> type;							//每个状态所对应的接受类型
	private String token;										//token表
	private Set<String> keyword;								//关键字表
	private boolean hasEnter;									//当前读取的单词前面有回车
	
	//将当前状态入栈
	private void push() {
		if(isAccept())
			stack.clear();
		stack.addFirst(state);
	}
	
	//出栈
	private int pop() {
		return stack.removeFirst();
	}
	
	//当前状态是否是接受状态
	private boolean isAccept() {
		return type.get(state)!=null;
	}
	
	//删除字符串尾部字符
	private void truncate() {
		lexeme=lexeme.substring(0,lexeme.length()-1);
	}
	
	//读取一个字符，并将其加到lexeme上
	private char nextChar() {
		char c=i.nextChar();
		lexeme=lexeme+c;
		return c;
	}
	
	//可识别转义符
	private char nextCharNeedEscape() {
		char c=i.nextChar();
		if(c=='\\') {
			c=i.nextChar();
			switch(c) {
				case('b'):
					c=8;break;
				case('t'):
					c=9;break;
				case('n'):
					c=10;break;
				case('r'):
					c=13;break;
				case('"'):
					c=34;break;
				case('\''):
					c=39;break;
				case('\\'):
					c=92;break;
				default:
					return 1;//错误的转义符
			}
			lexeme=lexeme+c;
			return 2;//正确的转义符
		}
		else
			lexeme=lexeme+c;
		return c;
	}
	
	//判断当前字符是否是字母或下划线
	private boolean isLetter(char c) {
		return 96<c&&c<123||64<c&&c<91||c==95;
	}
	
	//判断当前字符是否是数字
	private boolean isNum(char c) {
		return 47<c&&c<58;
	}
	
	//判断当前字符是否是一类界符
	private boolean isDelimiter1(char c) {
		return c=='!'||c=='='||c=='/'||c=='*'||c=='%'||c=='^';
	}
	
	//判断当前字符是否是二类界符
	private boolean isDelimiter2(char c) {
		return c=='<'||c=='>';
	}
	
	//判断当前字符是否是三类界符
	private boolean isDelimiter3(char c) {
		return c=='+'||c=='-'||c=='&'||c=='|';
	}
	
	//判断当前字符是否是四类界符
	private boolean isDelimiter4(char c) {
		return c==','||c=='.'||c==';'||c=='['||c==']'||c=='('||c==')'||c=='{'||c=='}'||c=='?'||c==':';
	}
	//判断是否读入注释
	private boolean isCommentSyntax(char c) {
		if(c=='/') {
			char n=nextChar();
			if(n=='/'||n=='*')
				return true;
			else { 
				i.rollBack();
				truncate();
			}
		}
		return false;
	}
	
	private void CommentSyntax() {
		i.rollBack();
		char c=i.nextChar();
		if(c=='*') {
			while(!((i.nextChar())=='*'&(i.nextChar())=='/')) 
				i.rollBack();
		}
		else if(c=='/') {
			while(i.nextChar()!='\n');
		}
	}
	
	//读取其它字符
	private void other(char c) {
		if(c==0) {
			truncate();
			S_out();
		}
		else {
			state=-2;
			S_out();
		}
	}
	
	//状态-3，防止将如123abc识别为<123,int><abc,identifier>的错误
	private void wrongIdentifier(char c) {
		state=-3;
		push();
		while(isLetter(c)||isNum(c)) {
			c=nextChar();
		}
		other(c);
	}
	
	//状态-4、-5，处理异常的char
	private void wrongChar(char c) {
		if(c==13||c=='\n') {
			truncate();
			c=nextChar();
			state=-4;
			push();
			other(c);
		}
		else {
			state= c==1?-7:-5;
			push();
			while(c!='\''&&c!=13&&c!='\n'&&c!=0) {
				c=nextChar();
			}
			if(c==0)
				other(c);
			else { 
				if(c==13||c=='\n')
					truncate();
				c=nextChar();
				other(c);
			}
		}

	}
	
	//状态-6，处理错误的字符串
	private void wrongString() {
		char c=nextChar();
		state=-6;
		push();
		truncate();
		other(c);
	}
	
	//状态S1
	private void S1() {
		char c=nextChar();
		state=1;
		push();
		while(isLetter(c)||isNum(c)) {
			c=nextChar();
		}
		other(c);
	}
	
	//状态S2
	private void S2() {
		char c=nextChar();
		state=2;
		push();
		while(isNum(c)) {
			c=nextChar();
		}
		if(c=='.') {
			S3();
		}
		else if(isLetter(c)) {
			wrongIdentifier(c);
		}
		else 
			other(c);
	}
	
	//状态S3
	private void S3() {
		char c=nextChar();
		state=3;
		push();
		if(isNum(c)) {
			S4();
		}
		else{
			wrongIdentifier(c);
		}
	}
	
	//状态S4
	private void S4() {
		char c=nextChar();
		state=4;
		push();
		while(isNum(c)) {
			c=nextChar();
		}
		if(isLetter(c)) {
			wrongIdentifier(c);
		}
		else
			other(c);
	}
	
	//状态S5
	private void S5() {
		char c=nextChar();
		state=5;
		push();
		if(c=='=')
			S6();
		else
			other(c);
	}
	
	//状态S6
	private void S6() {
		char c=nextChar();
		state=6;
		push();
		other(c);
	}
	
	//状态S7
	private void S7(char pre) {
		char c=nextChar();
		state=7;
		push();
		if(c==pre)
			S5();
		else if(c=='=')
			S6();
		else
			other(c);
	}
	
	//状态S8
	private void S8(char pre) {
		char c=nextChar();
		state=8;
		push();
		if(c==pre)
			S6();
		else
			other(c);
	}
	
	//状态S9
	private void S9() {
		char c=nextCharNeedEscape();
		state=9;
		push();
		if(c!='\''&&c!=13&&c!='\n'&&c!=1)
			S10();
		else
			wrongChar(c);
	}
	
	//状态S10
	private void S10() {
		char c=nextChar();
		state=10;
		push();
		if(c=='\'')
			S11();
		else
			wrongChar(c);
			
	}
	
	//状态S11
	private void S11() {
		char c=nextChar();
		state=11;
		push();
		other(c);
	}
	
	//状态S12
	private void S12() {
		char c=nextCharNeedEscape();
		state=12;
		push();
		while(c!='"'&&c!=13&&c!='\n'&&c!=0) {
			c=nextCharNeedEscape();
			if(c==1) {
				state=-7;
				push();
			}
		}
		if(c=='"')
			S13();
		else 
			wrongString();
	}
	
	//状态S13
	private void S13() {
		char c=nextChar();
		state=state!=-7?13:-7;
		push();
		other(c);
	}
	
	//状态S_out，对当前读入的字符串进行处理，选择接收类型
	private void S_out() {
		while(!isAccept()&&state!=-1) {
			state=pop();
			truncate();
			i.rollBack();
		}
		
		//用于语法分析器
		if(state==1) 
			token=keyword.contains(lexeme) ? lexeme : "identifier";
		else if(state==5||state==6||state==7||state==8)
			token=lexeme;
		else {
			token=type.getOrDefault(state,"invalid");
			}//System.out.println("TYPE:"+token+"  state:"+state);
		
	}
	
	private void printLexeme() {
		String t;
		t=type.getOrDefault(state,"invalid");
		if(state==1) {
			t=keyword.contains(lexeme) ? "keyword" : "identifier";
		}
		System.out.println("< "+lexeme+" , "+t+" >");
	}

	//无法识别的字符
	private void unrecognize(char c) {
		if(c=='\n')
			hasEnter=true;
		if(c!=13&&c!=' '&&c!='\n')
			System.out.println("Error: unrecognized "+lexeme);
	}
	
	//初始化关键字表
	private void putKeyword() {
		String key="auto|int|double|long|char|float|short|signed|unsigned|struct|union|enum|static|switch|case|default|break|register|const|volatile|typedef|extern|return|void|continue|do|while|if|else|for|goto|sizeof|import"; 
		String[] keyArray=key.split("\\|"); 
		for (String s:keyArray)
			keyword.add(s);	 
	}
	
	//初始化type表
	private void putType() {
		type.put(-3,"wrong identifier");
		type.put(-4, "wrong char");
		type.put(-5, "wrong char");
		type.put(-6, "wrong string");
		type.put(-7, "wrong escape");
		type.put(1,"identifier");
		type.put(2, "intNum");
		type.put(4, "doubleNum");
		type.put(5, "delimiter");
		type.put(6, "delimiter");
		type.put(7, "delimiter");
		type.put(8, "delimiter");
		type.put(10, "wrong char");
		type.put(11, "char");
		type.put(13, "string");
	}
	
	//从控制台读取内容
	public Lexer() {
		this("");
	}
	
	//从文件中读取内容
	public Lexer(String fileName) {
		lexeme="";
		stack.addFirst(-1);			//用-1标识栈的结尾
		i=new Input(fileName);
		state=0;
		type=new HashMap<>();
		token="";
		keyword=new HashSet<>();
		hasEnter=false;
		putKeyword();
		putType();
	}
	
	public void recognize() {
		char c;
		while(true) {
			lexeme="";
			c=nextChar();
			if(c==0)
				break;
			push();//将初始的零状态放入
			if(isLetter(c)) {
				S1();
				printLexeme();
			}
			else if(isNum(c)) {
				S2();
				printLexeme();
			}
			else if(isCommentSyntax(c)) {
				CommentSyntax();
				printLexeme();
			}
			else if(isDelimiter1(c)) {
				S5();
				printLexeme();
			}
			else if(isDelimiter2(c)) {
				S7(c);
				printLexeme();
			}
			else if(isDelimiter3(c)) {
				S8(c);
				printLexeme();
			}
			else if(isDelimiter4(c)) {
				S6();
				printLexeme();
			}
			else if(c=='\'') {
				S9();
				printLexeme();
			}
			else if(c=='"') {
				S12();
				printLexeme();
			}
			else
				unrecognize(c);
		}
	}
	
	//语法分析器用
	public String nextWord() {
		char c;
		token="";
		while("".equals(token)) {
			lexeme="";
			c=nextChar();
			if(c==0)
				return "EOF";
			push();
			if(isLetter(c)) 
				S1();
			else if(isNum(c))
				S2();
			else if(isCommentSyntax(c))
				CommentSyntax();
			else if(isDelimiter1(c))
				S5();
			else if(isDelimiter2(c))
				S7(c);
			else if(isDelimiter3(c))
				S8(c);
			else if(isDelimiter4(c))
				S6();
			else if(c=='\'')
				S9();
			else if(c=='"')
				S12();
			else
				unrecognize(c);
		}
		return token;
	}
	
	public String getLexeme() {
		return lexeme;
	}
	
	public int getLine() {
		return i.getLine();
	}
	
	public boolean lineFirst() {
		return hasEnter;
	}
}
/*

	错误状态：-1：到达栈尾；
		   -2：读入不接受的字符；
		   -3：接受当前lexeme，但将其标记为错误的标识符；
		   -4,-5:接受当前lexeme，但将其标记为错误的char型；
		   -6：接受当前lexeme，但将其标记为错误的string型
		   -7:错误的转义符
	
	注意：char=0表示到达文件结尾。
 
	
	          <-letter/num-
	          \           /
	           \         /
	            \       / 
	             \     /
	              \   /  
	0----letter---->1----other---->identifier
	|               ^
	|
	|        <--num---                    <--num---
	|        \       /                    \       / 
	|         \     /                      \     /
	|          \   /                        \   /                     
	+----num---->2----point---->3---->num---->4---->other---->double
	|            ^              |             ^
	|            |              |             |
	|            |            other         letter
	|            |              |             |
	|            +----letter--->+------------>+-------->-3---->other---->wrong identifier
	|            |                                     / ^ \
	|            |                                    /     \
	|            |                                   /       \
	|            +----other---->int                 /         \
	|                                              /           \
	|                                              -letter/num->
	|
    |
	+------- ! = / * % ^ ------->5---- = ---->6----other---->delimiter
	|                            ^            ^       
	|                            |            |       
	+---- < > ---->7---- < > ----+            |       
	|              ^                          |  
	|              |                          |
	|              +------------ = ---------->+
	|                                         |
	|                                         |       
	+------ + - & | ---->8---- + - & | ------>+
	|                    ^                    |
	|                                         |       
	+-------- , . ; [ ] ( ) { } ? : ----------->+ 
	|
	|
	|            +---- ' \n ---->-4----other---->wrong char
	|            |                ^
	+---- ' ---->9----not ' \n ---->10---- ' ---->11----other---->char
	|                               ^             ^
    |						        |  
    |                               +---- \n ---->wrong char
    |                               |
    |                               +----not ' \n ---->-5---- ' \n ---->wrong char
    |                                                / ^ \
	|      <--not " \n--                            /     \
	|      \           /                           /       \
	|		\         /                           /         \
	|        \       /                           /           \
	|         \     /                            --not ' \n-->
	|          \   / 
	+---- " ---->12---- " ---->13----other---->string
	             |             ^
	             |
	             +---- \n ---->-6----other---->wrong string
	                           ^               
*/















