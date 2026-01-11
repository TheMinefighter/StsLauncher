package de.theminefighter.stslauncher;

import de.theminefighter.stslauncher.caching.ResourceCache;
import de.theminefighter.stslauncher.caching.SimpleCache;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.jnlp.impl.JWSContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JnlpLauncher {

	public static final String DEFAULT_POLICY = "grant {permission java.net.SocketPermission \"localhost:1024-\", \"accept,listen,resolve\";};";

	public static void Launch(String jnlpPath, boolean slf) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(prepareLaunch(jnlpPath, slf));
		makeEnv(pb.environment());
		if (Flags.forwardSTSProcessIO) {
			pb.inheritIO();
		}
		System.out.println("Launching actual jnlp...");
		Process STSProcess = pb.start();
		if (Flags.forwardSTSProcessIO) {
			STSProcess.waitFor();
		}
	}
	/**
	 * Builds the environment for the new JVM process
	 * @param environment the environment to update
	 */
	private static void makeEnv(Map<String, String> environment) {
		if (Flags.forwardEnv) {
			environment.clear();
			environment.putAll(System.getenv());
		}
	}

	/**
	 * Prepares the launch of sts
	 * @param jnlp the jnlpFile
	 * @param slf whether the --show-license-files parameter was used
	 * @return all args for the exec call launching the new JVM for sts
	 * @throws Exception there are many thing which can go wrong in this method, for example the jnlp could be invalid
	 */
	static String[] prepareLaunch(String jnlp, boolean slf) throws Exception {
		//load jnlp structure
		Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(jnlp)).getDocumentElement();
		Element resources = (Element) root.getElementsByTagName("resources").item(0);
		Element appDesc = (Element) root.getElementsByTagName("application-desc").item(0);
		//load server addresses and other stuff from jnlp to system properties
		List<File> jarsForLaunch = getJarsForLaunch(resources);
		if (slf) {
			LibManager.showLicenseFiles(jarsForLaunch);
		}

		return makeArgs(resources, appDesc, appDesc.getAttribute("main-class"), jarsForLaunch, jnlp);
	}

	/**
	 * Builds all args for the exec call launching the new JVM
	 *
	 * @param resources     the resources element of the jnlp
	 * @param appDesc       the appDesc element of the jnlp
	 * @param mainClassName the name of the main class to launch extracted from the jnlp
	 * @param jarsForLaunch the jars needed in the classpath
	 * @return all args for the exec call launching the new JVM
	 */
	private static String[] makeArgs(Element resources, Element appDesc, String mainClassName, List<File> jarsForLaunch, String jnlpPath) {
		List<String> javaArgs = new LinkedList<>();
		javaArgs.add(JavaUtilities.getJavaPath());
		if (Flags.verboseJVM) {
			javaArgs.add("-verbose:class");
			javaArgs.add("-verbose:jni");
		}
		javaArgs.add("-cp");
		javaArgs.add(makeCPString(jarsForLaunch));
		Map<String, String> launchProps = makeJVMProps(resources,jnlpPath);
		launchProps.entrySet().stream().map(lp -> String.format("-D%s=%s", lp.getKey(), lp.getValue())).forEach(javaArgs::add);
		//load arguments for main method of sts
		javaArgs.add(mainClassName);
		NodeList argumentTags = appDesc.getElementsByTagName("argument");
		IntStream.range(0, argumentTags.getLength()).mapToObj(i -> argumentTags.item(i).getTextContent()).forEach(javaArgs::add);

		return javaArgs.toArray(new String[0]);
	}

	/**
	 * Build the classpath string for launching STS
	 *
	 * @param jarsForLaunch the jars to include
	 * @return A classpath string with the jars provided+the classpath of the current java instance
	 */
	private static String makeCPString(List<File> jarsForLaunch) {
		List<String> classPathParts = jarsForLaunch.stream().map(File::getPath).collect(Collectors.toList());
		classPathParts.add(System.getProperty("java.class.path"));
		String delimiter = System.getProperty("os.name").contains("Windows")?";":":";
		return String.join(delimiter, classPathParts);
	}

	/**
	 * Build a map of all props to set for the new JVM instance
	 * @apiNote does never include java.class.path prop and forces file.encoding=UTF-8
	 * @param resources the resources element of the jnlp
	 * @return a map of all props to set for the new JVM instance
	 */
	private static Map<String, String> makeJVMProps(Element resources, String jnlpPath) {
		Map<String, String> launchProps = Flags.forwardProps ? System.getProperties().stringPropertyNames()
				.stream().collect(Collectors.toMap(propN -> propN, System::getProperty, (a, b) -> b)) : new HashMap<>();
		NodeList props = resources.getElementsByTagName("property");
		IntStream.range(0, props.getLength()).mapToObj(i1 -> (Element) props.item(i1))
				.forEach(prop -> launchProps.put(prop.getAttribute("name"), prop.getAttribute("value")));
		//filter jvm prop telling the jvm to start StsLauncher
		launchProps.remove("java.class.path");
		launchProps.put("file.encoding", "UTF-8");
		launchProps.put("java.security.policy","/home/tobias/IdeaProjects/StsLauncher/src/main/resources/security.policy");
		launchProps.put("jnlpx.origFilenameArg",jnlpPath);
		URL policy;
		try {
			policy = new URL("https://github.com/TheMinefighter/StsLauncher/releases/latest/download/default-jnlp-security.policy");
		} catch (MalformedURLException e) {
			throw new RuntimeException("This does not happen");
		}
		if (!JWSContext.getCache().has(policy)) {
			try (PrintWriter printWriter = new PrintWriter(JWSContext.getCache().writeStream(policy))) {
				printWriter.print(DEFAULT_POLICY);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		launchProps.put("java.security.policy", JWSContext.getCache().get(policy, true).getAbsolutePath());
		return launchProps;
	}

	/**
	 * Prepares all jars from the jnlp and required libs for launch
	 *
	 * @param resources the resources element of the jnlp
	 * @return A list of all jars needed for launch
	 * @throws IOException something went wrong, probably whilst trying to cache the jars
	 */
	private static List<File> getJarsForLaunch(Element resources) throws IOException {
		ResourceCache cache = new SimpleCache();
		//Old java Version still include all libs required
		boolean oldJava = System.getProperty("java.version").startsWith("1.");
		System.out.println("Preparing java libraries for launch (this may take some seconds on the first launch or after an update)...");
		List<File> jarsToLoad = oldJava ? new LinkedList<>() :
				(Arrays.stream(LibManager.makeLibUrls())).map(x -> cache.get(x, true)).collect(Collectors.toList());
		//Fixed version maven download urls won't change, so no check for newer versions is needed
		NodeList jars = resources.getElementsByTagName("jar");
		for (int i = 0; i < jars.getLength(); i++) {
			Element jar = (Element) jars.item(i);
			URL href = JWSContext.resolveHref(jar.getAttribute("href"));
			File cached = cache.get(href, Flags.offline);
			jarsToLoad.add(cached);
		}
		return jarsToLoad;
	}
}
