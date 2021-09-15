package de.theminefighter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;


public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length!=1)
            throw new Exception("A jnlp file must be provided to launch it");
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
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        for (int i = 0; i < jars.getLength(); i++) {
            Element jar= (Element) jars.item(i);
            method.invoke(sysloader,new URL(codebase+"/"+jar.getAttribute("href")));
        }
        Class<?> loadedClass = sysloader.loadClass(mainClassName);
        Method mainMethod = loadedClass.getDeclaredMethod("main",String[].class);
        //run sts
        mainMethod.invoke(null,new Object[]{launchArgs});
    }
}
