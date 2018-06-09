package application.other;

public class OtherModel {

	private static OtherModel INSTANCE;
	private OtherSettings settings = new OtherSettings();

	public OtherModel() {

	}

	public static OtherModel getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new OtherModel();
		}
		return INSTANCE;
	}

	public void handleGradaButton() {
		GradaFileConverter grada = new GradaFileConverter();
		grada.convertGradaDeliveryListToExcel(settings.getFromPath(), settings.getToPath());
	}

	public OtherSettings getSettings() {
		return this.settings;
	}

	public void savePaths(String from, String to) {
		settings.savePaths(from, to);
	}

	public String getFromPath() {
		return settings.getFromPath();
	}

	public String getToPath() {
		return settings.getToPath();
	}

}