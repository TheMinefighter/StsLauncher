package javax.jnlp.impl;


import de.theminefighter.stslauncher.JavaUtilities;
import de.theminefighter.stslauncher.Main;

import javax.jnlp.IntegrationService;
import java.io.File;

/**
 * Implements IntegrationService partially for linux environments, assoc stuff is not implemented
 */
@SuppressWarnings("unused")
public abstract class IntegrationServiceImpl implements IntegrationService, JnlpServiceImpl {


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
	protected abstract File getDesktopFile(boolean menu);

	@Override
	public boolean hasMenuShortcut() {
		return hasShortcut(true);
	}

	/**
	 * Whether a .desktop shortcut exists
	 * @param isMenu whether to look for a menu or a shortcut
	 * @return whether a shortcut exists
	 */
	protected boolean hasShortcut(boolean isMenu) {
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
    protected abstract boolean makeShortcut(String subMenu, boolean isMenu);


	/**
     * Creates an exec string to launch this jnlp
     * @return an exec string to launch this jnlp
     */
	protected String makeExec() {
		return JavaUtilities.getJavaPath() +makeExecArgs();
	}

	protected static String makeExecArgs() {
		if (JavaUtilities.isJar()) {
			return String.format(" -jar %s %s", JWSContext.getLocalStsLauncherJarPath(), JWSContext.getLocalJNLP());
		} else {
			return String.format(" -cp %s %s %s", JavaUtilities.getStsLauncherLocation(), Main.class.getCanonicalName(), JWSContext.getLocalJNLP());
		}
	}
}
