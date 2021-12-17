/**
 *  Created 2/16/2003 by Ting Zhang 
 *  Part of implementation of the ChatClient to receive
 *  all the messages posted to the chat room.
 */

// socket
import java.net.*;
import java.io.*;

//  Swing
import javax.swing.JTextArea;

//  Crypto
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ChatClientThread extends Thread {

    private ChatClient _client;
    private JTextArea _outputArea;
    private Socket _socket = null;

    public ChatClientThread(ChatClient client) {

        super("ChatClientThread");
        _client = client;
        _socket = client.getSocket();
        _outputArea = client.getOutputArea();
    }

    public void run() {

        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    _socket.getInputStream()));

            String msg;

            while ((msg = in.readLine()) != null) {

                if (msg.equals(CommunicationType.KEY.toString())){
                    //Cliente session key yollandi.
                    String sessionKeyStr = in.readLine();
                    byte[] decodedKey = Base64.getDecoder().decode(sessionKeyStr);
                    SecretKey sessionKey = new SecretKeySpec(decodedKey,0,decodedKey.length,"AES");
                    _client.setSessionKey(sessionKey);
                }
                /*else if (msg.equals(CommunicationType.SET_ID.toString())){
                    String clientId = in.readLine();
                    _client.set_clientId(Integer.parseInt(clientId));

                }*/else {
                    consumeMessage(msg + " \n");
                }


            }

            _socket.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void consumeMessage(String msg) {


        if (msg != null) {
            _outputArea.append(msg);
        }

    }
}
