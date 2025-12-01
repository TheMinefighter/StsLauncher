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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class JWSContext {
	private static Element root;
	private static Element information;
	private static ResourceCache cache;
	private static URI codebase;

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
		if (root.hasAttribute("codebase")) {
			try {
				codebase = new URI(root.getAttribute("codebase"));
			} catch (URISyntaxException e) {
				throw new RuntimeException("JNLP code base URI is invalid", e);
			}
		}
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
		return cache.get(resolveHref(href), true);
	}

	public static File getLocalJNLP() {
		return cache.get(resolveHref(getRoot().getAttribute("href")), true);

	}

	public static Element getRoot() {
		return root;
	}

	/**
	 * Obtains path to STSLauncher that is probably permanently accessible
	 * @return a path to STSLauncher that is probably permanently accessible
	 */
	public static File getLocalStsLauncherJarPath() {
		try {
			final String spec = "https://github.com/TheMinefighter/StsLauncher/releases/latest/download/StsLauncher.jar";
			return cache.storeLocal(JavaUtilities.getStsLauncherLocation(), new URL(JavaUtilities.isJar() ? spec : spec + ".unpacked"));
		} catch (MalformedURLException e) {
			throw new RuntimeException("This does not happen");
		}

	}

	public static URL resolveHref(String href) {
		try {
			if (codebase == null) {
				return new URL(href);
			}
			return codebase.resolve(href).toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
