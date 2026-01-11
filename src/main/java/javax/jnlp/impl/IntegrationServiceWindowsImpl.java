package javax.jnlp.impl;

import de.theminefighter.stslauncher.JavaUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class IntegrationServiceWindowsImpl extends IntegrationServiceImpl {


	@Override
	protected File getDesktopFile(boolean menu) {
		String fn = JWSContext.getAppName() + ".lnk";
		return menu? Paths.get(System.getenv("APPDATA"), "Microsoft","Windows","Start Menu","Programs","StsLauncher",fn ).toFile():Paths.get(System.getenv("USERPROFILE"),"Desktop", fn).toFile();
	}

	@Override
	protected boolean makeShortcut(String subMenu, boolean isMenu) {
		if (isMenu) {getDesktopFile(isMenu).getParentFile().mkdir();}
		// Not yet implemented because windows wants ico files.
		// String icon = JWSContext.getLocalIcon().getAbsolutePath();
		String powerShellCommand = String.format(
			"$wshell = New-Object -ComObject WScript.Shell; " +
				"$shortcut = $wshell.CreateShortcut('%s'); " +
				"$shortcut.TargetPath = '%s'; " +
				"$shortcut.Arguments = '%s'; " +
				"$shortcut.Save();",
			getDesktopFile(isMenu).getAbsolutePath(), JavaUtilities.getJavaPath()+"w.exe", makeExecArgs()
		);

		try {
			ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", powerShellCommand);
			pb.redirectErrorStream(true);
			int exitCode = pb.start().waitFor();
			return exitCode == 0;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
}
