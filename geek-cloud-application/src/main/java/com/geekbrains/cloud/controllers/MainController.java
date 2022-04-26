package com.geekbrains.cloud.controllers;

import com.geekbrains.cloud.network.Net;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private Net net;

    private File dir = new File("geek-cloud-application/files");

    public Button btn_get;
    public Button btn_send;

    public ListView<String> listViewClient;
    public ListView<String> listViewServer;

    public TextField input;

    private void readListFilesOnServer() {
        try {
            listViewServer.getItems().clear();
            Long filesCount = net.readLong();
            for (int i = 0; i < filesCount; i++) {
                String fileName = net.readUtf();
                listViewServer.getItems().addAll(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fileReceiveFromServer() throws IOException {
        String fileName = net.readUtf();
        File file = dir.toPath().resolve(fileName).toFile();

        long size = net.readLong();

        byte[] buffer = new byte[256];

        try (OutputStream fos = new FileOutputStream(file)) {
            for (int i = 0; i < (size + 255)/ 256; i++) {
                int readCount = net.read(buffer);
                fos.write(buffer, 0, readCount);
            }
        }
        input.setText("Ok!");
        net.flush();
        updateListOfFiles();
    }

    private void read() {
        try {
            while (true) {
                String command = net.readUtf();
                if (command.equals("#list#")) {
                    readListFilesOnServer();
                }
                if (command.equals("#fileReceive#")) {
                    fileReceiveFromServer();
                }
                if (command.equals("#status#")) {
                    String status = net.readUtf();
                    input.setText(status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickMethodDownloadFileFromServer(){
        try {
            String selectedFileName = listViewServer.getSelectionModel().getSelectedItem();
            if(selectedFileName != null) {
                net.sendUtf("#sendFileOnClient#");
                net.sendUtf(selectedFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateListOfFiles() {
            listViewClient.getItems().clear();
            listViewClient.getItems().addAll(dir.list());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            net = new Net("localhost", 8189);
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
            updateListOfFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
