package de.kniffel.server.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * This class helps with serializing objects to a string to send through a socket as UTF. It also deserialises strings to objects again. This class is needed
 * for the client and the server
 * Taken from https://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
 * 
 * @author KBeck
 *
 */
public class Serializer {

	
	  /**
	   * 
	   * @param s String to deserialize 
	   * @return deserialized object
	   * @throws IOException
	   * @throws ClassNotFoundException
	   */
	   public static Object fromString( String s ) throws IOException ,
	                                                       ClassNotFoundException {
	        byte [] data = Base64.getDecoder().decode( s );
	        ObjectInputStream ois = new ObjectInputStream( 
	                                        new ByteArrayInputStream(  data ) );
	        Object o  = ois.readObject();
	        ois.close();
	        return o;
	   }
	   
	   /**
	    * 
	    * @param o Object to serialize to string
	    * @return serialized string
	    * @throws IOException
	    */
	    public static String toString( Object o ) throws IOException {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectOutputStream oos = new ObjectOutputStream( baos );
	        oos.writeObject( o );
	        oos.close();
	        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	    }
	    
	    /**
	     * 
	     * @param s to deserialize to byte[]
	     * @return deserialized byte[]
	     * @throws IOException
	     * @throws ClassNotFoundException
	     */
	    public static byte[] fromStringToByteArr(String s) throws IOException, ClassNotFoundException{
	    	byte[] data = Base64.getDecoder().decode(s);
	    	return data;
	    }
	    
		/**
		 * 
		 * @param data to serialize
		 * @return serialized byte[] as String
		 * @throws IOException
		 */
	    public static String toString( byte[] data ) throws IOException {
	        return Base64.getEncoder().encodeToString(data); 
	    }
}
