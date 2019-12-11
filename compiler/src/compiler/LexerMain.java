package compiler;

class LexerMain {
	public static void main(String[] args) {
		/*
		 * Def：变量的声明或初始化；
		 * Assign：表达式语句；
		 * Import:import 语句；
		 */
		String s="Goal";
		String p="Goal:Def"
			+ 		 "|Import"
			+        "|#"
			+  		 "$"
			+    "Stmt:If_stmt"
			+        "|While_stmt"
			+        "|Block"
			+  		 "|Assign ;"
			+        "|break ;"
			+        "$"
			+    "If_stmt:if ( Expr ) O_If Block O_Else Else_stmt$"
			+    "Else_stmt:else Block"
			+             "|#"
			+             "$"
			+    "While_stmt:while ( Expr ) O_While Block O_EndWhile$"
			+    "Import:import Name ;$"
			+    "Name:identifier Name'$"
			+    "Name':. identifier Name'"
			+         "|#"
			+ 		  "$"
			+    "Def:Static Def'$"
			+    "Def':Type O_Insert identifier EorF"
			+        "|void identifier Fun"
			+        "|struct identifier MemList ;"
			+        "|union identifier MemList ;"
			+        "$"
			+    "Block_def:Static Block_def'$"
			+    "Block_def':Type identifier Equel ; "
			+        "|void identifier Fun"
			+        "|struct identifier MemList ;"
			+        "|union identifier MemList ;"
			+        "$"
			+    "EorF:Equel ;"
			+        "|Fun"
			+ 		 "$"
			+    "Fun:( Para ) Block$"
			+    "Para:void"
			+        "|Type identifier Para'"
			+        "|#"
			+        "$"
			+    "Para':, Para''"
			+         "|#"
			+         "$"
			+    "Para'':Type identifier Para'"
			+          "|. . ."
			+          "$"
			+    "MemList:{ Mem }"
			+           "|identifier"
			+           "$"
			+    "Mem:Type identifier ; Mem"
			+       "|#"
			+       "$"
			+    "Block:{ Block' }"
			+         "|;"
			+         "$"
			+    "Block':Block_def Block'"
			+          "|Stmt Block'"
			+          "|Assign ; Block'"
			+          "|#"
			+          "$"
			+    "Assign:identifier Equel$"
			+    "Static:static"
			+          "|#"
			+          "$"
			+    "Type:Type' TypeRef$"
			+    "TypeRef:[ TypeRef' TypeRef"
			+           "|* TypeRef"
			+           "|( ParaType ) TypeRef"
			+           "|#"
			+           "$"
			+    "TypeRef':intNum ]"
			+            "|]"
			+            "$"
			+    "ParaType:void"
			+            "|Type ParaType'"
			+            "|#"
			+            "$"
			+    "ParaType':, ParaType''"
			+             "|#"
			+             "$"
			+    "ParaType'':Type ParaType'"
			+              "|. . ."
			+              "$"
			+    "Type':unsigned O_Type Type''"
			+         "|O_Type Type''"
			+         "$"
			+    "Type'':int|double|long|char|float|short$"
			+    "Equel:= Expr O_equel Equel'"
			+ 	      "|Equel'"
			+         "|#"
			+         "$"
			+    "Equel':, O_Insert identifier Equel"
			+ 	       "|#"
			+ 		   "$"
			+    "Expr:Term Expr'$"
			+    "Expr':+ Term O_add Expr'"
			+         "|- Term O_sub Expr'"
			+         "|#"
			+ 		  "$"
			+    "Term:Factor Term'$"
			+    "Term':* Factor O_mult Term'"
			+         "|/ Factor O_div Term'"
			+         "|#"
			+ 		  "$"
			+    "Factor:( Expr )"
			+          "|intNum"
			+          "|doubleNum"
			+          "|O_LookUp identifier";
		Parser aParser=new Parser(s,p);
        aParser.grmAnalysis();
	}
}
