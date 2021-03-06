package App;

import Records.Message;
import Records.NodeInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Handles receiving of messages (ie server functionality).
public class ListenThread extends Thread {
    ServerSocket listenSocket = null;

    // Sets up listening thread.
    public ListenThread(ServerSocket listenSocket) {
        this.listenSocket = listenSocket;
    }

    // Handles incoming messages based on message type.
    private void handleMessage(Message messageFromClient, ObjectOutputStream out){
        Message propMessage = null;
        NodeInfo nodeInfo = null;
        String propText = null;
        String clientName = null;

        switch (messageFromClient.type()) {
            case JOIN -> { // Send nodeList of peers to joiner and add joiner to own list.
                System.out.println("Received a JOIN message.");
                nodeInfo = (NodeInfo) messageFromClient.contents();
                ClientThread.connected = true;
                try { // Try sending list of peers.
                    out.writeObject(Peer.nodeList);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Puts client info into arrayList and starts a server thread for that client.
                clientName = nodeInfo.name();
                Peer.nodeList.add(nodeInfo);
                // Craft join message.
                propText = "'" + clientName + "' joined chat.";
            }
            case JOIN_INFO -> { // Adds info of new participant to nodeList of peers.
                // Puts client info into arraylist and starts a server thread for that client.
                nodeInfo = (NodeInfo) messageFromClient.contents();
                clientName = nodeInfo.name();
                Peer.nodeList.add(nodeInfo);
                // Craft join message.
                propText = "'" + clientName + "' joined chat.";
            }
            case LEAVE -> { // Remove leaver from peer nodeList.
                // Removes client from arraylist.
                nodeInfo = (NodeInfo) messageFromClient.contents();
                clientName = nodeInfo.name();
                Peer.nodeList.remove(nodeInfo);
                // Craft leave message.
                propText = "'" + clientName + "' left chat.";
            }
            case NOTES -> { // Standard message.
                // Formats and propagates text from client messages.
                String text = (String) messageFromClient.contents();
                // Craft message.
                propText = text;
            }
        }
        // Output whatever message came in.
        System.out.println(propText);
    }

    // Handles running server functions.
    public void run(){
        ObjectInputStream fromPeer = null;
        ObjectOutputStream toPeer = null;
        Message messageFromPeer = null;

        // Talk to the client.
        // Socket loop.
        while (true) {
            try {
                // From socket should listen waiting for connection, then on accept open object stream, then read message.
                // Close object stream, and create thread to do sending to peers and opens socket again.
                // Create and open socket.
                Socket socket = listenSocket.accept();
                toPeer = new ObjectOutputStream(socket.getOutputStream());
                fromPeer = new ObjectInputStream(socket.getInputStream());
                messageFromPeer = (Message) fromPeer.readObject();
                handleMessage(messageFromPeer, toPeer);
                // System.out.println("DEBUG: Received message " + messageFromClient);
                fromPeer.close();
                socket.close();
            } catch (Exception e) {
                System.err.println("Error reading character from client.");
                e.printStackTrace();
                return;
            }
        }
    }
}