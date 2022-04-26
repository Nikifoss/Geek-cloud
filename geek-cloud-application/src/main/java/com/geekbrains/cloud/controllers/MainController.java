package com.geekbrains.cloud.controllers;

import com.geekbrains.cloud.network.Net;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private Net net;

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
        String file = net.readUtf();
        listViewClient.getItems().add(file);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickMethodDownloadFileFromServer(){
        try {
            MultipleSelectionModel<String> selectFile = listViewServer.getSelectionModel();
            String selectedItem = selectFile.getSelectedItem();
            if(selectedItem != null) {
                net.sendUtf("#sendFileOnClient#");
                net.sendUtf(selectedItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            net = new Net("localhost", 8189);
            Thread readThread = new Thread(this::read);
            readThread.setDaemon(true);
            readThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
