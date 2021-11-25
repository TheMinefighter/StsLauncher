package de.theminefighter.stslauncher;

import de.theminefighter.stslauncher.caching.ResourceCache;
import de.theminefighter.stslauncher.caching.SimpleCache;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            throw new Exception("A jnlp file must be provided for launch");

        boolean slf = args.length > 1 && args[1].equals("--show-license-files");
        if (!slf)
            System.out.println("Use --show-license-files as second argument to show the license files of all libraries used.");
        ProcessBuilder pb = new ProcessBuilder(prepareLaunch(args[0], slf));
        makeEnv(pb.environment());
        if (Flags.forwardSTSProcessIO) {
            pb.inheritIO();
        }

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
    private static String[] prepareLaunch(String jnlp, boolean slf) throws Exception {
        //load jnlp structure
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(jnlp)).getDocumentElement();
        Element resources = (Element) root.getElementsByTagName("resources").item(0);
        Element appDesc = (Element) root.getElementsByTagName("application-desc").item(0);
        //load server addresses and other stuff from jnlp to system properties
        List<URL> jarsForLaunch = getJarsForLaunch(root.getAttribute("codebase"), resources);
        if (slf) {
            LibManager.showLicenseFiles(jarsForLaunch);
        }

        return makeArgs(resources, appDesc, appDesc.getAttribute("main-class"), jarsForLaunch);
        //return new String[]{"/bin/sh","-c",String.join(" ",javaArgs)};
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
    private static String[] makeArgs(Element resources, Element appDesc, String mainClassName, List<URL> jarsForLaunch) {
        List<String> javaArgs = new LinkedList<>();
        javaArgs.add(getJavaPath());
        if (Flags.verboseJVM) {
            javaArgs.add("-verbose:class");
            javaArgs.add("-verbose:jni");
        }
        javaArgs.add("-cp");
        javaArgs.add(makeCPString(jarsForLaunch));
        Map<String, String> launchProps = makeJVMProps(resources);
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
    private static String makeCPString(List<URL> jarsForLaunch) {
        List<String> classPathParts = jarsForLaunch.stream().map(URL::getPath).collect(Collectors.toList());
        classPathParts.add(System.getProperty("java.class.path"));
        return String.join(":", classPathParts);
    }

    /**
     * Build a map of all props to set for the new JVM instance
     * @apiNote does never include java.class.path prop and forces file.encoding=UTF-8
     * @param resources the resources element of the jnlp
     * @return a map of all props to set for the new JVM instance
     */
    private static Map<String, String> makeJVMProps(Element resources) {
        Map<String, String> launchProps = Flags.forwardProps ? System.getProperties().stringPropertyNames()
                .stream().collect(Collectors.toMap(propN -> propN, System::getProperty, (a, b) -> b)) : new HashMap<>();
        NodeList props = resources.getElementsByTagName("property");
        IntStream.range(0, props.getLength()).mapToObj(i1 -> (Element) props.item(i1))
                .forEach(prop -> launchProps.put(prop.getAttribute("name"), prop.getAttribute("value")));
        launchProps.remove("java.class.path");
        launchProps.put("file.encoding", "UTF-8");
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
    private static List<URL> getJarsForLaunch(String codebase, Element resources) throws IOException {
        ResourceCache cache = new SimpleCache();
        //Old java Version still include all libs required
        boolean oldJava = System.getProperty("java.version").startsWith("1.");
        List<URL> jarsToLoad = oldJava ? new LinkedList<>() :
                (Arrays.stream(LibManager.makeLibUrls())).map(x -> cache.get(x, true)).collect(Collectors.toList());
        NodeList jars = resources.getElementsByTagName("jar");
        for (int i = 0; i < jars.getLength(); i++) {
            Element jar = (Element) jars.item(i);
            URL href = new URL(codebase + "/" + jar.getAttribute("href"));
            URL cached = cache.get(href, Flags.offline);
            jarsToLoad.add(cached);
        }
        return jarsToLoad;
    }

    /**
     * Determines the path of the java executable running
     *
     * @return the path of the java executable running
     */
    private static String getJavaPath() {
        /*File winAttempt = Paths.get(System.getProperty("java.home"), "bin", "java.exe").toFile();
        if (winAttempt.exists()) return winAttempt.toString();*/
        //Just checked: also works w/o .exe  suffix under windows
        return Paths.get(System.getProperty("java.home"), "bin", "java").toFile().toString();
    }

}
