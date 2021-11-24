package de.theminefighter.stsLauncher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LibManager {
    public static List<URL> makeLibUrls(ResourceCache cache) {
        InputStream mavenStream = ClassLoader.getSystemClassLoader().getResourceAsStream("de/theminefighter/stsLauncher/MavenList.csv");
        Stream<String> lines = new BufferedReader(new InputStreamReader(mavenStream)).lines();
        List<URL> jarsToLoad = lines.map(LibManager::makeMvnUrl).map(x -> cache.get(x, true)).collect(Collectors.toList());
        return jarsToLoad;
    }

    public static void showLicenseFiles(List<URL> urls) {
        for (URL url :
                urls) {
            try {
                ZipFile zf=new ZipFile(url.getPath());
                Enumeration<? extends ZipEntry> entries = zf.entries();

                while(entries.hasMoreElements()){
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory()) continue;
                    if (entry.getName().toLowerCase().contains("license")) {
                        InputStream lcs = zf.getInputStream(entry);
                        InputStreamReader lcsr =  new InputStreamReader(lcs);
                        BufferedReader blcsr=new BufferedReader(lcsr);
                        String line;
                        while ((line = blcsr.readLine()) != null) {
                            System.out.println(line);
                        }
                        System.out.println("=============================================");
                        blcsr.close();
                        lcsr.close();
                        lcs.close();
                    }
                }
                zf.close();
            } catch (IOException e) {
                System.out.println("An error occurred whilst looking for License files in " + url.toString());
            }
        }
    }
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
