package proxy;

import chat.ActualChatter;
import chat.Chatter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatProviderClientStub implements IChatProvider {

    public static int CHATTER_MARSHALL_ID = 0;
    public synchronized static int createMarschallId () {
        int id = CHATTER_MARSHALL_ID;
        ChatProviderClientStub.CHATTER_MARSHALL_ID += 1;
        return id;
    }

    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;

    private Map<Chatter, Integer> chatterCommunicationIdMap = new HashMap<>();
    private int communicationId = 0;

    public ChatProviderClientStub (String host, int port) {
        try {
            this.socket = new Socket(host, port);

            if (this.openInputStream() == false || this.openOutputStream() == false) {
                return;
            }

            String headerLine;
            while ((headerLine = this.in.readLine()) != null) {
                if (headerLine.equalsIgnoreCase("----")) {
                    break;
                }
                System.out.println("[INFORMATION]: Server said: " + headerLine);
            }


        } catch (Throwable t) {
            System.out.println("[ERROR]: beim öffnen des Sockets ist folgender Fehelr aufgetreten:");
            t.printStackTrace();
        }
    }

    public void marshallChatter (Chatter chatter) throws Exception {
        int communicationId;
        if (this.chatterCommunicationIdMap.containsKey(chatter) == false) {
            communicationId = ChatProviderClientStub.createMarschallId ();
            this.chatterCommunicationIdMap.put(chatter, communicationId);
        } else {
            communicationId = this.chatterCommunicationIdMap.get (chatter);
        }

        // Hier würde eigentlich noch ein .readLine kommen. Dies wird jedoch in den einzelnen Funktionen
        // vor dem Aufruf von .marschallChatter getätigt. :D
        this.out.println(communicationId);

        String sStatus = this.in.readLine();
        System.out.println("[INFORMATION]: server said: " + sStatus);
        if (sStatus.equalsIgnoreCase("!- Gib mir den Chatter-Port (int), bitte.")) {

            boolean foundPort = false;
            int port = 0;
            Random random = new Random();
            ServerSocket chatterServer = null;
            while (foundPort == false) {
                port = 1024 + random.nextInt(8975);

                try {
                    chatterServer = new ServerSocket (port);
                    foundPort = true;
                } catch (Throwable t) { continue; }
            }

            System.out.println("[INFORMATION]: found open port: " + port);

            System.out.println("[INFORMATION]: send \"" + port + "\" to server");
            this.out.println(port);
            Socket client = chatterServer.accept();

            new Thread(new ChatterServerStub(client, chatter)).start();
            System.out.println("[INFORMATION]: connection established... marshalling fertig");
        } else {
            System.out.println("[INFORMATION]: kein marshalling benötigt");
        }
    }

    public boolean openInputStream () {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            return true;
        } catch (IOException e) {
            System.out.println("[ERROR]: beim öffnen des Input-Streams ist folgender Fehelr aufgetreten:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean openOutputStream () {
        try {
            this.out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            return true;
        } catch (IOException e) {
            System.out.println("[ERROR]: beim öffnen des Output-Streams ist folgender Fehelr aufgetreten:");
            e.printStackTrace();
            return false;
        }
    }

    public void killConnection () {
        try {
            System.out.println("[INFORMATION]: sent 0 (kill) to server.");
            this.out.println("0");

            System.out.println("[INFORMATION]: server said: " + this.in.readLine());

            String killLine = this.in.readLine();
            if (killLine.equalsIgnoreCase("!- Who do I need to kill?")) {
                for (int commId : this.chatterCommunicationIdMap.values()) {
                    System.out.println("[INFORMATION]: send excecution order for comm-id " + commId);
                    this.out.println(commId);
                }
                this.out.println("----");
            }

            System.out.println("[INFORMATION]: server said: " + this.in.readLine());
            this.in.close();
            this.out.close();
            this.socket.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    @Override
    public void joinChat(Chatter chatter) {
        try {
            System.out.println("[INFORMATION]: sent 1 to server.");
            this.out.println("1");
            System.out.println("[INFORMATION]: server said: " + this.in.readLine());

            String commandOrStatus = this.in.readLine();
            System.out.println("[INFORMATION]: server said: " + commandOrStatus);

            this.marshallChatter (chatter);

            String sStatus = this.in.readLine();
            System.out.println("[INFORMATION]: server said: " + sStatus);

            if (sStatus.equalsIgnoreCase("1") == true) {
                System.out.println("[INFORMATION]: .joinServer(Chatter chatter) executed successfully.");
            } else if (sStatus.equalsIgnoreCase("0") == true) {
                throw new Exception(this.in.readLine());
            }

        } catch (Throwable t) {
            System.out.println("[ERROR]: bei .joinChat(Chatter chatter) ist folgender Fehler aufgetreten:");
            t.printStackTrace();
        }
    }

    @Override
    public void leaveChat(Chatter chatter) {
        try {
            System.out.println("[INFORMATION]: sent 2 to server.");
            this.out.println("2");
            System.out.println("[INFORMATION]: server said: " + this.in.readLine());

            String commandOrStatus = this.in.readLine();
            System.out.println("[INFORMATION]: server said: " + commandOrStatus);

            this.marshallChatter (chatter);

            String sStatus = this.in.readLine();
            System.out.println("[INFORMATION]: server said: " + sStatus);

            if (sStatus.equalsIgnoreCase("1") == true) {
                System.out.println("[INFORMATION]: .joinServer(Chatter chatter) executed successfully.");
            } else if (sStatus.equalsIgnoreCase("0") == true) {
                throw new Exception(this.in.readLine());
            }

        } catch (Throwable t) {
            System.out.println("[ERROR]: bei .joinChat(Chatter chatter) ist folgender Fehler aufgetreten:");
            t.printStackTrace();
        }
    }

    @Override
    public void sendMessage(Chatter chatter, String message) {
        try {
            System.out.println("[INFORMATION]: sent 3 to server.");
            this.out.println("3");
            System.out.println("[INFORMATION]: server said: " + this.in.readLine());

            String commandOrStatus = this.in.readLine();
            System.out.println("[INFORMATION]: server said: " + commandOrStatus);

            this.marshallChatter (chatter);

            System.out.println("[INFORMATION]: server said: " + this.in.readLine());
            System.out.println("[INFORMATION]: send \"" + message + "\" to server.");
            this.out.println(message);

            String sStatus = this.in.readLine();
            System.out.println("[INFORMATION]: server said: " + sStatus);

            if (sStatus.equalsIgnoreCase("1") == true) {
                System.out.println("[INFORMATION]: .sendMessage(Chatter chatter, String message) executed successfully.");
            } else if (sStatus.equalsIgnoreCase("0") == true) {
                throw new Exception(this.in.readLine());
            }

        } catch (Throwable t) {
            System.out.println("[ERROR]: bei .sendMessage(Chatter chatter, String message) ist folgender Fehler aufgetreten:");
            t.printStackTrace();
        }
    }
}
