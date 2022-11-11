package parser.wrappers;

import parser.statements.ExtensionStatementNode;
import parser.statements.FunctionStatementNode;
import parser.statements.RecordStatementNode;
import parser.statements.VariableStatementNode;

import java.util.List;

public final class FileRootNode {
	
	public final List<ExtensionStatementNode> extensions;
	public final List<FunctionStatementNode> functions;
	public final List<VariableStatementNode> variables;
	public final List<RecordStatementNode> records;

	public FileRootNode(List<ExtensionStatementNode> extensions, List<FunctionStatementNode> functions, List<VariableStatementNode> variables, List<RecordStatementNode> records) {
		this.extensions = extensions;
		this.functions = functions;
		this.variables = variables;
		this.records = records;
	}
}
