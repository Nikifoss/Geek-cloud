package com.geekbrains.cloud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class FileMessageHandler implements Runnable{

    private final File dir;
    private final DataInputStream is;
    private final DataOutputStream os;

    public FileMessageHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        dir = new File("files");
        String[] files = dir.list();
        os.writeUTF("#list#");
        os.writeLong(files.length);
        for (String file : files) {
            os.writeUTF(file);
        }
    }

    private void sendFileOnClient() throws IOException {
        String file = is.readUTF();
        os.writeUTF("#fileReceive#");
        os.writeUTF(file);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                if(command.equals("#sendFileOnClient#")) {
                    sendFileOnClient();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
