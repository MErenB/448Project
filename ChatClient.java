//  ChatClient.java
//
//  Modified 1/30/2000 by Alan Frindell
//  Last modified 2/18/2003 by Ting Zhang 
//  Last modified : Priyank Patel <pkpatel@cs.stanford.edu>
//
//  Chat Client starter application.

//  AWT/Swing
import java.awt.*;
import java.awt.event.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;

//  Java
import java.io.*;

// socket
import java.net.*;


//  Crypto
import java.security.*;
import java.util.Base64;

public class ChatClient {

    public static final int SUCCESS = 0;
    public static final int CONNECTION_REFUSED = 1;
    public static final int BAD_HOST = 2;
    public static final int ERROR = 3;
    String _loginName;
    ChatServer _server;
    ChatClientThread _thread;
    ChatLoginPanel _loginPanel;
    ChatRoomPanel _chatPanel;
    ChatRoomSelectPanel _chatRoomSelectPanel;
    PrintWriter _out = null;
    BufferedReader _in = null;
    CardLayout _layout;
    JFrame _appFrame;

    private String chatRoomName;
    private SecretKey sessionKey;

    private int _clientId;

    private byte[] iv;

    public String getChatRoomName() {
        return chatRoomName;
    }

    Socket _socket = null;
    SecureRandom secureRandom;
    KeyStore clientKeyStore;
    KeyStore caKeyStore;
//    KeyManagerFactory keyManagerFactory;
//    TrustManagerFactory trustManagerFactory;
  
    //  ChatClient Constructor
    //
    //  empty, as you can see.
    public ChatClient() {

        _loginName = null;
        _server = null;

        //todo yanlissa update yaparsin iv lere.
        iv = EncryptionUtil.generateIv();

        try {
            initComponents();
        } catch (Exception e) {
            System.out.println("ChatClient error: " + e.getMessage());
            e.printStackTrace();
        }

        _layout.show(_appFrame.getContentPane(), "Login");

    }

    public void run() {
        _appFrame.pack();
        _appFrame.setVisible(true);

    }

    //  main
    //
    //  Construct the app inside a frame, in the center of the screen
    public static void main(String[] args) {
        
        ChatClient app = new ChatClient();

        app.run();
    }

    //  initComponents
    //
    //  Component initialization
    private void initComponents() throws Exception {

        _appFrame = new JFrame("BIL448 Chat");
        _layout = new CardLayout();
        _appFrame.getContentPane().setLayout(_layout);
        _loginPanel = new ChatLoginPanel(this);
        _chatPanel = new ChatRoomPanel(this);
        _chatRoomSelectPanel = new ChatRoomSelectPanel(this);
        _appFrame.getContentPane().add(_loginPanel, "Login");
        _appFrame.getContentPane().add(_chatPanel, "ChatRoom");
        _appFrame.getContentPane().add(_chatRoomSelectPanel,"ChatRoomSelect");
        _appFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }

    //  quit
    //
    //  Called when the application is about to quit.
    public void quit() {

        try {
            _socket.shutdownOutput();
            _thread.join();
            _socket.close();

        } catch (Exception err) {
            System.out.println("ChatClient error: " + err.getMessage());
            err.printStackTrace();
        }

        System.exit(0);
    }

    //
    //  connect
    //
    //  Called from the login panel when the user clicks the "connect"
    //  button. You will need to modify this method to add certificate
    //  authentication.  
    //  There are two passwords : the keystorepassword is the password
    //  to access your private key on the file system
    //  The other is your authentication password on the CA.
    //
    public int connect(String loginName, char[] password,
            String keyStoreName, char[] keyStorePassword,
            String caHost, int caPort,
            String serverHost, int serverPort) {

        try {

            _loginName = loginName;


            //
            //  Read the client keystore
            //         (for its private/public keys)
            //  Establish secure connection to the CA
            //  Send public key and get back certificate
            //  Use certificate to establish secure connection with server
            //

            _socket = new Socket(serverHost, serverPort);
            _out = new PrintWriter(_socket.getOutputStream(), true);

            _in = new BufferedReader(new InputStreamReader(
                    _socket.getInputStream()));

            _layout.show(_appFrame.getContentPane(),"ChatRoomSelect");


            _thread = new ChatClientThread(this);
            _thread.start();

            //todo burayi silersin ilk kismi implement ettikten sonra. Session key creation ve transfer icin.
            /*_out.println(CommunicationType.KEY);
            _out.println(keyStr);*/
            return SUCCESS;

        } catch (UnknownHostException e) {

            System.err.println("Don't know about the serverHost: " + serverHost);
            System.exit(1);

        } catch (IOException e) {

            System.err.println("Couldn't get I/O for "
                    + "the connection to the serverHost: " + serverHost);
            System.out.println("ChatClient error: " + e.getMessage());
            e.printStackTrace();

            System.exit(1);

        } catch (AccessControlException e) {

            return BAD_HOST;

        } catch (Exception e) {

            System.out.println("ChatClient err: " + e.getMessage());
            e.printStackTrace();
        }

        return ERROR;

    }


    public void enterChatRoom(){
        _layout.show(_appFrame.getContentPane(), "ChatRoom");
    }

    //todo once bu metodu cagirmak mantikli olabilir.
    public void joinChatRoom(String chatRoomName) {
        this.chatRoomName = chatRoomName;

        //todo şimdilik loginName veriliyo clientID olarak. Gerekiyorsa degistir.
        String encrypted1 = null;
        String mac1 = null;
        try {
            encrypted1 = EncryptionUtil.encrypt(this.chatRoomName,this.sessionKey,iv);
            mac1 = EncryptionUtil.getMac(this.sessionKey,""+encrypted1+this._loginName);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        _out.println(CommunicationType.CHATROOM_JOIN.toString());
        _out.println(Base64.getEncoder().encodeToString(iv));
        _out.println(this._loginName);
        _out.println(encrypted1);
        _out.println(mac1);

    }

    //  sendMessage
    //
    //  Called from the ChatPanel when the user types a carrige return.
    public void sendMessage(String msg) {

        try {
            //todo 1 blocka sigmayan mesajari da şifreleyip gonderme eklenecek.
            msg = _loginName + "> " + msg;

            String plainText = msg+ this.chatRoomName;


            SecretKey key = EncryptionUtil.generateKey(128);


            String cipherText = EncryptionUtil.encrypt(plainText,key,iv);


            String macResultString = EncryptionUtil.getMac(key, cipherText);


            sendMessagesToServer(this.chatRoomName,cipherText,macResultString);

        } catch (Exception e) {

            System.out.println("ChatClient err: " + e.getMessage());
            e.printStackTrace();
        }

    }



    public Socket getSocket() {

        return _socket;
    }

    public JTextArea getOutputArea() {

        return _chatPanel.getOutputArea();
    }


    private void sendMessagesToServer(String chatRoomId, String cipherText, String mac){
        _out.println(CommunicationType.MSG_SEND.toString());
        _out.println(chatRoomId);
        _out.println(cipherText);
        _out.println(mac);
    }


    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int get_clientId() {
        return _clientId;
    }

    public void set_clientId(int _clientId) {
        this._clientId = _clientId;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
}
