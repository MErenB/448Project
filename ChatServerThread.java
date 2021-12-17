//
// ChatServerThread.java
// created 02/18/03 by Ting Zhang
// Modified : Priyank K. Patel <pkpatel@cs.stanford.edu>
//

// Java
import java.util.*;
import java.math.BigInteger;

// socket
import java.net.*;
import java.io.*;


// Crypto
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class ChatServerThread extends Thread {


    private Socket _socket = null;
    private ChatServer _server = null;
    private Hashtable _records = null;
    private Mac mac;
    private int clientId;
    private PrintWriter outClient;
    private ClientRecord clientRecord;

    public ChatServerThread(ChatServer server, Socket socket,int clientId) {

        super("ChatServerThread");
        _server = server;
        _socket = socket;
        _records = server.getClientRecords();
        this.clientId = clientId;
    }

    public void run() {

        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    _socket.getInputStream()));

            clientRecord = (ClientRecord) _records.get(clientId);

            openOutStreamToClient();
            sendChatroomKeyToClient();


            String receivedMsg;
            while ((receivedMsg = in.readLine()) != null ) {
                if (receivedMsg.equals(CommunicationType.MSG_SEND.toString())){
                    String chatRoomId = in.readLine();
                    String cipherText = in.readLine();
                    String macText = in.readLine();

                    Mac mac = Mac.getInstance("HmacSHA256");

                    Enumeration theClients = _records.elements();

                    while (theClients.hasMoreElements()) {

                        ClientRecord c = (ClientRecord) theClients.nextElement();
                        if (c.getChatRoomId().equals(chatRoomId)){
                            Socket socket = c.getClientSocket();

                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(cipherText);
                        }else{
                            //Client A roomunda degilmis ona mesaj gitmez.
                        }

                    }

                }
                else if (receivedMsg.equals(CommunicationType.KEY.toString())){
                    String key = in.readLine();

                }
                else if(receivedMsg.equals(CommunicationType.CHATROOM_JOIN.toString())){
                    String iv = in.readLine();
                    String clientId = in.readLine();
                    String encrypted1 = in.readLine();
                    String mac1 = in.readLine();

                    byte[] ivClient = Base64.getDecoder().decode(iv);

                    if (checkMac(clientId,encrypted1,mac1)){
                        //send the chatroom key..
                        System.out.println("Mac is true..");

                        String chatRoomId = EncryptionUtil.decrypt(encrypted1,clientRecord.getSecretKey(),ivClient);

                        clientRecord.setChatRoomId(chatRoomId);


                    }


                }

            }

            _socket.shutdownInput();
            _socket.shutdownOutput();
            _socket.close();

        } catch (IOException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {

            e.printStackTrace();
        }

    }

    private void openOutStreamToClient() throws IOException {
        ClientRecord cl = (ClientRecord) _records.get(clientId);
        Socket socketCl = cl.getClientSocket();
        this.outClient = new PrintWriter(socketCl.getOutputStream(), true);
    }

    /*private void setClientId(){
        outClient.println(CommunicationType.SET_ID.toString());
        outClient.println(this.clientId);

    }*/

    private void sendChatroomKeyToClient() throws IOException {
        outClient.println(CommunicationType.KEY.toString());
        outClient.println(Base64.getEncoder().encodeToString(clientRecord.getSecretKey().getEncoded()));
    }

    private boolean checkMac(String clientId, String encrypted1,String mac1){

        String macTest = null;
        try {
            macTest = EncryptionUtil.getMac(clientRecord.getSecretKey(),""+encrypted1+clientId);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        if (macTest.equals(mac1)){
            return true;
        }
        else{
            return false;
        }
    }


}
