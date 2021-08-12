package de.kniffel.client.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * This class helps with serializing objects to a string to send through a socket as UTF. It also deserialises strings to objects again. This class is needed
 * for the client and the server
 * @author KBeck
 *
 */
public class Serializer {

	
	  /** Read the object from Base64 string. */
	   public static Object fromString( String s ) throws IOException ,
	                                                       ClassNotFoundException {
	        byte [] data = Base64.getDecoder().decode( s );
	        ObjectInputStream ois = new ObjectInputStream( 
	                                        new ByteArrayInputStream(  data ) );
	        Object o  = ois.readObject();
	        ois.close();
	        return o;
	   }
	   
	   /** Write the object to a Base64 string. */
	    public static String toString( Object o ) throws IOException {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream( baos );
	        oos.writeObject( o );
	        oos.close();
	        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	    }
	    
	    public static byte[] fromStringToByteArr(String s) throws IOException, ClassNotFoundException{
	    	byte[] data = Base64.getDecoder().decode(s);
	    	return data;
	    }
	    
		/** Write the object to a Base64 string. */
	    public static String toString( byte[] data ) throws IOException {
	        return Base64.getEncoder().encodeToString(data); 
	    }
}
