package utils;

import tokens.TokenType;

public final class TokenTypes {
	
	private TokenTypes() { throw new UnsupportedOperationException(); }

	static {
		for (final TokenType type : TokenType.values()) {
			isStartOfExpression(type);
		}
	}

	public static boolean isStartOfExpression(TokenType type) {
		switch (type) {
			case ReturnKeyword:
			case FalseKeyword:
			case TrueKeyword:
			case NoneKeyword:
			case DoubleQuote:
			case SelfKeyword:
			case FunKeyword:
			case OpenParent:
			case Identifier:
			case String:
			case Number:
			case Minus:
			case Plus:
			case Bang: {
				return true;
			}
			case AmpersandAmpersand:
			case CloseAngleEqual:
			case OpenAngleEqual:
			case RecordKeyword:
			case CloseParent:
			case CloseAngle:
			case DefKeyword:
			case EqualEqual:
			case LetKeyword:
			case CloseCurly:
			case DollarSign:
			case OpenCurly:
			case OpenAngle:
			case BangEqual:
			case PipePipe:
			case Question:
			case Comma:
			case Arrow:
			case Slash:
			case Equal:
			case Colon:
			case Pipe:
			case Star:
			case Dot:
			case EOF: {
				return false;
			}
			default: {
				final String message = type + "'s case is missing";
				throw new IllegalStateException(message);
			}
		}
	}
}
