package compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

class Lexer{
	private String lexeme;
	private LinkedList<Integer> stack=new LinkedList<>();		//ջ
	private Input i;
	private int state;                                          //��ǰ״̬
	private Map<Integer,String> type;							//ÿ��״̬����Ӧ�Ľ�������
	private String token;										//token��
	private Set<String> keyword;								//�ؼ��ֱ�
	private boolean hasEnter;									//��ǰ��ȡ�ĵ���ǰ���лس�
	
	//����ǰ״̬��ջ
	private void push() {
		if(isAccept())
			stack.clear();
		stack.addFirst(state);
	}
	
	//��ջ
	private int pop() {
		return stack.removeFirst();
	}
	
	//��ǰ״̬�Ƿ��ǽ���״̬
	private boolean isAccept() {
		return type.get(state)!=null;
	}
	
	//ɾ���ַ���β���ַ�
	private void truncate() {
		lexeme=lexeme.substring(0,lexeme.length()-1);
	}
	
	//��ȡһ���ַ���������ӵ�lexeme��
	private char nextChar() {
		char c=i.nextChar();
		lexeme=lexeme+c;
		return c;
	}
	
	//��ʶ��ת���
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
					return 1;//�����ת���
			}
			lexeme=lexeme+c;
			return 2;//��ȷ��ת���
		}
		else
			lexeme=lexeme+c;
		return c;
	}
	
	//�жϵ�ǰ�ַ��Ƿ�����ĸ���»���
	private boolean isLetter(char c) {
		return 96<c&&c<123||64<c&&c<91||c==95;
	}
	
	//�жϵ�ǰ�ַ��Ƿ�������
	private boolean isNum(char c) {
		return 47<c&&c<58;
	}
	
	//�жϵ�ǰ�ַ��Ƿ���һ����
	private boolean isDelimiter1(char c) {
		return c=='!'||c=='='||c=='/'||c=='*'||c=='%'||c=='^';
	}
	
	//�жϵ�ǰ�ַ��Ƿ��Ƕ�����
	private boolean isDelimiter2(char c) {
		return c=='<'||c=='>';
	}
	
	//�жϵ�ǰ�ַ��Ƿ���������
	private boolean isDelimiter3(char c) {
		return c=='+'||c=='-'||c=='&'||c=='|';
	}
	
	//�жϵ�ǰ�ַ��Ƿ���������
	private boolean isDelimiter4(char c) {
		return c==','||c=='.'||c==';'||c=='['||c==']'||c=='('||c==')'||c=='{'||c=='}'||c=='?'||c==':';
	}
	//�ж��Ƿ����ע��
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
	
	//��ȡ�����ַ�
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
	
	//״̬-3����ֹ����123abcʶ��Ϊ<123,int><abc,identifier>�Ĵ���
	private void wrongIdentifier(char c) {
		state=-3;
		push();
		while(isLetter(c)||isNum(c)) {
			c=nextChar();
		}
		other(c);
	}
	
	//״̬-4��-5�������쳣��char
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
	
	//״̬-6�����������ַ���
	private void wrongString() {
		char c=nextChar();
		state=-6;
		push();
		truncate();
		other(c);
	}
	
	//״̬S1
	private void S1() {
		char c=nextChar();
		state=1;
		push();
		while(isLetter(c)||isNum(c)) {
			c=nextChar();
		}
		other(c);
	}
	
	//״̬S2
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
	
	//״̬S3
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
	
	//״̬S4
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
	
	//״̬S5
	private void S5() {
		char c=nextChar();
		state=5;
		push();
		if(c=='=')
			S6();
		else
			other(c);
	}
	
	//״̬S6
	private void S6() {
		char c=nextChar();
		state=6;
		push();
		other(c);
	}
	
	//״̬S7
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
	
	//״̬S8
	private void S8(char pre) {
		char c=nextChar();
		state=8;
		push();
		if(c==pre)
			S6();
		else
			other(c);
	}
	
	//״̬S9
	private void S9() {
		char c=nextCharNeedEscape();
		state=9;
		push();
		if(c!='\''&&c!=13&&c!='\n'&&c!=1)
			S10();
		else
			wrongChar(c);
	}
	
	//״̬S10
	private void S10() {
		char c=nextChar();
		state=10;
		push();
		if(c=='\'')
			S11();
		else
			wrongChar(c);
			
	}
	
	//״̬S11
	private void S11() {
		char c=nextChar();
		state=11;
		push();
		other(c);
	}
	
	//״̬S12
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
	
	//״̬S13
	private void S13() {
		char c=nextChar();
		state=state!=-7?13:-7;
		push();
		other(c);
	}
	
	//״̬S_out���Ե�ǰ������ַ������д���ѡ���������
	private void S_out() {
		while(!isAccept()&&state!=-1) {
			state=pop();
			truncate();
			i.rollBack();
		}
		
		//�����﷨������
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

	//�޷�ʶ����ַ�
	private void unrecognize(char c) {
		if(c=='\n')
			hasEnter=true;
		if(c!=13&&c!=' '&&c!='\n')
			System.out.println("Error: unrecognized "+lexeme);
	}
	
	//��ʼ���ؼ��ֱ�
	private void putKeyword() {
		String key="auto|int|double|long|char|float|short|signed|unsigned|struct|union|enum|static|switch|case|default|break|register|const|volatile|typedef|extern|return|void|continue|do|while|if|else|for|goto|sizeof|import"; 
		String[] keyArray=key.split("\\|"); 
		for (String s:keyArray)
			keyword.add(s);	 
	}
	
	//��ʼ��type��
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
	
	//�ӿ���̨��ȡ����
	public Lexer() {
		this("");
	}
	
	//���ļ��ж�ȡ����
	public Lexer(String fileName) {
		lexeme="";
		stack.addFirst(-1);			//��-1��ʶջ�Ľ�β
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
			push();//����ʼ����״̬����
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
	
	//�﷨��������
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

	����״̬��-1������ջβ��
		   -2�����벻���ܵ��ַ���
		   -3�����ܵ�ǰlexeme����������Ϊ����ı�ʶ����
		   -4,-5:���ܵ�ǰlexeme����������Ϊ�����char�ͣ�
		   -6�����ܵ�ǰlexeme����������Ϊ�����string��
		   -7:�����ת���
	
	ע�⣺char=0��ʾ�����ļ���β��
 
	
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















