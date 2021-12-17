//  ClientRecord.java

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

// You may need to expand this class for anonymity and revocation control.
public class ClientRecord {

    private Socket _socket = null;
    private SecretKey secretKey;
    private String chatRoomId;

    public ClientRecord(Socket socket) throws NoSuchAlgorithmException {
        _socket = socket;

        secretKey = EncryptionUtil.generateKey(128);

    }

    public Socket getClientSocket() {

        return _socket;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
