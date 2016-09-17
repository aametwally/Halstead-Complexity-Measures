/**
 * @author Ahmed Metwally
 *
 */

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;


public class ASTVisitorMod extends ASTVisitor{	
	public HashMap<String, Integer> names = new HashMap<String, Integer>();
	public HashMap<String, Integer> oprt = new HashMap<String, Integer>();
	public HashMap<String, Integer> declaration = new HashMap<String, Integer>();
	CompilationUnit compilation=null;
	
	
	
	// visit the infix expressions
	public boolean visit(InfixExpression node)
	{			
		if (!this.oprt.containsKey(node.getOperator().toString()))
		{
			this.oprt.put(node.getOperator().toString(), 1);
		}
		else
		{
			this.oprt.put(node.getOperator().toString(), this.oprt.get(node.getOperator().toString())+1);		
		}				
		return true;
	}

	
	
	public boolean visit(PostfixExpression node)
	{		
		if (!this.oprt.containsKey(node.getOperator().toString()))
		{
			this.oprt.put(node.getOperator().toString(), 1);
		}
		else
		{
			this.oprt.put(node.getOperator().toString(), this.oprt.get(node.getOperator().toString())+1);
		}	
		return true;
	}
	
	
	
	public boolean visit(PrefixExpression node)
	{		
		if (!this.oprt.containsKey(node.getOperator().toString()))
		{
			this.oprt.put(node.getOperator().toString(), 1);
		}
		else
		{
			this.oprt.put(node.getOperator().toString(), this.oprt.get(node.getOperator().toString())+1);
		}
		
		return true;
	}

	
	
	
	// visit Assignment statements to add the assignment operator to the hashmap. 
	public boolean visit(Assignment node)
	{			
		if (!this.oprt.containsKey(node.getOperator().toString()))
		{
			this.oprt.put(node.getOperator().toString(), 1);
		}
		else
		{
			this.oprt.put(node.getOperator().toString(), this.oprt.get(node.getOperator().toString())+1);
		}	
		
		return true;
	}
	
		
	public boolean visit(SingleVariableDeclaration node) {
		if(node.getInitializer()!=null)
		{
			if (!this.oprt.containsKey("="))
			{
				this.oprt.put("=", 1);
			}
			else
			{
				this.oprt.put("=", this.oprt.get("=")+1);
			}
		}
		
		return true;
	}
	
	
	public boolean visit(VariableDeclarationFragment node) {		
		
		if(node.getInitializer()!=null)
		{	
			if (!this.oprt.containsKey("="))
			{
				this.oprt.put("=", 1);
			}
			else
			{
				this.oprt.put("=", this.oprt.get("=")+1);
			}
		}
		
		return true;
	}

	
	
	
	
	public boolean visit(SimpleName node) {
		if (!this.names.containsKey(node.getIdentifier()))
		{
			this.names.put(node.getIdentifier(),1);
		}
		else
		{
			this.names.put(node.getIdentifier(), this.names.get(node.getIdentifier())+1);
		}		
		return true;
	}

	
	

	public boolean visit(NullLiteral node) {		
		if (!this.names.containsKey("null"))
		{
			this.names.put("null", 1);
		}
		else
		{
			this.names.put("null", this.names.get("null")+1);
		}
		
		return true;
	}
	
	
	public boolean visit(StringLiteral node) {	
		
		if (!this.names.containsKey(node.getLiteralValue()))
		{
			this.names.put(node.getLiteralValue(),1);
		}
		else
		{
			this.names.put(node.getLiteralValue(), this.names.get(node.getLiteralValue())+1);
		}
		return true;
	}
	
	
	public boolean visit(CharacterLiteral node) {			
		
		if (!this.names.containsKey(Character.toString(node.charValue())))
		{
			this.names.put(Character.toString(node.charValue()),1);
		}
		else
		{
			this.names.put(Character.toString(node.charValue()), this.names.get(Character.toString(node.charValue()))+1);
		}
		
		return true;
	}
	
	public boolean visit(BooleanLiteral node) {	
		
		if (!this.names.containsKey(Boolean.toString(node.booleanValue())))
		{
			this.names.put(Boolean.toString(node.booleanValue()),1);
		}
		else
		{
			this.names.put(Boolean.toString(node.booleanValue()), this.names.get(Boolean.toString(node.booleanValue()))+1);
		}
		
		
		return true;
	}
	
	
	
	public boolean visit(NumberLiteral node) {	
		if (!this.names.containsKey(node.getToken()))
		{
			this.names.put(node.getToken(),1);
		}
		else
		{
			this.names.put(node.getToken(), this.names.get(node.getToken())+1);
		}
		
		return true;
	}	
	
	// Override visit the compilationUnit to be able to retrieve the line numbers. 
		public boolean visit(CompilationUnit unit)
		{
			compilation=unit;
			return true;
		}
}		