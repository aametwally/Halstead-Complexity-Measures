/**
 * 
 */

/**
 * @author Ahmed Metwally
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;



public class TestParsing {
	 
	//use ASTParse to parse string
	public static ASTVisitorMod parse(char[] str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setSource(str);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		
		// Check for compilation problems in the provided file
		IProblem[] problems = cu.getProblems();
		
		for(IProblem problem : problems) {
			
			// 1610613332 = Syntax error, annotations are only available if
            // source level is 5.0
            if (problem.getID() == 1610613332)
                continue;
            // 1610613332 = Syntax error, parameterized types are only
            // available if source level is 5.0
            else if (problem.getID() == 1610613329)
                continue;
            else if (problem.getID() == 1610613328) // 'for each' statements are only available if source level is 5.0
                continue;
            else 
            {
    	        System.out.println("problem Message " + problem.getMessage() + "  Start= "+problem.getSourceLineNumber() + "Problem ID="+ problem.getID());            	
            	System.exit(1);
            }
	    }
		
		ASTVisitorMod visitor= new ASTVisitorMod();
		cu.accept(visitor);
	  
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
		List<String> Files = new ArrayList<String>();
		File dir = new File(directory);
		
		if (!dir.isDirectory())
		{			
			 System.out.println("The provided path is not a valid directory");
			 System.exit(1);
		}
		
		
		for (File file : dir.listFiles()) {
			if(file.isDirectory())
			{
				Files.addAll(retrieveFiles(file.getAbsolutePath()));
			}
			if (file.getName().endsWith((".java"))) 
			{
				Files.add(file.getAbsolutePath());
			}
		}
		
		
		return Files;
	}
	

	public static List<char[]> ParseFilesInDir(List<String> JavaFiles) throws IOException{
		if(JavaFiles.isEmpty()) 
		{
			System.out.println("There is no java source code in the provided directory");
			System.exit(0);
		}
		
		List<char[]> FilesRead= new ArrayList<char []>();		
		
		for(int i=0; i<JavaFiles.size(); i++)
		{
			System.out.println("Now, Processing = "+ JavaFiles.get(i));
			FilesRead.add(ReadFileToCharArray(JavaFiles.get(i)));
		}
		
		return FilesRead;
	}
	
	
	public static void main(String[] args) throws IOException {
		String DirName=null;
		Scanner user_input = new Scanner( System.in );
		System.out.print("Enter Directory Name: ");
		DirName = user_input.next( );
		user_input.close();
		
		
		System.out.println("DirName is: " + DirName);
		List<String> JavaFiles=retrieveFiles(DirName);
		List<char[]> FilesRead=ParseFilesInDir(JavaFiles);	
		ASTVisitorMod ASTVisitorFile;		
		int DistinctOperators=0;
		int DistinctOperands=0;
		int TotalOperators=0;
		int TotalOperands=0;
		int OperatorCount=0;
		int OperandCount=0;
		  
		for(int i=0; i<FilesRead.size(); i++)
		{	
			
			System.out.println("Now Parsing File= "+ JavaFiles.get(i));			
			ASTVisitorFile=parse(FilesRead.get(i));
			DistinctOperators+=ASTVisitorFile.oprt.size();
			DistinctOperands+=ASTVisitorFile.names.size();			
			
			OperatorCount=0;
			for (int f : ASTVisitorFile.oprt.values()) {				
				OperatorCount+= f;			
			}
			TotalOperators+=OperatorCount;
			
			OperandCount=0;
			for (int f : ASTVisitorFile.names.values()) {
				OperandCount += f;
			}
			TotalOperands+=OperandCount;
			
			System.out.println("Distinct Operators= "+ ASTVisitorFile.oprt.size());
			System.out.println("Distinct Operands= "+ ASTVisitorFile.names.size());
			System.out.println("Total Operators= "+ OperatorCount);
			System.out.println("Total Operands= "+ OperandCount);		
		}
		
		
		System.out.println("Overall Distinct Operators= "+ DistinctOperators);
		System.out.println("Overall Distinct Operands= "+ DistinctOperands);
		System.out.println("Overall Total Operators= "+ TotalOperators);
		System.out.println("Overall Total Operands= "+ TotalOperands);
		
		
		
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