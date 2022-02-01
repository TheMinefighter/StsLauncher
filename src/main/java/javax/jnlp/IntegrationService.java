package javax.jnlp;

@SuppressWarnings("unused")
public interface IntegrationService {

	boolean hasAssociation(java.lang.String mimeType, java.lang.String[] extensions);

	boolean hasDesktopShortcut();

	boolean hasMenuShortcut();

	boolean removeAssociation(java.lang.String mimeType, java.lang.String[] extensions);

	boolean removeShortcuts();

	boolean requestAssociation(java.lang.String mimeType, java.lang.String[] extensions);

	boolean requestShortcut(boolean onDesktop, boolean inMenu, java.lang.String subMenu);

}