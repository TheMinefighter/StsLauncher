package de.theminefighter.stslauncher;

import de.theminefighter.stslauncher.caching.ResourceCache;
import de.theminefighter.stslauncher.caching.SimpleCache;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.jnlp.impl.IntegrationServiceLinuxImpl;
import javax.jnlp.impl.JWSContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = interactionNoJnlpProvided();
            if (args == null) return;
        }
		System.setProperty("jnlpx.origFilenameArg", args[0]);
		createRequestedShortcuts();

		boolean slf = checkSLF(args);
        ProcessBuilder pb = new ProcessBuilder(prepareLaunch(args[0], slf));
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
	 * Creates the shortcuts requested by the jnlp file that is currently executed
	 */
	private static void createRequestedShortcuts() {
		Element info =JWSContext.getInformation();
		NodeList scs=info.getElementsByTagName("shortcut");
		if (scs.getLength()>0) {
			Element sc=(Element) scs.item(0);
			boolean desktop=sc.getElementsByTagName("desktop").getLength()>0;
			NodeList menuNs=sc.getElementsByTagName("menu");
			boolean menu=menuNs.getLength()>0;
			String submenu=null;
			if (menu&& menuNs.item(0).getAttributes().getNamedItem("submenu")!=null) {
				submenu=menuNs.item(0).getAttributes().getNamedItem("submenu").getNodeValue();
			}
			new IntegrationServiceLinuxImpl().requestShortcut(desktop,menu,submenu);
		}
	}

	/**
     * checks whether the --show-license-files parameter is used, if not it informs the user about it's existence
     * @param args the args the program received
     * @return whether slf is specified
     */
    private static boolean checkSLF(String[] args) {
        boolean slf = args.length > 1 && args[1].equals("--show-license-files");
        if (!slf)
            System.out.println("Use --show-license-files as second argument to show the license files of all libraries used.");
        return slf;
    }

    /**
     * Provides an interactive cli if no args are provided
     * @return an array of args to be treated as cli args
     * @throws IOException when the user does bs
     */
    private static String[] interactionNoJnlpProvided() throws IOException {
        String[] args;
        System.out.println("Please enter the path of the jnlp to launch");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = reader.readLine();
        if (str.length()==0) {
            System.out.println("No file provided. Exiting");
            return null;
        }
        File f= new File(str);
        if (!f.exists()) {
            System.out.println("The file specified does not exist");
            return null;
        }
        if (f.isDirectory()) {
            System.out.println("The path is a directory and not a file");
            return null;
        }
        args= new String[]{f.toString()};
        return args;
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
        List<File> jarsForLaunch = getJarsForLaunch(root.getAttribute("codebase"), resources);
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
        return String.join(":", classPathParts);
    }

    /**
     * Build a map of all props to set for the new JVM instance
     * @apiNote does never include java.class.path prop and forces file.encoding=UTF-8
     * @param resources the resources element of the jnlp
     * @return a map of all props to set for the new JVM instance
     */
    private static Map<String, String> makeJVMProps(Element resources,String jnlpPath) {
        Map<String, String> launchProps = Flags.forwardProps ? System.getProperties().stringPropertyNames()
                .stream().collect(Collectors.toMap(propN -> propN, System::getProperty, (a, b) -> b)) : new HashMap<>();
        NodeList props = resources.getElementsByTagName("property");
        IntStream.range(0, props.getLength()).mapToObj(i1 -> (Element) props.item(i1))
                .forEach(prop -> launchProps.put(prop.getAttribute("name"), prop.getAttribute("value")));
        launchProps.remove("java.class.path");
        launchProps.put("file.encoding", "UTF-8");
        launchProps.put("jnlpx.origFilenameArg",jnlpPath);
        return launchProps;
    }

    /**
     * Prepares all jars from the jnlp and required libs for launch
     *
     * @param codebase  the codebase attribute of the jnlp
     * @param resources the resources element of the jnlp
     * @return A list of all jars needed for launch
     * @throws IOException something went wrong, probably whilst trying to cache the jars
     */
    private static List<File> getJarsForLaunch(String codebase, Element resources) throws IOException {
        ResourceCache cache = new SimpleCache();
        //Old java Version still include all libs required
        boolean oldJava = System.getProperty("java.version").startsWith("1.");
        System.out.println("Preparing java libraries for launch (this may take some seconds on the first launch or after an update)...");
        List<File> jarsToLoad = oldJava ? new LinkedList<>() :
                (Arrays.stream(LibManager.makeLibUrls())).map(x -> cache.get(x, true)).collect(Collectors.toList());
        NodeList jars = resources.getElementsByTagName("jar");
        for (int i = 0; i < jars.getLength(); i++) {
            Element jar = (Element) jars.item(i);
            URL href = new URL(codebase + "/" + jar.getAttribute("href"));
            File cached = cache.get(href, Flags.offline);
            jarsToLoad.add(cached);
        }
        return jarsToLoad;
    }

}
