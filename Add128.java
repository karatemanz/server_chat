import java.io.UnsupportedEncodingException;
import java.nio.charset.*;
import java.util.*;


public class Add128 implements SymCipher{

	private byte [] key;
	private byte [] code;
	
	/************************************************
	* 	Constructors implemented below          	*
	*   - Secure Chat Client calls parameterless	*
	*   - Secure Chat Server calls parameters		*
	************************************************/
	
	public Add128(){
		
		// create random 128 byte key
		key = new byte[128];
		new Random().nextBytes(key);
		
	}
	
	public Add128(byte [] b){
		
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
		
		try {
			
			// -- convert the string to UTF encoded string of bytes -- //
			code = S.getBytes("UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
		
		
			// -- display original string -- //
			System.out.println("String         -> " + S);
			
			// -- display original byte value of string -- //
			System.out.print("Array of Bytes -> ");
			for(byte i : code){ System.out.print(i + " "); }	
			System.out.println();	
			//----//	
			
			// -- encoding segment -- //
			for(int i=0; i<code.length; i++){
					
					//System.out.println("Current Index: " + i);
					//System.out.println("Current Value: " + code[i]);
				code[i] = (byte) (code[i] + key[i]);
					//System.out.println("New Value:     " + code[i]);
				
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

		// -- decode segment -- //
		for(int i=0; i<bytes.length; i++){
			
				//System.out.println("Current Index: " + i);
				//System.out.println("Current Value: " + bytes[i]);
			bytes[i] = (byte) (bytes[i] - key[i]); 
				//System.out.println("Location Value: " + bytes[i]);
			
		}
		//----//	
		
		// -- display decoded bytes -- //
		System.out.print("Decoded        -> ");
		for(byte i : bytes){ System.out.print(i + " "); }
		System.out.println();
		//----//
		
		// -- convert bytes to original string with UTF char-set -- //
		String str = new String(bytes, StandardCharsets.UTF_8);
		
		// -- display original string -- //
		System.out.println("String         -> " + str);
		
		return str;
		
	}

}
