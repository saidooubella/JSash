package tokens;

public enum TokenType {
    // Punctuation
    Plus, Minus, Star, Slash, OpenParent,
    CloseParent, Equal, Colon, OpenCurly,
    CloseCurly, Comma, Arrow, Pipe, Bang,
    AmpersandAmpersand, CloseAngleEqual,
    OpenAngleEqual, CloseAngle, Question,
    OpenAngle, EqualEqual, BangEqual, Dot,
    PipePipe, DoubleQuote, DollarSign,
    // Identifier & keywords
    Identifier, DefKeyword, FunKeyword,
    ReturnKeyword, NoneKeyword,
    LetKeyword, RecordKeyword,
    TrueKeyword, FalseKeyword,
    SelfKeyword,
    // Literals
    Number, String,
    // End of file
    EOF
}
