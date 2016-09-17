/**
 * 
 */

/**
 * @author Ahmed Metwally
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;





public class TestParsing {
	 
	//use ASTParse to parse string
	public static ASTVisitorMod parse(char[] str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3); //JLS8
		parser.setSource(str);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ASTVisitorMod visitor= new ASTVisitorMod();
		cu.accept(visitor);
				
		/* Display content using Iterator*/
//		  Set<?> set = visitor.oprt.entrySet();
//		  Iterator<?> iterator = set.iterator();
//		  while(iterator.hasNext()) {
//		     Map.Entry mentry = (Map.Entry)iterator.next();
//		     System.out.print("Operator key is: "+ mentry.getKey() + " & Value is: ");
//		     System.out.println(mentry.getValue());
//		  }
//		  
//		  
//		  set = visitor.names.entrySet();
//		  iterator = set.iterator();
//		  while(iterator.hasNext()) {
//		     Map.Entry mentry = (Map.Entry)iterator.next();
//		     System.out.print("Names key is: "+ mentry.getKey() + " & Value is: ");
//		     System.out.println(mentry.getValue());
//		  }
//		  
//		  
//		  set = visitor.declaration.entrySet();
//		  iterator = set.iterator();
//		  while(iterator.hasNext()) {
//		     Map.Entry mentry = (Map.Entry)iterator.next();
//		     System.out.print("Declaration key is: "+ mentry.getKey() + " & Value is: ");
//		     System.out.println(mentry.getValue());
//		  }
//		  
		  
//		  System.out.println("Distinct oprt="+visitor.oprt.size());
//		  System.out.println("Distinct names="+visitor.names.size()); 
		  
		  
		  
		  return visitor;
	}
	
		
	
	// read file content into a string
	public static char[] ReadFileToCharArray(String filePath) throws IOException {
		// check the ability of reading the huge files base don the below numbers 
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
 
		return  fileData.toString().toCharArray();	
	}

	

 
	
	
	public static List<String> retrieveFiles(String directory) {
		List<String> textFiles = new ArrayList<String>();
		File dir = new File(directory);
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith((".java"))) {
				textFiles.add(file.getAbsolutePath());
			}
		}
		return textFiles;
	}
	

	public static List<char[]> ParseFilesInDir(String Dir) throws IOException{
		List<String> JavaFiles=retrieveFiles(Dir);
		List<char[]> FilesRead= new ArrayList<char []>();
		for(int i=0; i<JavaFiles.size(); i++)
		{
			System.out.println("Processing ################################= "+ JavaFiles.get(i));
			FilesRead.add(ReadFileToCharArray(JavaFiles.get(i)));
			
//			parse(ReadFileToCharArray(JavaFiles.get(i)));			
//			ParseFilesInDir(JavaFiles.get(i));
		}
		
		return FilesRead;


	}
	
	
	public static void main(String[] args) throws IOException {	
		/// Test public, non static, protected.
		// Check if the file does not exist
		// check if the java code is not compiled
		/// LambdaExpression is added in JLS8 API
		
		/// catch enhanced for and if condition for the : 
		/// catch switch for the :
		/// negative numbers counted as 
		/// the program count import statements as Operands
		/// Should we add the operators in different scopes as two different things
		

		//String DirName=null;
		//Scanner user_input = new Scanner( System.in );
		//System.out.print("Enter Directory Name: ");
		//DirName = user_input.next( );
		//System.out.println("DirName is: " + DirName);
		
		
		
//		parse(ReadFileToCharArray("C:/Users/Ahmed/Downloads/whatswrong-0.2.3/src/main/java/com/googlecode/whatswrong/WhatsWrongWithMyNLP.java"));
//		String Dir="/home/hady/Dropbox/JavaWorkSpace/CalcHalstead/datasets";		
//		/home/hady/Dropbox/JavaWorkSpace/CalcHalstead/src/main/java
		String Dir="/home/hady/Dropbox/JavaWorkSpace/CalcHalstead/src/main/java";
//		String Dir="/home/hady/Downloads/pfaat/src/com/neogenesis/pfaat";

		
		List<char[]> FilesRead=ParseFilesInDir(Dir);	
		ASTVisitorMod ASTVisitorFile;		
		int DistinctOperators=0;
		int DistinctOperands=0;
		int TotalOperators=0;
		int TotalOperands=0;
		  
		for(int i=0; i<FilesRead.size(); i++)
		{	
			System.out.println("################### File= "+ i);
			
			ASTVisitorFile=parse(FilesRead.get(i));
			DistinctOperators+=ASTVisitorFile.oprt.size();
			DistinctOperands+=ASTVisitorFile.names.size();			
			for (int f : ASTVisitorFile.oprt.values()) {
				TotalOperators += f;
			}
			for (int f : ASTVisitorFile.names.values()) {
				TotalOperands += f;
			}
			
			
			System.out.println("DistinctOperators is: "+ DistinctOperators);
			System.out.println("DistinctOperands is: "+ DistinctOperands);
			System.out.println("TotalOperators is: "+ TotalOperators);
			System.out.println("TotalOperands is: "+ TotalOperands);	
		}
		
		
		
		
		//Test HalsteadMetrics
		 System.out.println("Halstead Complexity Measures");
		  HalsteadMetrics hal = new HalsteadMetrics();
		 
		  hal.setParameters(DistinctOperators, DistinctOperands, TotalOperators, TotalOperands);
		  hal.getVocabulary();
		  hal.getProglen();
		  hal.getCalcProgLen();
		  hal.getVolume();
		  hal.getDifficulty();
		  hal.getEffort();
		  hal.getTimeReqProg();
		  hal.getTimeDelBugs();
			
	}
}