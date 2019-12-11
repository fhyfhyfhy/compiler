package compiler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

class Input {
	private static final int BUFFERSIZE=1024;
	private static final byte[] BUFFER=new byte[2*BUFFERSIZE];	//缓存区
	String fileName;
	private int readPos;
	private int inputPos;
	private int fence;
	private int line;
	
	private void fillBuffer(int input) {
		if(fileName.isEmpty()) {
			Scanner in=new Scanner(System.in);
			String s=in.nextLine();
			int pos=0;
			int len=s.length();
			while(!s.equals("end")) {
				for(int i=0;i<s.length();i++) {
					BUFFER[pos]=(byte) s.charAt(i);
					pos++;
				}
				BUFFER[pos]='\n';
				pos++;
				s=in.nextLine();
				len+=s.length();
				if(len>=2*BUFFERSIZE) {
					System.out.println("输入数据过长，请改至文件输入！");
				}
			}
			in.close();
			/*
			for(byte b:BUFFER)
				System.out.print((char) b);
			*/
		}
		else {
			byte i[]=new byte[BUFFERSIZE];
			int readLen;
			try {
				RandomAccessFile file = new RandomAccessFile(fileName,"r");
				file.seek(readPos);
				readLen=file.read(i);
				readPos+=readLen;
				for(byte b:i) {
					BUFFER[input]=b;
					input++;
				}
				file.close();
			}
			catch(IOException e) {
				System.out.println("Error reading file: "+fileName);
			}
			//System.out.println("fill");
		}
	}
	
	public Input(String fileName) {
		readPos=0;
		inputPos=0;
		fence=0;
		this.fileName=fileName;
		fillBuffer(inputPos);
		line=1;
	}
	
	public char nextChar() {
		char c=(char) BUFFER[inputPos];
		if(c=='\n')
			line++;
		inputPos=Math.floorMod(inputPos+1,2*BUFFERSIZE);
		if(Math.floorMod(inputPos,BUFFERSIZE)==0) {
			fillBuffer(inputPos);
			fence=Math.floorMod(inputPos+BUFFERSIZE,2*BUFFERSIZE);
		}
		
		return c;
	}
	
	public void rollBack() {
		if(inputPos==fence)
			System.out.println("roll back error");
		inputPos=Math.floorMod(inputPos-1,2*BUFFERSIZE);
	}
	
	public int getLine() {
		return line;
	}
}
