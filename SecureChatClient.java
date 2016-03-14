/* CS 1501
   Primitive chat client. 
   This client connects to a server so that messages can be typed and forwarded
   to all other clients.  Try it out in conjunction with ImprovedChatServer.java.
   You will need to modify / update this program to incorporate the secure elements
   as specified in the Assignment sheet.  Note that the PORT used below is not the
   one required in the assignment -- for your SecureChatClient be sure to 
   change the port that so that it matches the port specified for the secure
   server.
*/
import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.event.*;
import java.awt.*;

public class SecureChatClient extends JFrame implements Runnable, ActionListener {

    public static final int PORT = 8765; //5678;

    //BufferedReader myReader;
    //PrintWriter myWriter;
    JTextArea outputArea;
    JLabel prompt;
    JTextField inputField;
    JScrollPane scroller;
    String myName, serverName;
	Socket connection;
	// added instance variables
	SymCipher cipher;
	ObjectOutputStream myOutput;
	ObjectInputStream myInput;

    public SecureChatClient ()
    {
    	
    	try { 
    	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
        try {

        myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
        serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
        InetAddress addr = InetAddress.getByName(serverName);
        connection = new Socket(addr, PORT);   // Connect to server with new
                                               // Socket

        	//System.out.println("Connection created successfully");
        
         	
       // myWriter = new PrintWriter(new BufferedWriter( new OutputStreamWriter(connection.getOutputStream())), true);	 	
        	
        //myReader = new BufferedReader(  new InputStreamReader( connection.getInputStream()));   // Get Reader and Writer
        
        // --   connect reading and writing segments with the server   -- //
        
        myOutput = 
        		new ObjectOutputStream(connection.getOutputStream());
              
        // deadlock prevention
        myOutput.flush();
        
        	//System.out.println("Output Stream Created");
        
        myInput = 
        		new ObjectInputStream(connection.getInputStream());
        
        	//System.out.println("Input Stream Created");
         	
        // --   end read/write connect   -- //	
        
        	
        // --   receive	the public key (E) and mod value N  -- //
        	
        BigInteger E = (BigInteger)myInput.readObject();	
        BigInteger N = (BigInteger)myInput.readObject();	
        
        System.out.println();
        System.out.println("Received E: " + E);
        System.out.println();
        System.out.println("Received N: " + N);
        System.out.println();	
        
		// --   receives encryption type and processes key exchange   -- //
        
        String eType = (String) myInput.readObject();
        byte [] theKey = null;
        BigInteger key = null;
        cipher = null;
        
        System.out.println("Symmetric Cipher Type: " + eType);
        System.out.println();

        if(eType.equals("Sub")){
        	
        		//System.out.println("Substitution Cipher Received");
        	cipher = new Substitute();
        	theKey = new byte[256];
        	theKey = cipher.getKey();
        	
        	// creates a BigInt of positive sign
        	key = new BigInteger(1, theKey);
        	
        }else if(eType.equals("Add")){
        	
        		//System.out.println("Add128 Cipher Received");
        	cipher = new Add128();
        	theKey = new byte[128];
        	theKey = cipher.getKey();
        	
        	// creates a BigInt of positive sign
        	key = new BigInteger(1, theKey);
        	
        }else{
        		System.out.println("CIPHER SELECTION ERROR");
        		System.exit(0);;
        }
        // --   key exchange end   -- //
        
        // -- display key to console -- //
		System.out.println("Key: ");
		for (int i = 0; i < theKey.length; i++)
			System.out.print(theKey[i] + " ");
		System.out.println();
		//----//
        
		System.out.println();
		
        // --   RSA encryption and sending   -- //
        
        BigInteger eKey = key.modPow(E, N);
        myOutput.writeObject(eKey);
        myOutput.flush();
        
        // --   RSA end   -- //
        		
        
        // --   user-name encrypt handling   -- //	
        
        //myWriter.println(myName);  // Send name to Server.  Server will need
                                    // this to announce sign-on and sign-off
                                    // of clients

        this.setTitle(myName);      // Set title to identify chatter
        
        byte [] eName = cipher.encode(myName);
        myOutput.writeObject(eName);
        myOutput.flush();
        
        System.out.println();
        
        	//System.out.println("Name sent properly");

        // --   user-name end    -- //	
        	
        	
        // --   Set-up GUI for user   -- //
        
        Box b = Box.createHorizontalBox();  // Set up graphical environment for
        outputArea = new JTextArea(7, 30);  // user
        outputArea.setEditable(false);
        scroller = new JScrollPane(outputArea);
        b.add(scroller);
        
        DefaultCaret caret = (DefaultCaret) outputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    
        outputArea.append("Welcome to the Chat Group, " + myName + "\n");

        inputField = new JTextField("");  // This is where user will type input
        inputField.addActionListener(this);

        prompt = new JLabel("Type your messages below:");
        Container c = getContentPane();
        
        c.add(b, BorderLayout.NORTH);
        c.add(prompt, BorderLayout.CENTER);
        c.add(inputField, BorderLayout.SOUTH);

        Thread outputThread = new Thread(this);  // Thread is to receive strings
        outputThread.start();                    // from Server

		addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e){
                    		
                      String closing = "CLIENT CLOSING";
                      //myWriter.println(closing);
                      
                      // --   closing sentinel to server   -- //
                      
                    try {
                    	 	
	                    byte [] close = cipher.encode(closing);
	                    
	                    myOutput.writeObject(close);
	                    myOutput.flush();
	                                        
					} catch (IOException e1) {
						
						System.out.println("Connection Closure Incomplete: " + e1);
						
				    }

                      // --   end   -- //
                    
                    System.exit(0);
                      
                    }
                }
        );


        setSize(500, 200);
        setVisible(true);
        
        	//System.out.println("GUI Set-up successful");
        	
        // --   end GUI prep   -- //
        

        }catch (Exception ioe){
        	
            System.out.println("Problem starting client!");
            
        }
    }

    public void run()
    {
        while (true)
        {
             try {
            	   
            	// --   decode msg   -- // 
            	byte [] dMsg = (byte[]) myInput.readObject();
                String currMsg = cipher.decode(dMsg);
                // --   end   -- //
               
                	//System.out.println("received msg decoded");
			    outputArea.append(currMsg+"\n");
			    
			    System.out.println();
			   
             }
             catch (Exception e){
            	 
                System.out.println("...closing client!");
                break;
                
             }
        }
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e)
    {
        String currMsg = e.getActionCommand();      // Get input value
        inputField.setText("");
        //myWriter.println(myName + ":" + currMsg);   // Add name and send it
        											// to Server       
        // --   encode msg and send to server   -- //
        
        try{
        	
	        byte [] eMsg = cipher.encode(myName + ": " + currMsg);
	        
	        myOutput.writeObject(eMsg);
	        myOutput.flush();
	        
	        System.out.println();
	        	//System.out.println("sent msg encoded");
        
        }catch(IOException ioe){
        	
        	System.out.println(ioe + " ...client closing!");
        	System.exit(0);
        	
        }
        // --   encode end   -- //
        
    }                                              

    public static void main(String [] args)
    {
         SecureChatClient JR = new SecureChatClient();
         JR.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
