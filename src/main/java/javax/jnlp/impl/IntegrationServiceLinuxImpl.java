package javax.jnlp.impl;


import de.theminefighter.stslauncher.JavaUtilities;
import de.theminefighter.stslauncher.Main;

import javax.jnlp.IntegrationService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements IntegrationService partially for linux environments, assoc stuff is not implemented
 */
@SuppressWarnings("unused")
public class IntegrationServiceLinuxImpl implements IntegrationService, JnlpServiceImpl {
	private static final String desktopFolder = System.getenv("XDG_DESKTOP_DIR")!=null?System.getenv("XDG_DESKTOP_DIR"): "~/Desktop/";
	private static final String dataHome = System.getenv("XDG_DATA_HOME")!=null?System.getenv("XDG_DATA_HOME"): "~/.local/share/";
	private static final String dePrefix = "stsLauncher.DesktopSc_";
	private static final String mePrefix = "stsLauncher.MenuSc_";
	private static final String fileType = ".desktop";

	@Override
	public boolean hasAssociation(String mimeType, String[] extensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasDesktopShortcut() {
		return hasShortcut(false);
	}

    /**
     * Retrieves the possibly existing desktop shortcut file for this jnlp
     * @param menu whether to get the menu shortcut file
     */
	private File getDesktopFile(boolean menu) {
		File baseDir = menu ? new File(dataHome, "applications"): new File(desktopFolder);
		return new File(baseDir, (menu?mePrefix:dePrefix)+ JWSContext.getIdentifier() + fileType);
	}

	@Override
	public boolean hasMenuShortcut() {
		return hasShortcut(true);
	}

	/**
	 * Whether a .desktop shortcut exists
	 * @param isMenu whether to look for a menu or a shortcut
	 * @return whether a shortcut exists
	 */
	private boolean hasShortcut(boolean isMenu) {
		return getDesktopFile(isMenu).exists();
	}


	@Override
	public boolean removeAssociation(String mimeType, String[] extensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeShortcuts() {
		return getDesktopFile(false).delete() | getDesktopFile(true).delete();
	}

	@Override
	public boolean requestAssociation(String mimeType, String[] extensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean requestShortcut(boolean onDesktop, boolean inMenu, String subMenu) {
        //For Win use
        //File file = new File(Paths.get(menuFolder, subMenu, mePrefix + JWSContext.getIdentifier() + fileType).toString());
        //if (file.exists()) return false;

        return (!onDesktop || makeShortcut(null, false)) & (!inMenu || makeShortcut(subMenu, true));
	}

	/**
	 * Creates a .desktop Shortcut
	 * @param subMenu The submenu to put the application into or null, if none
	 * @param isMenu whether it should be a menu or desktop shortcut
	 * @return whether a shortcut has been created
	 */
    private boolean makeShortcut(String subMenu, boolean isMenu) {
		File desktopFile = getDesktopFile(isMenu);
		if (hasShortcut(isMenu)) return false;
		String desktopFileContent = makeDeFString(subMenu);
		try {
			if (!desktopFile.getParentFile().exists() || !desktopFile.createNewFile()) return false;
			FileOutputStream fos = new FileOutputStream(desktopFile);
			OutputStreamWriter fileWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			fileWriter.write(desktopFileContent);
			fileWriter.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String makeDeFString(String subMenu) {
		StringBuilder sb = new StringBuilder("[Desktop Entry]\n");
		Map<String, String> info = makeDeFData(subMenu);
		for (Map.Entry<String, String> e :
				info.entrySet()) {
			sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');

		}
		return sb.toString();
	}

    /**
     * Generates the Map of data to write into a desktop file
     * @param subMenu the submenu requested or null
     * @return the Map of data to write into a desktop file
     */
	private Map<String, String> makeDeFData(String subMenu) {
		Map<String, String> r = new HashMap<>();
		r.put("Version", "1.5");
		r.put("Type", "Application");
		String description = JWSContext.getInformation().getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
		if (description != null) r.put("Comment", description);
		File localIcon = JWSContext.getLocalIcon();
		if (localIcon != null) r.put("Icon", localIcon.getPath());
		if (subMenu == null) {
			r.put("Categories", "Applications");
		} else {
			r.put("Categories", "Applications;" + subMenu);
		}
		r.put("Exec", makeExec());
		r.put("Terminal", "false");
		r.put("Type", "Application");
		r.put("Name", JWSContext.getInformation().getElementsByTagName("title").item(0).getFirstChild().getNodeValue());
		return r;
	}

    /**
     * Creates an exec string to launch this jnlp
     * @return an exec string to launch this jnlp
     */
	private String makeExec() {
		if (JavaUtilities.isJar()) {
			return String.format("%s -jar %s %s", JavaUtilities.getJavaPath(), JWSContext.getLocalStsLauncherJarPath(), JWSContext.getLocalJNLP());
		} else {
			return String.format("%s -cp %s %s %s", JavaUtilities.getJavaPath(), JavaUtilities.getStsLauncherLocation(), Main.class.getCanonicalName(), JWSContext.getLocalJNLP());
		}
	}
}
