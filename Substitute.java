import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Substitute implements SymCipher{

	private byte [] key;
	private byte [] code;
	
	/************************************************
	* 	Constructors implemented below        		*
	*   - Secure Chat Client calls parameterless	*
	*   - Secure Chat Server calls parameters		*
	************************************************/
	
	public Substitute(){
		
		// creates random 256 byte key (permutation of 256 byte values)
		key = new byte[256];
		new Random().nextBytes(key);
		
	}
	
	public Substitute(byte [] b){
		
		key = b;
		
	}
	
	
	/********************************************
	* 	Methods from the SymCipher interface    *
	********************************************/
	
	public byte[] getKey() {

		return key;
		
	}

	public byte[] encode(String S) {

			System.out.println("[ -- Encoding -- ]");
		
		// creates an array of bytes from the string using a
		// standard charset of most text editors
		try {
			
			code = S.getBytes("UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
			
			// -- display original string -- //
			System.out.println("String         -> " + S);
			
			// -- display original byte values -- //
			System.out.print("Array of Bytes -> ");
			for(byte i : code){	System.out.print(i + " "); }
			System.out.println();
			//----//
			
			
			// --   encoding segment and encoding display   -- //
			for(int i=0; i<code.length; i++){
				
					//System.out.println("Current Index: " + i);
					//System.out.println("Current Value: " + code[i]);
				code[i] = (byte)((code[i] + key[i]) % 256);
					//System.out.println("New Value:     " + Math.abs(key[i]));
			
			}
			//----//
			
			// -- dislpay encoded values -- //
			System.out.print("Encoded        -> ");
			for(byte i : code){ System.out.print(i + " "); }	
			System.out.println();
			//----//
			
			
				
		return code;
		
	}

	public String decode(byte[] bytes) {

		System.out.println("[ -- Decoding -- ]");
		
		// -- display bytes to decode -- //
		System.out.print("Array of Bytes -> ");
		for(byte i : bytes){ System.out.print(i + " "); }
		System.out.println();
		//----//
			
		// --   decoding segment and decoded byte display   -- //
		for(int i=0; i<bytes.length; i++){
				
				//System.out.println("Current Index: " + i);
				//System.out.println("Current Value: " + Math.abs(key[i]));
			bytes[i] = (byte)((bytes[i] - key[i]) % 256);	
				//System.out.println("New Value:     " + bytes[i]);
			
		}
		//----//	
		
		// -- dislpay encoded values -- //
		System.out.print("Decoded        -> ");
		for(byte i : bytes){ System.out.print(i + " "); }	
		System.out.println();
		//----//
		
		// --   converts from bytes to string using UTF-8 standard   -- //
		String str = new String(bytes, StandardCharsets.UTF_8);
		
		// -- display original string -- //
		System.out.println("String         -> " + str);
		
		return str;
	}

}
