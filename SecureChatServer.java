/* CS 1501 Summer 2015
   Main Program for Chat Server to be used with Assignment 4
   You must use the program as written without any changes.  Note that this program
   requires the following:
   		keys.txt where the RSA keys are stored
   		SymCipher.java, the interface for the symmetric ciphers
   		Substitute.java, the substitution cipher
   		Add128.java, the addition cipher
   	The first two files are provided for you, but you must write the other two, as well
   	as the client program, yourself.
   	See more details on these requirements in the assignment sheet.
*/
import java.util.*;
import java.io.*;
import java.net.*;
import java.math.*;


/* there are numerous System.out statements throughout used for testing purposes *
 * they should be commented out unless I miss them when checking back  			 *
 * over the files 																 */
public class SecureChatServer {

    public static final int PORT = 8765;

    private int MaxUsers;
    private Socket [] users;         // Need array of Sockets and Threads,
    private UserThread [] threads;   // one of each per client -- using an
    private int numUsers;            // ArrayList for these would make less work
                                     // for the programmer
	private String StringE, StringD, StringN;
	private BigInteger E, D, N;
	Random R;

    public SecureChatServer(int MaxU) throws IOException
    {
        MaxUsers = MaxU;
        users = new Socket[MaxUsers];
        threads = new UserThread[MaxUsers];   // Set things up and start
        numUsers = 0;                         // Server running

		Scanner inScan = new Scanner(new File("keys.txt"));
		StringE = inScan.nextLine();
		StringD = inScan.nextLine();
		StringN = inScan.nextLine();
		E = new BigInteger(StringE);
		D = new BigInteger(StringD);
		N = new BigInteger(StringN);
		R = new Random();
		
		// console display of E, D, and N
		System.out.println();
		System.out.println("My E: " + E);
		System.out.println();
		System.out.println("My D: " + D);
		System.out.println();
		System.out.println("My N: " + N);
		System.out.println();
		
        try
        {
			System.out.println("Server starting up");
            runServer();
        }
        catch (Exception e)
        {
           System.out.println("Problem with server " + e.getMessage());
        }
    }

    public synchronized void SendMsg(String msg)
			// Send current message to all clients (even sender).  This
            // must be synchronized so that chatters do not "interrupt"
            // each other.  For each chatter, get the cipher, then use
            // it to encode the message, and send the result.
                        
    {
        for (int i = 0; i < numUsers; i++)
        {
			System.out.println("Sending to user " + i);
            ObjectOutputStream currWriter = threads[i].getWriter();
			SymCipher currCipher = threads[i].getCipher();
			byte [] sendmsg = currCipher.encode(msg);
			try
			{
					System.out.println("Encrypted Msg Sent");
				currWriter.writeObject(sendmsg);
			}
			catch (IOException e)
			{
				System.out.println("Output error " + e);
			}
        }
    }

    public synchronized void removeClient(int id, String name)
    {
        try                          // Remove a client from the server.  This
        {                            // also must be synchronized, since we
            users[id].close();       // could have an inconsistent state if this
        }                            // is interrupted in the middle
        catch (IOException e)
        {
            System.out.println("Already closed");
        }
        users[id] = null;
        threads[id] = null;
        for (int i = id; i < numUsers-1; i++)   // Shift remaining clients in
        {                                       // array up one position
            users[i] = users[i+1];
            threads[i] = threads[i+1];
            threads[i].setId(i);
        }
        numUsers--;
        SendMsg(name + " has logged off");    // Announce departure
    }

    private void runServer() throws IOException
    {

    ServerSocket s = new ServerSocket(PORT);
	System.out.println("Started: " + s);
    Socket newSocket = null;
    	//System.out.println("Socket Initialized");
    
	try
	{
		while (true)
		{
			if (numUsers < MaxUsers)
			{
				try
				{
				// INITIALIZATION PROCESSES	
					
					newSocket = s.accept();    		// get next client
					newSocket.setSoTimeout(20000);  // timeout if client does not respond
					                                // within 20 seconds.  This keeps
					          						// server from getting hung up by an unresponsive client
													// during the handshaking phase
					
					ObjectOutputStream tempWriter =
							  new ObjectOutputStream(newSocket.getOutputStream());
					
						//System.out.println("Output Stream Created");
					
					// avoid deadlock
					tempWriter.flush();
					
					ObjectInputStream tempReader =
							  new ObjectInputStream(newSocket.getInputStream());
					
						//System.out.println("Input Stream Created");
					
					System.out.println("Sending E"); // output E
					tempWriter.writeObject(E); tempWriter.flush();
					System.out.println("Sending N"); // output N
					tempWriter.writeObject(N); tempWriter.flush();
					double test = R.nextDouble();
					String encType = null;
					
					// Randomly determine which cipher will be used and send the
					// appropriate string to the client.
					if (test > 0.5){
						// sub cipher selection and test notification
							//System.out.println("Using Substitute Cipher");
						encType = new String("Sub");	
					}else{
						// add cipher selection and test notification
							//System.out.println("Using Add128 Cipher");
						encType = new String("Add");
					}	
					
					// writes encryption type for client
					tempWriter.writeObject(encType);
					tempWriter.flush();
					
						System.out.println();
					
						//String addKey = tempReader.readLine();
					BigInteger bigKey = (BigInteger) tempReader.readObject();
					System.out.println("Encrypted key: " + bigKey);
					bigKey = bigKey.modPow(D, N);
						System.out.println();
					System.out.println("Decrypted key: " + bigKey);
					byte [] byteKey = bigKey.toByteArray();
						System.out.println();
					System.out.println("Byte array length: " + byteKey.length);
					SymCipher cipher = null;
					
					// Since the key is sent from the client as a positive BigInteger,
					// when converted back the result could have an extra byte.  This
					// code tests for that byte and removes it if necessary.
					
					// creates key for proper encryption use
					if (encType.equals("Add") && byteKey.length > 128)
					{
						// creates key for Add cipher and test notification
							//System.out.println("Creating Add128 Cipher Key");
						byte [] temp = new byte[128];
						System.arraycopy(byteKey, 1, temp, 0, 128);
						byteKey = temp;
					}
					else if (encType.equals("Sub") && byteKey.length > 256)
					{
						// creates key for Sub cipher and test notification
							//System.out.println("Creating Substitution Cipher Key");
						byte [] temp = new byte[256];
						System.arraycopy(byteKey, 1, temp, 0, 256);
						byteKey = temp;
					}
					
					// creates cipher for encryption and decryption
					if (encType.equals("Add"))
					{
						// Add cipher creation and test notif.
						System.out.println("Chosen Cipher is Add128");
						cipher = new Add128(byteKey);
					}
					else
					{
						// Sub cipher creation and test notif.
						System.out.println("Chosen Cipher is Substitute");
						cipher = new Substitute(byteKey);
					}
						
						System.out.println();
					
					// display key to console
					System.out.println("Key: ");
					for (int i = 0; i < byteKey.length; i++)
						System.out.print(byteKey[i] + " ");
					System.out.println();
					
						System.out.println();
					
					// Get the Client's name (note that this is encoded).  Then
					// pass it on to all of the current chatters before adding
					// the new chatter to the list.
					
						//System.out.println("[before read object");
					
					byte [] newBytes = (byte []) tempReader.readObject();
					String newName = cipher.decode(newBytes);
					
						//System.out.println("after read object]");
					
					newSocket.setSoTimeout(0);  // A 0 value for the timeout is actually
					        // no timeout -- once the client is correctly connected there
					        // can be an arbitrary delay between messages.
					synchronized (this)
					{
						
						// server gets the new chatter's and announces
						// to the rest of the group
						users[numUsers] = newSocket;
						SendMsg(newName + " has just joined the chat group");
						
							System.out.println("Name Successfully Sent");

						// new thread is created and started for the new user
						threads[numUsers] = new UserThread(newSocket, numUsers,
													newName, tempReader, tempWriter, cipher);
						threads[numUsers].start();
						
						System.out.println("Connection " + numUsers + users[numUsers]);
						numUsers++;
						
					}
				}
				catch (java.net.SocketTimeoutException e1)
				{
					System.out.println("Client timed out during login " + e1);
					newSocket.close();
				}
				catch (Exception e2)
				{
					System.out.println("Problem with connection " + e2.getMessage());
					e2.printStackTrace();
					newSocket.close();
				}
			}  // if
			else
			{
				Thread.sleep(1000);
			}

		}  // while
	}   // try
	catch (Exception e)
	{
		System.out.println("Something went wrong " + e);
	}
    finally 
    {
        System.out.println("Server shutting down");

    }
    }  //  end of runServer method

    // Below is the class used by the server to keep track of the clients.  Each
    // client is a new UserThread object, with the data shown.  Note that the cipher
    // being used is encapsulated within the UserThread, so that each client can have
    // a different encryption scheme, as long as it satisfies the SymCipher interface.

    private class UserThread extends Thread
    {
		 private Socket mySocket;
         private ObjectInputStream myReader;
         private ObjectOutputStream myWriter;
         private SymCipher myCipher;
         private int myId;
         private String myName;

         private UserThread(Socket newSocket, int id, String newName,
                            ObjectInputStream newReader,
							ObjectOutputStream newWriter, SymCipher c) throws IOException
         {
              mySocket = newSocket;
              myId = id;
              myName = newName;
              myReader = newReader;
			  myWriter = newWriter;
			  myCipher = c;
         }

         public ObjectInputStream getReader()
         {
              return myReader;
         }

         public ObjectOutputStream getWriter()
         {
              return myWriter;
         }

		 public SymCipher getCipher()
		 {
			 return myCipher;
		 }

         public synchronized void setId(int newId)
         {
              myId = newId;   // id may change when a previous chatter quits
         }

         // While running, each UserThread will get the next message from its
         // corresponding client, and then send it to the other clients (through
         // the Server).  A departing client is detected by an IOException in
         // trying to read, which causes the removeClient method to be executed.

         public void run()
         {
              boolean ok = true;
              while (ok)
              {
                    String newMsg = null;
					byte[] newBytes = null;
                    try {
                         newBytes = (byte []) myReader.readObject(); 
						 newMsg = myCipher.decode(newBytes);
						 if (newBytes == null || newMsg.equals("CLIENT CLOSING")){
							 	ok = false;
						 }else{
                             	SecureChatServer.this.SendMsg(newMsg);
						 }
				    }
                    catch (Exception e)
                    {
                        System.out.println("Client closing!!" + e);
                        ok = false;
                    }
              }
              removeClient(myId, myName);
         }
    }

    public static void main(String [] args) throws IOException
    {
         SecureChatServer Secure = new SecureChatServer(30);
    }
}


