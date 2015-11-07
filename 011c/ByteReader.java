import java.io.*;

public class ByteReader {
	private int cursor;					//Bit pointed to in current byte
	private String buffer;				//Stores most recently read byte
	private DataInputStream data;		//Loads compressed file


	/**
	 * Creates a new ByteReader object that processes the passed file. If the
	 * file cannot be opened, data will be set to NULL.
	 *
	 * @param	file	The file to be opened
	 **/
	public ByteReader(String file) {
		cursor = 0;
		buffer = "";
		data = null;

		//Attempt to load file for processing
		try {
			FileInputStream fin = new FileInputStream(file);
			data = new DataInputStream(fin);
		} catch (FileNotFoundException fnfe) {
			System.out.println("Unable to load file. Check filename.");
			data = null;
		}
	}


	/**
	 * Check if the file was successfully loaded during initialization.
	 *
	 * @return	Returns true if file was successfully loaded
	 **/
	public boolean loadFailure() {
		return data == null;
	}


	/**
	 * Reads the next byte from the encoded file, ensuring it has the proper
	 * width. Bytes are read without leading zeroes, however all zeroes are
	 * significant in this encoding, so an appropriate number are prepended.
	 *
	 * If a failure occurs when reading a byte, either the header specified
	 * the wrong length for the code, or the header specified wrong codes.
	 *
	 * @return	
	 **/
	private String readByte() {
		try {
			String in = Integer.toBinaryString(data.readUnsignedByte());

			//Prepend zeroes to fit byte-width
			while (in.length() < 8) {
				in = "0" + in;
			}

			return in;
		}
		catch (IOException ioe) {
			System.out.println(
				"Data cannot be read, possibly corrupted header."
			);

			return null;
		}
	}


	/**
	 * Retrieves n bits from the buffer, reading further into the file as
	 * necessary.
	 *
	 * @return	N-length bitstring
	 **/
	public String grabBits(int n) {
		String bits = "";

		for (int i = 0; i < n; i++) {
			/*
			 * If the byte has been completely read, fetch a new one, and start
			 * reading from the MSB
			 */
			if (cursor == 0 || cursor == 8) {
				buffer = readByte();
				cursor = 0;
			}

			bits += buffer.substring(cursor++, cursor);
		}
		
		return bits;
	}
}