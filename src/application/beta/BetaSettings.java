package application.beta;

import java.util.prefs.Preferences;

public class BetaSettings {
	Preferences betaPrefs;

	public BetaSettings() {
		betaPrefs = Preferences.userNodeForPackage(BetaSettings.class);
	}

	public void savePaths(String from, String to) {
		savePathFrom(from);
		savePathTo(to);
	}

	private void savePathFrom(String path) {
		betaPrefs.put("from", path);
	}

	private void savePathTo(String path) {
		betaPrefs.put("to", path);
	}

	public String getFromPath() {
		return betaPrefs.get("from", "");
	}

	public String getToPath() {
		return betaPrefs.get("to", "");
	}

}
