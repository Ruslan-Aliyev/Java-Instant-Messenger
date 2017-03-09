
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MessengerClient{
	public static void main(String [] args){
		Client c=new Client("127.0.0.1");
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.startRunning();
	}
}


class Client extends JFrame{
	private JTextField usertext;	//text your message here before you send it
	private JTextArea chatWindow;	//where conversation is displayed
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message ="";
	private String serverIP;
	private Socket connection;

	//constructor, GUI
	public Client(String host){	//host is the ip add of the server
		super("Client");
		serverIP=host;
		usertext=new JTextField();
		usertext.setEditable(false);
		usertext.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){	//when user types and press enter, message gets send
					sendMessage(event.getActionCommand());	//gets the text inside the textbox
					usertext.setText("");	//reset text area to blank
				}
			}
		);
		add(usertext, BorderLayout.NORTH);

		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300, 150);
		setVisible(true);
	}

	//connect to server
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch (EOFException eofException){
			showMessage("\n client ended the connection! "); 
		}catch (IOException ioException){
			ioException.printStackTrace(); 
		} finally {
			closeCrap();
		}
	}

	private void connectToServer() throws IOException{
		showMessage("attempt to connect ...\n");
		connection=new Socket(InetAddress.getByName(serverIP), 6789);	//ip add & port
		showMessage("connected to "+connection.getInetAddress().getHostName());
	}

	private void setupStreams() throws IOException{
		output=new ObjectOutputStream(connection.getOutputStream());	//create stream to another comp
		output.flush();
		
		input=new ObjectInputStream(connection.getInputStream());
		//you cant flush their stream to you

		showMessage("\n Streams are now set up \n");
	}

	private void whileChatting() throws IOException {
		ableToType(true);
		do{	
			try{
				message = (String)input.readObject();	
				showMessage("\n"+message);	//show the other persons message to us on the screen
			}catch(ClassNotFoundException classNotFoundException){showMessage("\n donno that obj type ");}	//if other person send to us a non string or an unknown object type
		}while(!message.equals("SERVER - END"));
	}

	private void closeCrap() {
		showMessage("\n Closing connection ... \n");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	private void sendMessage(String message){
		try{
			output.writeObject("CLIENT - "+message);	//sends the message to the other comp
			output.flush();
			showMessage("\n CLIENT -"+message);	//put the message into the convo box
		}catch (IOException ioException) {
			chatWindow.append("\n smt messed up message sending");	//puts a error message into the convo box
		}
	}

	private void showMessage(final String m){
		SwingUtilities.invokeLater(	//appending more texts to the chat window without having to update the whole GUI, by setting aside a thread for updating a part of the GUI
			new Runnable(){
				public void run(){
					chatWindow.append(m);	//append new text to the end of the chat window, then chat window will be updated by this show message method
				}
			}
		);
	}

	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(	
			new Runnable(){
				public void run(){
					usertext.setEditable(tof);
				}
			}
		);
	}

}
