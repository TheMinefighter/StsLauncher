package de.theminefighter.StsLauncher;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Main {
    //used for offline debugging
    static final boolean offline = false;

    private static String getJavaPath() {
        File winAttempt = Paths.get(System.getProperty("java.home"), "bin", "java.exe").toFile();
        if (winAttempt.exists()) return winAttempt.toString();
        return Paths.get(System.getProperty("java.home"), "bin", "java").toFile().toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            throw new Exception("A jnlp file must be provided to launch it");

        boolean slf = args.length > 1 && args[1].equals("--show-license-files");
        if (!slf)
            System.out.println("use --show-license-files as second argument to show license files of all libraries used");
        ProcessBuilder pb = new ProcessBuilder(prepareLaunch(args[0], slf));
        Map<String, String> environment = pb.environment();
        environment.clear();
        environment.putAll(System.getenv());
        pb.inheritIO();
        pb.start().waitFor();

    }

    private static String[] prepareLaunch(String arg, boolean slf) throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
        //load jnlp structure
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(arg)).getDocumentElement();
        String codebase = root.getAttribute("codebase");
        Element resources = (Element) root.getElementsByTagName("resources").item(0);
        Element appDesc = (Element) root.getElementsByTagName("application-desc").item(0);
        String mainClassName = appDesc.getAttribute("main-class");
//load server addresses and other stuff from jnlp to system properties
        List<URL> jarsForLaunch = getJarsForLaunch(codebase, resources);
        if (slf) {
            LibManager.showLicenseFiles(jarsForLaunch);
        }

        return makeArgs(resources, appDesc, mainClassName, jarsForLaunch);
    }

    private static String[] makeArgs(Element resources, Element appDesc, String mainClassName, List<URL> jarsForLaunch) throws URISyntaxException {
        List<String> javaArgs = new LinkedList<>();
        javaArgs.add(getJavaPath());
        javaArgs.add("-verbose:class");
        javaArgs.add("-cp");
        javaArgs.add(makeCPString(jarsForLaunch));
        Map<String, String> launchProps = makeJVMProps(resources);
        launchProps.entrySet().stream().map(lp -> String.format("-D%s\"%s\"", lp.getKey(), lp.getValue())).forEach(javaArgs::add);
        //load arguments for main method of sts
        javaArgs.add(mainClassName);
        NodeList argumentTags = appDesc.getElementsByTagName("argument");
        IntStream.range(0, argumentTags.getLength()).mapToObj(i -> argumentTags.item(i).getTextContent()).forEach(javaArgs::add);

        return javaArgs.toArray(new String[0]);
    }

    private static String makeCPString(List<URL> jarsForLaunch) throws URISyntaxException {
        List<String> classPathParts = jarsForLaunch.stream().map(URL::getPath).collect(Collectors.toList());
        classPathParts.add(System.getProperty("java.class.path"));
        classPathParts.add(makeCPStringFromClass(Main.class));
        return String.join(":", classPathParts);
    }

    private static String makeCPStringFromClass(Class<?> aClass) throws URISyntaxException {
        CodeSource codeSource = aClass.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            String path = aClass.getResource(aClass.getSimpleName() + ".class").getPath();
            if (path.contains("!")) {
                return new URI(path.split("!")[0]).getPath();
            } else {
                throw new RuntimeException("Could not locate the location from which this program was launched");
            }
        }
        else {
                return codeSource.getLocation().getPath();
            }
        }

        private static Map<String, String> makeJVMProps (Element resources){
            Map<String, String> launchProps = System.getProperties().stringPropertyNames()
                    .stream().collect(Collectors.toMap(propN -> propN, System::getProperty, (a, b) -> b));
            NodeList props = resources.getElementsByTagName("property");
            IntStream.range(0, props.getLength()).mapToObj(i1 -> (Element) props.item(i1))
                    .forEach(prop -> launchProps.put(prop.getAttribute("name"), prop.getAttribute("value")));
            launchProps.remove("java.class.path");
            return launchProps;
        }


        private static List<URL> getJarsForLaunch (String codebase, Element resources) throws IOException {
            ResourceCache cache = new SimpleCache();
            List<URL> jarsToLoad = (System.getProperty("java.version").startsWith("1.")) ? new LinkedList<>() : LibManager.makeLibUrls(cache);

            //load sts code

            NodeList jars = resources.getElementsByTagName("jar");
            for (int i = 0; i < jars.getLength(); i++) {
                Element jar = (Element) jars.item(i);
                URL href = new URL(codebase + "/" + jar.getAttribute("href"));
                URL cached = cache.get(href, offline);
                jarsToLoad.add(cached);
            }
            return jarsToLoad;
        }


}
