package com.geekbrains.cloud;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

public class FileMessageHandler implements Runnable{

    private final File dir;
    private final DataInputStream is;
    private final DataOutputStream os;

    public FileMessageHandler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        dir = new File("geek-cloud-server/files");
        String[] files = dir.list();
        os.writeUTF("#list#");
        os.writeLong(files.length);
        for (String file : files) {
            os.writeUTF(file);
        }
    }

    private void sendFileOnClient() throws IOException {
        String fileName = is.readUTF();
        os.writeUTF("#fileReceive#");
        os.writeUTF(fileName);
        File file = dir.toPath().resolve(fileName).toFile();
        os.writeLong(file.length());
        byte [] buffer = new byte[256];
        try (InputStream fis = new FileInputStream(file)) {
            while (fis.available() > 0) {
                int readCount = fis.read(buffer);
                os.write(buffer,0 , readCount);
            }
        }
        os.flush();
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
