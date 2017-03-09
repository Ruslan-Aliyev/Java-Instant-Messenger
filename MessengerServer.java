import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MessengerServer{
	public static void main(String [] args){
		Server s = new Server();
		s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		s.startRunning();

	}
}

class Server extends JFrame{
	private JTextField usertext;	//text your message here before you send it
	private JTextArea chatWindow;	//where conversation is displayed
	private ObjectOutputStream output;	//pakaged message texts from your comp to another comp
	private ObjectInputStream input;
	private ServerSocket server;	//the server on which this chat program will sit on
	private Socket connection;	//socket

	//constructor - set up GUI window's stuff here
	public Server(){
		super("Messenger");		//title of GUI window
		usertext= new JTextField();	
		usertext.setEditable(false);	//so cant write and sent text messages when you are not connected
		usertext.addActionListener(	
			new ActionListener(){
				public void actionPerformed(ActionEvent event){	//when user types and press enter, message gets send
					sendMessage(event.getActionCommand());	//this method sends the message
					usertext.setText("");			//clear textbox after send
				}
			}
		);
		add(usertext, BorderLayout.NORTH);

		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300, 150);
		setVisible(true);
	}

	//set up the server
	public void startRunning(){
		try{
			server=new ServerSocket(6789, 100);	//port number(client side needa know this number), max number of conn (AKA backlog)
			while(true){
				try{
					waitForConnection();
					setupStreams();		//set up out and in streams
					whileChatting();	//allows messages to be sent back n forth

				} catch (EOFException eofException){
					showMessage("\n Server ended the connection! "); //what happens when chat ends
				} finally {
					closeCrap();
				}
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	//wait for connection, then display connection info
	private void waitForConnection() throws IOException{
		showMessage("waiting for someone to connect ...\n");
		connection=server.accept();	//accepts someones convo request. When the infinity loop above receives another person's request, here a socket is created
		showMessage("now connected to "+connection.getInetAddress().getHostName());
	}

	//create streams
	private void setupStreams() throws IOException{
		output=new ObjectOutputStream(connection.getOutputStream());	//create stream to another comp
		output.flush();
		
		input=new ObjectInputStream(connection.getInputStream());
		//you cant flush their stream to you

		showMessage("\n Streams are now set up \n");
	}

	//the is the code thatll be running during convo
	private void whileChatting() throws IOException {
		String message = "You are now connected";
		showMessage(message);
		ableToType(true);	//now you can type into the text box
		do{	//have a convo while both of you wanna to
			try{
				message = (String)input.readObject();	//what the other person typed to us
				showMessage("\n"+message);	//show the other persons message to us on the screen
			}catch(ClassNotFoundException classNotFoundException){showMessage("\n idk wtf that user send! ");}	//if other person send to us a non string or an unknown object type
		}while(!message.equals("CLIENT - END"));
	}

	//closing streams
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

	//when you type into textbox and press enter, this method actually sends the message to the other comp
	private void sendMessage(String message){
		try{
			output.writeObject("SERVER - "+message);	//sends the message to the other comp
			output.flush();
			showMessage("\n Server -"+message);	//put the message into the convo box
		}catch (IOException ioException) {
			chatWindow.append("\n ERROR: Dude, I cant see message");	//puts a error message into the convo box
		}
	}
	
	//display message in chat window/convo box
	private void showMessage(final String text){
		SwingUtilities.invokeLater(	//appending more texts to the chat window without having to update the whole GUI, by setting aside a thread for updating a part of the GUI
			new Runnable(){
				public void run(){
					chatWindow.append(text);	//append new text to the end of the chat window, then chat window will be updated by this show message method
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











