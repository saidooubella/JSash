package utils;

import java.io.*;

public final class SourceFile {

	private final File file, parent;

	public final String path;

	public SourceFile(File file, File parent) {
		this.path = getPath(file);
		this.parent = parent;
		this.file = file;
	}

	public Result<SourceFile> fromParent(String child) {
		return fromFile(new File(parent, child));
	}

	public Reader toReader() {
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	public static Result<SourceFile> from(String path)  {
		return fromFile(new File(path));
	}

	@Override
	public String toString() {
		return file.toString();
	}

	private static Result<SourceFile> fromFile(final File file) {

		if (!file.exists() || !file.isFile())
			return Result.message("There is no such file in this path");

		if (!file.canRead())
			return Result.message("Cannot read from the file");

		return Result.value(new SourceFile(file, file.getParentFile()));
	}

	private static String getPath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
}
