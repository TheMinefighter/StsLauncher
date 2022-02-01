package de.theminefighter.stslauncher;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class JavaUtilities {
	/**
	 * Determines the path of the java executable running
	 *
	 * @return the path of the java executable running
	 */
	public static String getJavaPath() {
		 /*File winAttempt = Paths.get(System.getProperty("java.home"), "bin", "java.exe").toFile();
		 if (winAttempt.exists()) return winAttempt.toString();*/
		//Just checked: also works w/o .exe  suffix under windows
		return Paths.get(System.getProperty("java.home"), "bin", "java").toFile().toString();
	}

	public static File getStsLauncherJarLocation() {
		try {
			return new File(JavaUtilities.class.getProtectionDomain().getCodeSource().getLocation()
					.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("This does not happen");
		}
	}

	public static boolean isJar() {
		return !getStsLauncherJarLocation().isDirectory();
	}
}
