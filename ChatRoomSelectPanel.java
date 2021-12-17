import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatRoomSelectPanel extends JPanel {

    JTextField _chatRoomName;
    JButton _connectButton;
    ChatClient _client;

    public ChatRoomSelectPanel(ChatClient client){
        _client = client;

        try {
            componentInit();
        }catch (Exception e){

        }

    }

    private void componentInit() {
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridBag);

        addLabel(gridBag,"Connection is successful.\nPlease enter the name of the Chatroom that you want to join",SwingConstants.CENTER,1,0,2,1);

        addLabel(gridBag,"Chatroom name: ",SwingConstants.CENTER,1,1,1,1);
        _chatRoomName = new JTextField();
        addField(gridBag, _chatRoomName, 2, 1, 1, 1);


        _connectButton = new JButton("Connect");
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 2;
        gridBag.setConstraints(_connectButton, c);
        add(_connectButton);


        _connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinChatRoom();
                _client.enterChatRoom();

                //Connect to chatroom islemi yapilacak.
            }
        });

    }


    private void joinChatRoom(){
        String chatRoomName = _chatRoomName.getText();
        _client.joinChatRoom(chatRoomName);
    }


    JLabel addLabel(GridBagLayout gridBag, String labelStr, int align,
                    int x, int y, int width, int height) {
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel(labelStr);
        if (align == SwingConstants.LEFT) {
            c.anchor = GridBagConstraints.WEST;
        } else {
            c.insets = new Insets(10, 0, 10, 0);
        }
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        gridBag.setConstraints(label, c);
        add(label);

        return label;
    }

    void addField(GridBagLayout gridBag, JTextField field, int x, int y,
                  int width, int height) {
        GridBagConstraints c = new GridBagConstraints();
        field.setPreferredSize(new Dimension(96,
                field.getMinimumSize().height));
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        gridBag.setConstraints(field, c);
        add(field);
    }


}
