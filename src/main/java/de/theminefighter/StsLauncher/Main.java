package de.theminefighter.StsLauncher;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {
    static final boolean offline=true;
    public static void main(String[] args) throws Exception {
        ResourceCache cache= new SimpleCache();
        if (args.length==0)
            throw new Exception("A jnlp file must be provided to launch it");
        System.out.println("use --show-license-files as second argument to show license files of all libraries used");
        boolean slf= args.length>1 && args[1].equals("--show-license-files");
        InputStream mavenStream = ClassLoader.getSystemClassLoader().getResourceAsStream("de/theminefighter/StsLauncher/MavenList.csv");
        Stream<String> lines = new BufferedReader(new InputStreamReader(mavenStream)).lines();
        List<URL> jarsToLoad = lines.map(Main::makeMvnUrl).map(x -> cache.get(x, true)).collect(Collectors.toList());

//load jnlp structure
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0])).getDocumentElement();
        String codebase = root.getAttribute("codebase");
        Element resources = (Element) root.getElementsByTagName("resources").item(0);
        Element appDesc = (Element) root.getElementsByTagName("application-desc").item(0);
        String mainClassName = appDesc.getAttribute("main-class");
//load server addresses and other stuff from jnlp to system properties
        NodeList props = resources.getElementsByTagName("property");
        for (int i = 0; i < props.getLength(); i++) {
            Element prop= (Element) props.item(i);
            System.setProperty(prop.getAttribute("name"),prop.getAttribute("value"));
        }
        //load arguments for main method of sts
        NodeList argumentTags = appDesc.getElementsByTagName("argument");
        String[] launchArgs=new String[argumentTags.getLength()];
        for (int i = 0; i < argumentTags.getLength(); i++) {
            launchArgs[i]= argumentTags.item(i).getTextContent();
        }
        //load sts code to system classpath

        NodeList jars = resources.getElementsByTagName("jar");
        for (int i = 0; i < jars.getLength(); i++) {
            Element jar= (Element) jars.item(i);
            URL href = new URL(codebase + "/" + jar.getAttribute("href"));
            URL cached=cache.get(href, offline);
            jarsToLoad.add(cached);
        }
        URLClassLoader finalClassLoader = makeClassLoaderFromUrls(jarsToLoad.toArray(new URL[0]));

        Class<?> loadedClass = finalClassLoader.loadClass(mainClassName);
        Method mainMethod = loadedClass.getDeclaredMethod("main",String[].class);
        //run sts
        mainMethod.invoke(null,new Object[]{launchArgs});
    }

    private static URL makeMvnUrl(String mvnCsvLine)  {
        String[] split = mvnCsvLine.split(",");

        try {
            return new URL(String.format("https://repo.maven.apache.org/maven2/%s/%s/%s/%s-%s.jar",
                    split[0].replace('.', '/'), split[1], split[2], split[1], split[2]));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    private static URLClassLoader makeClassLoaderFromUrls(URL[] urls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        boolean oldJava=System.getProperty("java.version").startsWith("1.");
        URLClassLoader finalClassLoader;
        if (oldJava) {
            finalClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            for (URL url : urls) {
                method.invoke(finalClassLoader, url);
            }
        } else {
            finalClassLoader= URLClassLoader.newInstance(urls,ClassLoader.getSystemClassLoader().getParent());
            try {
                Field sclParentField = ClassLoader.class.getDeclaredField("parent");
                sclParentField.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(sclParentField, sclParentField.getModifiers() & ~Modifier.FINAL);

                sclParentField.set(ClassLoader.getSystemClassLoader(), finalClassLoader);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        //Thread.currentThread().setContextClassLoader(finalClassLoader);
        try {
            ClassLoader.getSystemClassLoader().loadClass("javax.xml.ws.Service");
            ClassLoader.getSystemClassLoader().loadClass("javax.jnlp.SingleInstanceListener");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return finalClassLoader;
    }
}
