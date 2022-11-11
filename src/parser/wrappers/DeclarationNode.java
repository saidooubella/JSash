package parser.wrappers;

import span.Spannable;
import span.TextSpan;
import tokens.Token;
import utils.SeparatedList;

import java.util.Optional;

public interface DeclarationNode extends Spannable {

	final class Simple implements DeclarationNode {

		private final TextSpan span;

		public final LabelNode label;
		public final TypeNode type;

		public Simple(LabelNode label, TypeNode type) {
			this.span = label.span().plus(type.span());
			this.label = label;
			this.type = type;
		}

		@Override
		public TextSpan span() {
			return span;
		}
	}

	final class Distructure implements DeclarationNode {

		private final TextSpan span;

		public final SeparatedList<DeclarationNode, Token> declarations;
		public final Optional<LabelNode> label;
		public final Token closeParent;
		public final Token openParent;

		public Distructure(Optional<LabelNode> label, Token openParent, SeparatedList<DeclarationNode, Token> declarations, Token closeParent) {
			this.span = openParent.span().plus(closeParent.span());
			this.declarations = declarations;
			this.closeParent = closeParent;
			this.openParent = openParent;
			this.label = label;
		}

		@Override
		public TextSpan span() {
			return span;
		}
	}
}
