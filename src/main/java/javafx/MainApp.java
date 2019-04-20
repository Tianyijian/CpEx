package javafx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainApp extends Application{

	@Override
	public void start(Stage stage) {
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			
			init(root, stage);
			
			Scene scene = new Scene(root);
	        
	        stage.setTitle("Compile");
	        stage.setScene(scene);
	        stage.setResizable(false);	//
	        stage.show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void init(Parent root, Stage stage) {
		Button bt_cf_read_input = (Button) root.lookup("#btn_read_from_file");
		Button bt_yf_read_grammar = (Button) root.lookup("#bt_read_grammar");
		Button bt_yf_read_input = (Button) root.lookup("#bt_read_input");
		Button bt_yf_cf_input = (Button) root.lookup("#bt_cf_input");
		TextArea ta_input = (TextArea)root.lookup("#ta_input");
		TextArea ta_grammar = (TextArea)root.lookup("#ta_grammar");
		TextArea ta_yf_input = (TextArea)root.lookup("#ta_yf_input");
		TextArea ta_cf_result = (TextArea)root.lookup("#ta_result");
		//词法分析读入文件
		bt_cf_read_input.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
//					System.out.println(file.getPath());
					String content = readFile(file);
					ta_input.setText(content);
				}
				
			}
		});
		//语法分析读入语法
		bt_yf_read_grammar.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
//					System.out.println(file.getPath());
					String content = readFile(file);
					ta_grammar.setText(content);
				}
			}
		});
		//语法分析读入字符串
		bt_yf_read_input.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
//					System.out.println(file.getPath());
					String content = readFile(file);
					ta_yf_input.setText(content);
				}
			}
		});
		//语法分析获取词法分析token序列
		bt_yf_cf_input.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ta_yf_input.setText("Token:\n" + ta_cf_result.getText());
			}
		});
	}
	
	private String readFile(File file) {
		String content = "";
		try {
//			File file = new File
			Long fileLength = file.length();
			FileInputStream in = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(in, "utf-8");
//			byte[] bytes = new byte[fileLength.intValue()];
			char[] ch = new char[fileLength.intValue()];
			inputStreamReader.read(ch);
//			in.read(bytes);
			in.close();
			content = new String(ch);
//			System.out.println("content: " + content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

}
