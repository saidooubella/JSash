package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;
import utils.SeparatedList;

public abstract class TypeNode implements Spannable {

	private final TextSpan span;

	private TypeNode(TextSpan span) {
		this.span = span;
	}

	@Override
	public final TextSpan span() {
		return span;
	}
	
	public static final class Basic extends TypeNode {

		public final Token name;

		public Basic(Token name) {
			super(name.span());
			this.name = name;
		}
	}
	
	public static final class Union extends TypeNode {
		
		public final TypeNode left;
		public final Token pipe;
		public final TypeNode right;

		public Union(TypeNode left, Token pipe, TypeNode right) {
			super(left.span().plus(right.span()));
			this.left = left;
			this.pipe = pipe;
			this.right = right;
		}
	}
	
	public static final class Tuple extends TypeNode {

		public final Token openParent;
		public final SeparatedList<TypeNode, Token> types;
		public final Token closeParent;
		
		public Tuple(Token openParent, SeparatedList<TypeNode, Token> types, Token closeParent) {
			super(openParent.span().plus(closeParent.span()));
			this.closeParent = closeParent;
			this.openParent = openParent;
			this.types = types;
		}
	}
	
	public static final class Function extends TypeNode {
		
		public final Token openParent;
		public final SeparatedList<TypeNode, Token> parameters;
		public final Token closeParent;
		public final Token arrow;
		public final TypeNode returnType;

		public Function(Token openParent, SeparatedList<TypeNode, Token> parameters, Token closeParent, Token arrow, TypeNode returnType) {
			super(openParent.span().plus(returnType.span()));
			this.openParent = openParent;
			this.parameters = parameters;
			this.closeParent = closeParent;
			this.arrow = arrow;
			this.returnType = returnType;
		}
	}
	
	public static final class Parenthesized extends TypeNode {
		
		public final Token openParent;
		public final TypeNode type;
		public final Token closeParent;

		public Parenthesized(Token openParent, TypeNode type, Token closeParent) {
			super(openParent.span().plus(closeParent.span()));
			this.closeParent = closeParent;
			this.openParent = openParent;
			this.type = type;
		}
	}
}
