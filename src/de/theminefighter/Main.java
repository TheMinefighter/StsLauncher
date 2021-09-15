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
            throw new Exception("Es muss exakt 1 Argument bereitgestellt werden: Eine gültige jnlp Datei.");

        DocumentBuilderFactory dBfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dBfactory.newDocumentBuilder();
        Document document = builder.parse(new File(args[0]));
        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();
        String codebase = root.getAttribute("codebase");
        Element resources = (Element) root.getElementsByTagName("resources").item(0);
        Element appDesc = (Element) root.getElementsByTagName("application-desc").item(0);
        String mainClassName = appDesc.getAttribute("main-class");

        NodeList props = resources.getElementsByTagName("property");
        for (int i = 0; i < props.getLength(); i++) {
            Element prop= (Element) props.item(i);
            System.setProperty(prop.getAttribute("name"),prop.getAttribute("value"));
        }
        NodeList argumentTags = appDesc.getElementsByTagName("argument");
        String[] nargs=new String[argumentTags.getLength()];
        for (int i = 0; i < argumentTags.getLength(); i++) {
            nargs[i]=((Element) argumentTags.item(i)).getTextContent();
        }
        NodeList jars = resources.getElementsByTagName("jar");
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
method.setAccessible(true);
        URL[] downloads= new URL[jars.getLength()];
        for (int i = 0; i < jars.getLength(); i++) {
            Element jar= (Element) jars.item(i);
            downloads[i]= new URL(codebase+"/"+jar.getAttribute("href"));
            method.invoke(sysloader, new Object[]{downloads[i]});
        }
        Class<?> loadedClass = sysloader.loadClass(mainClassName);
        Method mainMethod = loadedClass.getDeclaredMethod("main",String[].class);
        mainMethod.invoke(null,new Object[]{nargs});
    }
}
