package com.cabify.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
	private static String base = "src/test/resources/";

	public static String loadFile(String path) {
		try {
			return new String(Files.readAllBytes(Paths.get(base, path)), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
