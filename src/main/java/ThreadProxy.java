import java.io.*;
import java.net.Socket;

public class ThreadProxy extends Thread {
    private final String SERVER_URL;
    private final int SERVER_PORT;
    Socket client = null,
            server = null;
    private Socket sClient;

    public ThreadProxy(Socket sClient, String SERVER_URL, int SERVER_PORT) {
        this.sClient = sClient;
        this.SERVER_URL = SERVER_URL;
        this.SERVER_PORT = SERVER_PORT;

        this.start();
    }

    @Override
    public void run() {

        try {
            final byte[] request = new byte[1024];
            byte[] reply = new byte[4096];

            final InputStream inFromClient = sClient.getInputStream();
            final OutputStream outToClient = sClient.getOutputStream();

            try {
                server = new Socket(SERVER_URL, SERVER_PORT);
            } catch (IOException e) {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(outToClient));
                out.flush();
                throw new RuntimeException(e);
            }

            final InputStream inFromServer = server.getInputStream();
            final OutputStream outToServer = server.getOutputStream();

            new Thread() {
                @Override
                public void run() {
                    int bytes_read;
                    try {
                        while ((bytes_read = inFromClient.read(request)) != -1) {
                            outToServer.write(request, 0, bytes_read);
                            outToServer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        outToServer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            int bytes_read;
            try {
                while ((bytes_read = inFromServer.read(reply)) != -1) {
                    outToClient.write(reply, 0, bytes_read);
                    outToClient.flush();
                    //TODO CREATE YOUR LOGIC HERE
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (server != null)
                        server.close();
                    if (client != null)
                        client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            outToClient.close();
            sClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
