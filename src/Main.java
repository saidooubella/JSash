import errors.ErrorMessage;
import errors.ErrorReports;
import evaluator.Evaluator;
import utils.Compiler;
import utils.Result;
import utils.SourceFile;

public final class Main {

	public static void main(String[] args) throws Exception {
		Class.forName("utils.TokenTypes"); // required
		main();
	}

	public static void main() {

		final Result<SourceFile> result = SourceFile.from("/Users/soubella/Desktop/JSash/src/main.sash");
		
		if (result instanceof Result.Success) {
			
			final Compiler.Result compilerResult = Compiler.compile(((Result.Success<SourceFile>) result).value);

			if (compilerResult.reports.isEmpty()) {
				Evaluator.evaluate(compilerResult.statements, compilerResult.mainEntry);
				System.out.println("OK!");
			} else {
				printReports(compilerResult.reports);
			}
			
		} else {
			System.out.println("File Loading Error: " + ((Result.Failure<SourceFile>) result).message);
		}
	}

	private static void printReports(ErrorReports reports) {
		for (final ErrorMessage message : reports) {
			System.out.println(message);
		}
	}
}

