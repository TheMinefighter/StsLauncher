package javax.jnlp.impl;



import de.theminefighter.stslauncher.JavaUtilities;
import de.theminefighter.stslauncher.Main;

import javax.jnlp.IntegrationService;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IntegrationServiceLinuxImpl implements IntegrationService {
	private static String desktopFolder = "/Desktop/";
	private static String menuFolder = "/.local/share/applications/";
	private static final String dePrefix = "stslauncher.DesktopSc_";
	private static final String mePrefix = "stslauncher.MenuSc_";
	private static final String fileType = ".desktop";

	@Override
	public boolean hasAssociation(String mimeType, String[] extensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasDesktopShortcut() {
		return getDesktopFile().exists();
	}

	private File getDesktopFile() {
		return new File(System.getProperty("user.home"), desktopFolder + dePrefix + JWSContext.getIdentifier() + fileType);
	}

	private File getMenuFile() {
		return new File(System.getProperty("user.home"), menuFolder + mePrefix + JWSContext.getIdentifier() + fileType);
	}

	@Override
	public boolean hasMenuShortcut() {
		return getMenuFile().exists();
		//return Arrays.stream(new File(menuFolder).listFiles()).findAny().isPresent();
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
		if (hasDesktopShortcut()) return false;
		try {
			File desktopFile = getDesktopFile();
			desktopFile.createNewFile();
			FileWriter fileWriter = new FileWriter(desktopFile);
			fileWriter.write(makeDeFString(null));
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean makeMenuShortcut(String subMenu) {
		//File file = new File(Paths.get(menuFolder, subMenu, mePrefix + JWSContext.getIdentifier() + fileType).toString());
		//if (file.exists()) return false;
		if (hasMenuShortcut()) return false;
		try {
			File desktopFile = getMenuFile();
			desktopFile.getParentFile().mkdirs();
			desktopFile.createNewFile();
			FileWriter fileWriter = new FileWriter(desktopFile, false);
			fileWriter.write(makeDeFString(subMenu));
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	private String makeDeFString(String subMenu) {
		StringBuilder sb = new StringBuilder("[Desktop Entry]\n");
		Map<String, String> info = makeData(subMenu);
		for (Map.Entry<String, String> e :
				info.entrySet()) {
			sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');

		}
		return sb.toString();
	}

	private Map<String, String> makeData(String subMenu) {
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

	private String makeExec() {if (JavaUtilities.isJar()) {
		return String.format("%s -jar %s %s", JavaUtilities.getJavaPath(), JWSContext.getLocalStsLauncherJarPath(), JWSContext.getLocalJNLP());
	} else return String.format("%s -cp %s %s %s", JavaUtilities.getJavaPath(), JavaUtilities.getStsLauncherJarLocation(), Main.class.getCanonicalName(), JWSContext.getLocalJNLP());
	}
}
