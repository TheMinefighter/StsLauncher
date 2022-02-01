package de.theminefighter.stslauncher;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Some Java utility methods
 */
public class JavaUtilities {
	/**
	 * Determines the path of the java executable running
	 *
	 * @return the path of the java executable running
	 */
	public static String getJavaPath() {
		return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
	}

	/**
	 * Gets the Location of the StsLauncher Code
	 * @return A file (jar or dir) where the compiled code of this is located
	 */
	public static File getStsLauncherLocation() {
		try {
			return new File(JavaUtilities.class.getProtectionDomain().getCodeSource().getLocation()
					.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("This does not happen");
		}
	}

	/**
	 * Checks if StsLauncher is running from a jar
	 * @return Whether StsLauncher is running from a jar
	 */
	public static boolean isJar() {
		return !getStsLauncherLocation().isDirectory();
	}
}
