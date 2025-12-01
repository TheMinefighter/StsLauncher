package de.theminefighter.stslauncher;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.jnlp.IntegrationService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.jnlp.impl.JWSContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = interactionNoJnlpProvided();
            if (args == null) return;
        }
		String jnlpPath = args[0];
		System.setProperty("jnlpx.origFilenameArg", jnlpPath);
		//Only now JWSContext may be loaded
		String href = JWSContext.getRoot().getAttribute("href");
		URL jnlpUrl= JWSContext.resolveHref(href);
		boolean launchedBefore=JWSContext.getCache().has(jnlpUrl);
		if (!launchedBefore) {
			createRequestedShortcuts();
			JWSContext.getCache().get(jnlpUrl, false);
		}

		boolean slf = checkSLF(args);
		JnlpLauncher.Launch(jnlpPath, slf);
	}



	/**
	 * Creates the shortcuts requested by the jnlp file that is currently executed
	 */
	public static void createRequestedShortcuts() {
		IntegrationService is;
		try {
			is= (IntegrationService) ServiceManager.lookup("javax.jnlp.IntegrationService");
		} catch (UnavailableServiceException e) {
			return;
		}

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
			is.requestShortcut(desktop,menu,submenu);
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
		str = str.replace("\"", "").replace("'", "").trim();
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



}
