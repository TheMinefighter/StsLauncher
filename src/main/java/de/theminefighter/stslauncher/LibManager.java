package de.theminefighter.stslauncher;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Manages the libs needed, because they are not included in newer java versions
 */
public class LibManager {

	/**
	 * makes an array of all Libs needed
	 * @return an array of URLS to all jar Libs needed
	 */
	public static URL[] makeLibUrls() {
		InputStream mavenStream = ClassLoader.getSystemClassLoader().getResourceAsStream("de/theminefighter/stslauncher/MavenList.csv");
		assert mavenStream != null; //Safe as the resource is integrated into this project
		Stream<String> lines = new BufferedReader(new InputStreamReader(mavenStream)).lines();
		return lines.map(LibManager::makeMvnUrl).toArray(URL[]::new);
	}

	/**
	 * Shows all License files in a given Collection of jars
	 * @param files a Collection of urls to jars to extract license files from
	 */
	public static void showLicenseFiles(Collection<File> files) {
		PrintStream out = System.out;
		for (File file :
				files) {
			showLicenseFile(out, file);
		}
	}

	static void showLicenseFile(PrintStream out, File file) {
		try {
			ZipFile zf = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zf.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) continue;
				if (entry.getName().toLowerCase().contains("license")) {
					InputStream lcs = zf.getInputStream(entry);
					InputStreamReader lcsr = new InputStreamReader(lcs);
					BufferedReader blcsr = new BufferedReader(lcsr);
					String line;
					while ((line = blcsr.readLine()) != null) {
						out.println(line);
					}
					out.println("=============================================");
					blcsr.close();
					lcsr.close();
					lcs.close();
				}
			}
			zf.close();
		} catch (IOException e) {
			out.printf("An error occurred whilst looking for License files in %s%n", file);
		}
	}

	/**
	 * Makes a mvn url for the jar download of a given mvn package
	 * @param mvnCsvLine a string consisting of the groupId artifactId and version separated by commas.
	 * @return a mvn url for the jar download of the given mvn package
	 */
	private static URL makeMvnUrl(String mvnCsvLine) {
		String[] split = mvnCsvLine.split(",");
		try {
			return new URL(String.format("https://repo.maven.apache.org/maven2/%s/%s/%s/%s-%s.jar",
					split[0].replace('.', '/'), split[1], split[2], split[1], split[2]));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
