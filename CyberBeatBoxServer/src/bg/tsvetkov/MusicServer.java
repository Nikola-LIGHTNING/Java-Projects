package bg.tsvetkov;

import java.util.*;
import java.io.*;
import java.net.*;

public class MusicServer {
    ArrayList<ObjectOutputStream> clientOutputStreams;
    
    public static void main(String[] args) {
        new MusicServer().go();
    }
    
    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket clientSocket;
        
        public ClientHandler(Socket sock) {
            try {
                clientSocket = sock;
                in = new ObjectInputStream(sock.getInputStream());
            } catch (Exception ex) {
                ex.printStackTrace();
            }           
        }
        
        @Override
        public void run() {
            Object o1 = null;            
            Object o2 = null;
            
            try {            
                if((o1 = in.readObject()) != null) {
                    o2 = in.readObject();
                    System.out.println("Read two objects.");
                    tellEveryone(o1, o2);                    
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }            
        }
    }

    
    public void go() {
        clientOutputStreams = new ArrayList<ObjectOutputStream>();
        try {
            ServerSocket serverSock = new ServerSocket(4242);
            
            while(true) {
                Socket clientSocket = serverSock.accept();
                
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(out);
                
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();      
                
                System.out.println("Got a connection");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }                      
    }
    
    private void tellEveryone(Object o1, Object o2) {
        Iterator it = clientOutputStreams.iterator();
        while(it.hasNext()) {
            try {
                ObjectOutputStream out = (ObjectOutputStream) it.next();
                out.writeObject(o1);
                out.writeObject(o2);                
            } catch (Exception ex) {
                ex.printStackTrace();
            } 
        }
    }
}
