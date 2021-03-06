package application.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import application.RowRecord;
import application.beta.BetaModel;
import application.infobar.InfoModel;
import application.kosmas.KosmasModel;

public class ExcelUtils {

	private File fromDirectory, toDirectory;

	public void kosmasExcel() {
		File directory = new File(System.getProperty("user.dir") + File.separator + "temp" + File.separator);
		for (File f : directory.listFiles()) {
			InfoModel.getInstance().updateInfo("Přesouvám  a přejmenovávám soubor " + f.getName());
			List<ExcelRecord> records = new ArrayList<ExcelRecord>();
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ((line = br.readLine()) != null) {

					String[] data = line.split(";");

					String ean = data[0];
					String amount = data[5];
					String price = data[3].replaceAll(" ", "").replaceAll("\\.00", "");
					double pricePerUnit = Double.parseDouble(data[6]);
					double totalPrice = Double.parseDouble(amount) * pricePerUnit;

					records.add(new ExcelRecord(ean, amount, price, totalPrice));
				}
				writeFileFourInputs(records, KosmasModel.getInstance().getSettings().getPath(),
						f.getName().replaceAll(".csv", ".xlsx"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			KosmasModel.getInstance().getFileNames()
					.add(new RowRecord(f.getName().toUpperCase().replace(".CSV", ""), "", ""));
		}
		InfoModel.getInstance().updateInfo("Hotovo!");
	}

	public void prescoExcel(String fromDirectoryPath, String toDirectoryPath) {
		createDirectoriesIfDontExist(fromDirectoryPath, toDirectoryPath);
		for (File f : fromDirectory.listFiles()) {
			InfoModel.getInstance().updateInfo("Pracuju s " + f.getName());
			writeFileTwoInputs(readPrescoFile(f), toDirectoryPath, f.getName());
			f.delete();
		}
	}

	private List<ExcelRecord> readPrescoFile(File file) {
		List<ExcelRecord> records = new ArrayList<ExcelRecord>();
		try {
			Workbook wb = WorkbookFactory.create(file);
			Sheet sheet = wb.getSheetAt(0);
			sheet.removeRow(sheet.getRow(0));

			for (Row row : sheet) {
				if (row.getCell(1) == null) {
					break;
				}
				String ean = row.getCell(1).toString();
				int amount = (int) row.getCell(3).getNumericCellValue();
				System.out.println(ean + " " + amount);
				records.add(new ExcelRecord(ean, String.format("%d", amount)));
			}
			wb.close();

		} catch (EncryptedDocumentException | IOException e) {
			System.out.println("readFile in ExcelUtils");
			e.printStackTrace();
		}
		return records;
	}

	// public void betaExcel() {
	// List<ExcelRecord> records = new ArrayList<>();
	// File directory = new File(BetaModel.getInstance().getFromPath());
	// for (File f : directory.listFiles()) {
	// InfoModel.getInstance().updateInfo("Pracuju s " + f.getName());
	// try (BufferedReader br = new BufferedReader(new FileReader(f))) {
	// String line;
	// String ean = "", amount = "", price = "";
	// while ((line = br.readLine()) != null) {
	// if (line.contains("Ks")) {
	// amount = line.substring(54, 59);
	// }
	// if (line.contains("0.09")) {
	// ean = line.substring(line.indexOf("0.09") + 3, line.indexOf("0.09") + 16);
	// price = line.substring(line.indexOf("0.09") - 33, line.indexOf("0.09") - 24);
	// }
	//
	// if (line.contains("0.08")) {
	// ean = line.substring(line.indexOf("0.08") + 3, line.indexOf("0.08") + 16);
	// price = line.substring(line.indexOf("0.08") - 33, line.indexOf("0.08") - 24);
	// }
	//
	// if (!amount.isEmpty() && !ean.isEmpty()) {
	// records.add(
	// new ExcelRecord(ean, amount.replace(".0", ""), price.substring(0,
	// price.length() - 3)));
	// ean = "";
	// amount = "";
	// }
	// }
	// writeFileThreeInputs(records, BetaModel.getInstance().getToPath(),
	// f.getName().substring(0, f.getName().length() -
	// 3).toUpperCase().replaceAll("CNTSV-", "")
	// + "xlsx");
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// records.clear();
	// f.delete();
	// }
	// }

	List<ExcelRecord> records = new ArrayList<>();

	public void betaExcel() {
		File directory = new File(BetaModel.getInstance().getFromPath());
		for (File f : directory.listFiles()) {

			if (f.getName().toLowerCase().contains(".txt")) {
				try {
					handleBetaTxt(f);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (f.getName().toLowerCase().contains(".csv")) {
				try {
					handleBetaCsv(f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			records.clear();
			f.delete();
		}

	}

	private void handleBetaCsv(File f) throws IOException {
		InfoModel.getInstance().updateInfo("Pracuju s " + f.getName());
		boolean first = true;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			String ean = "", amount = "", price = "0";
			while ((line = br.readLine()) != null) {
				if (first == true) {
					first = false;
					continue;
				}
				String[] temp = line.split(";");
				ean = temp[0].replaceAll("\"", "");
				amount = temp[1];
				price = temp[3];

				records.add(new ExcelRecord(ean, amount, price));
			}
//			writeFileThreeInputs(records, BetaModel.getInstance().getToPath(),
//					f.getName().substring(0, f.getName().length() - 3).toUpperCase().replaceAll("CNTSV-", "") + "xlsx");
			writeFileThreeInputs(records, BetaModel.getInstance().getToPath(),
					f.getName().substring(0, f.getName().length() - 3) + "xlsx");
		}
	}

	private void handleBetaTxt(File f) throws Exception {
		InfoModel.getInstance().updateInfo("Pracuju s " + f.getName());
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			String ean = "", amount = "", price = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("Ks")) {
					amount = line.substring(54, 59);
				}
				if (line.contains("0.09")) {
					ean = line.substring(line.indexOf("0.09") + 3, line.indexOf("0.09") + 16);
					price = line.substring(line.indexOf("0.09") - 33, line.indexOf("0.09") - 24);
				}

				if (line.contains("0.08")) {
					ean = line.substring(line.indexOf("0.08") + 3, line.indexOf("0.08") + 16);
					price = line.substring(line.indexOf("0.08") - 33, line.indexOf("0.08") - 24);
				}

				if (line.contains("0.01")) {
					ean = line.substring(line.indexOf("0.01") + 3, line.indexOf("0.01") + 16);
					price = line.substring(line.indexOf("0.01") - 33, line.indexOf("0.01") - 24);
				}

				if (!amount.isEmpty() && !ean.isEmpty()) {
					records.add(new ExcelRecord(ean, amount.replace(".0", ""), price.substring(0, price.length() - 3)));
					ean = "";
					amount = "";
				}
			}
			writeFileThreeInputs(records, BetaModel.getInstance().getToPath(),
					f.getName().substring(0, f.getName().length() - 3) + "xlsx");
		}
	}

	private void writeFileFourInputs(List<ExcelRecord> records, String toDirectoryPath, String fileName) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet1 = workbook.createSheet();

		int i = 0;
		for (ExcelRecord r : records) {
			Row row = sheet1.createRow(i);
			row.createCell(0).setCellValue(r.getEan());
			row.createCell(1).setCellValue(r.getAmount());
			row.createCell(3).setCellValue(r.getPrice());
			row.createCell(2).setCellValue(r.getTotalPrice());
			i++;
		}

		for (int k = 0; k < records.size(); k++) {
			sheet1.autoSizeColumn(k);
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(toDirectoryPath + File.separator + fileName);
			workbook.write(fileOut);
			fileOut.close();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void writeFileThreeInputs(List<ExcelRecord> records, String toDirectoryPath, String fileName) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet1 = workbook.createSheet();

		int i = 0;
		for (ExcelRecord r : records) {
			Row row = sheet1.createRow(i);
			row.createCell(0).setCellValue(r.getEan());
			row.createCell(1).setCellValue(r.getAmount());
			row.createCell(3).setCellValue(r.getPrice());
			i++;
		}

		for (int k = 0; k < records.size(); k++) {
			sheet1.autoSizeColumn(k);
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(toDirectoryPath + File.separator + fileName);
			workbook.write(fileOut);
			fileOut.close();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createDirectoriesIfDontExist(String fromDirectoryPath, String toDirectoryPath) {
		fromDirectory = new File(fromDirectoryPath);
		toDirectory = new File(toDirectoryPath);
		if (!fromDirectory.exists()) {
			fromDirectory.mkdirs();
		}

		if (!toDirectory.exists()) {
			toDirectory.mkdirs();
		}
	}

	private void writeFileTwoInputs(List<ExcelRecord> records, String toDirectoryPath, String fileName) {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet1 = workbook.createSheet();
		int i = 0;
		for (ExcelRecord r : records) {
			Row row = sheet1.createRow(i);
			row.createCell(0).setCellValue(r.getEan());
			row.createCell(1).setCellValue(r.getAmount());
			i++;
		}
		for (int k = 0; k < records.size(); k++) {
			sheet1.autoSizeColumn(k);
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(toDirectoryPath + File.separator + fileName);
			workbook.write(fileOut);
			fileOut.close();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
