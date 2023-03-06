package javax.jnlp.impl;

import de.theminefighter.stslauncher.JavaUtilities;
import de.theminefighter.stslauncher.caching.ResourceCache;
import de.theminefighter.stslauncher.caching.SimpleCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class JWSContext {
	private static Element root;
	private static Element information;
	private static ResourceCache cache;

	private JWSContext() {}

	static {
		try {
			cache = new SimpleCache();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ini(System.getProperty("jnlpx.origFilenameArg"));
	}

	/**
	 * Initializes the JWSContext using the path to the jnlp file
	 *
	 * @param jnlp the path to the jnlp file
	 */
	private static void ini(String jnlp) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		Document parse;
		try {
			parse = builderFactory.newDocumentBuilder().parse(new File(jnlp));
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new RuntimeException("Error whilst determining jnlp context using jnlp file " + jnlp, e);
		}
		root = parse.getDocumentElement();
		information = (Element) root.getElementsByTagName("information").item(0);
	}

	public static Element getInformation() {
		return information;
	}

	public static ResourceCache getCache() {
		return cache;
	}

	public static String getIdentifier() {
		return SimpleCache.toOsCompliantFileName(getRoot().getAttribute("href"));
	}

	public static File getLocalIcon() {
		NodeList iconTags = getInformation().getElementsByTagName("icon");
		if (iconTags.getLength() == 0) return null;
		String href = ((Element) iconTags.item(0)).getAttribute("href");
		try {
			return cache.get(new URL(getRoot().getAttribute("codebase") + href), true);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static File getLocalJNLP() {
		try {
			return cache.get(new URL(getRoot().getAttribute("href")), true);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public static Element getRoot() {
		return root;
	}

	public static File getLocalStsLauncherJarPath() {
		try {
			final String spec = "https://github.com/TheMinefighter/StsLauncher/releases/latest/download/StsLauncher.jar";
			return cache.storeLocal(JavaUtilities.getStsLauncherLocation(), new URL(JavaUtilities.isJar() ? spec : spec + ".unpacked"));
		} catch (MalformedURLException e) {
			throw new RuntimeException("This does not happen");
		}

	}

}
