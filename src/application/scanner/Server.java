package application.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class Server {

	static ServerSocket serverScoket;
	static Socket socket;

	private String text, fileName;
	ArrayList<ServerArticle> listOfArticlesFromClient;
	int port = 8889;

	private Server() {

	}

	private static class LazyHolder {
		static final Server INSTANCE = new Server();
	}

	public static Server getInstance() {
		return LazyHolder.INSTANCE;
	}

	// tento
	public void waitForResponse(Label label, ArrayList<Article> articles, ObservableList<Article> dataForList,
			Label amountLabel, Label totalAmountOfBooks) {
		final Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() {
				try {
					serverScoket = new ServerSocket(port);
					serverScoket.setReuseAddress(true);
					while (true) {
						System.out.println("Server is waiting for response");
						changeLabel(label, "Naslouchá k portu číslo: " + port);
						socket = serverScoket.accept();
						System.out.println("Connected");
						try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
							text = br.readLine();
							System.out.println("TEST");
							listOfArticlesFromClient = convertStringToList(text);

							createTable(articles, dataForList, amountLabel, totalAmountOfBooks);

							changeLabel(label, "Konec spojení.");
						}
					}
				} catch (BindException e) {
					changeLabel(label, "Připojení selhalo, port už je používán.");
					System.out.println(e.getMessage() + " 1Thrown by " + e.getClass().getSimpleName());
				} catch (IOException e) {
					changeLabel(label, "IO exception.");
					System.out.println(e.getMessage() + " 2Thrown by " + e.getClass().getSimpleName());

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		new Thread(task).start();
	}

	public static void closeAll() throws IOException {

		if (serverScoket != null) {
			serverScoket.close();
		}

		if (socket != null) {
			socket.close();
		}

	}

	private void createTable(ArrayList<Article> articles, ObservableList<Article> dataForList, Label amountLabel,
			Label totalAmountOfBooks) {
		articles.clear();
		dataForList.clear();
		int amount = 0;
		for (ServerArticle a : listOfArticlesFromClient) {
			Article article = new Article(a.getEan());
			article.editAmountValue(a.getAmount());
			articles.add(article);
			amount += Integer.parseInt(a.getAmount());
		}
		dataForList.addAll(articles);
		createFile(articles);
		updateAmountLabels(amountLabel, dataForList, totalAmountOfBooks, amount);
	}

	private void createFile(ArrayList<Article> articles) {
		ExcelUtils excel = new ExcelUtils();
		excel.writeFileTwoInputs(articles, ScannerModel.getInstance().getSettings().getPath(), fileName);
	}

	private void changeLabel(Label label, String text) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				label.setText(text);
			}
		});
	}

	private void updateAmountLabels(Label amountLabel, ObservableList<Article> dataForList, Label totalAmountOfBooks,
			int amount) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				amountLabel.setText(dataForList.size() + "");
				totalAmountOfBooks.setText(amount + "");
			}
		});
	}

	public ArrayList<ServerArticle> getServerArticles() {
		return this.listOfArticlesFromClient;
	}

	public ArrayList<ServerArticle> convertStringToList(String txt) {
		ArrayList<ServerArticle> articles = new ArrayList<>();
		String[] rows = txt.split(";");
		fileName = rows[0] + ".xlsx";

		boolean first = true;
		for (String s : rows) {
			if (first) {
				first = false;
				continue;
			}
			String[] temp = s.split("\\.");
			articles.add(new ServerArticle(temp[0], temp[1]));
		}
		return articles;
	}

}
