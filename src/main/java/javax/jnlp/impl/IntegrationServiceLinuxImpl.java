package javax.jnlp.impl;


import de.theminefighter.stslauncher.JavaUtilities;
import de.theminefighter.stslauncher.Main;

import javax.jnlp.IntegrationService;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements IntegrationService partially for linux environments, assoc stiff is not implemented
 */
@SuppressWarnings("unused")
public class IntegrationServiceLinuxImpl implements IntegrationService {
	private static final String desktopFolder = "/Desktop/";
	private static final String menuFolder = "/.local/share/applications/";
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

	private File getDesktopFile() {
		return new File(System.getProperty("user.home"), desktopFolder + dePrefix + JWSContext.getIdentifier() + fileType);
	}

	private File getMenuFile() {
		return new File(System.getProperty("user.home"), menuFolder + mePrefix + JWSContext.getIdentifier() + fileType);
	}

	@Override
	public boolean hasMenuShortcut() {
		return hasShortcut(true);
		//return Arrays.stream(new File(menuFolder).listFiles()).findAny().isPresent();
	}

	private boolean hasShortcut(boolean isMenu) {
		return (isMenu? getMenuFile():getDesktopFile()).exists();
	}


	@Override
	public boolean removeAssociation(String mimeType, String[] extensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeShortcuts() {

		return getDesktopFile().delete() | removeMenuShortcut();
	}

	private boolean removeMenuShortcut() {
		//return Arrays.stream(new File(menuFolder).listFiles()).anyMatch(File::delete);
		return getMenuFile().delete();
	}

	@Override
	public boolean requestAssociation(String mimeType, String[] extensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean requestShortcut(boolean onDesktop, boolean inMenu, String subMenu) {
		return (!onDesktop || makeDesktopShortcut()) & (!inMenu || makeMenuShortcut(subMenu));
	}

	private boolean makeDesktopShortcut() {
		return makeShortcut(null, false);
	}

	private boolean makeMenuShortcut(String subMenu) {
		//For Win use
		//File file = new File(Paths.get(menuFolder, subMenu, mePrefix + JWSContext.getIdentifier() + fileType).toString());
		//if (file.exists()) return false;
		return makeShortcut(subMenu, true);

	}

	private boolean makeShortcut(String subMenu, boolean isMenu) {
		File desktopFile = isMenu? getMenuFile():getDesktopFile();
		if (hasShortcut(isMenu)) return false;
		String desktopFileContent = makeDeFString(subMenu);
		try {
			if (!desktopFile.getParentFile().exists() || !desktopFile.createNewFile()) return false;
			FileWriter fileWriter = new FileWriter(desktopFile, false);
			fileWriter.write(desktopFileContent);
			fileWriter.close();
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

	private String makeExec() {
		if (JavaUtilities.isJar()) {
			return String.format("%s -jar %s %s", JavaUtilities.getJavaPath(), JWSContext.getLocalStsLauncherJarPath(), JWSContext.getLocalJNLP());
		} else {
			return String.format("%s -cp %s %s %s", JavaUtilities.getJavaPath(), JavaUtilities.getStsLauncherLocation(), Main.class.getCanonicalName(), JWSContext.getLocalJNLP());
		}
	}
}
