import java.io.*;

public class ByteReader {
	private byte cursor;				//Bit pointed to in current byte
	private String buffer;				//Stores most recently read byte
	private DataInputStream data;		//Loads compressed file
	private long filesize;				//Size of loaded file
	private long bytesread;				//Number of bytes that have been read
	private String file;


	/**
	 * Creates a new ByteReader object that processes the passed file. 
	 * If the file cannot be opened, data will be set to NULL.
	 *
	 * @param	file	The file to be opened
	 **/
	public ByteReader(String filename) {
		file = filename;
		filesize = bytesread = cursor = 0;
		buffer = "";
		data = null;

		//Attempt to load file for processing
		try {
			FileInputStream fin = new FileInputStream(file);
			filesize = fin.getChannel().size();
			data = new DataInputStream(new BufferedInputStream(fin));
		} 
		catch (FileNotFoundException fnfe) {
			System.out.println("Unable to load file. Check filename.");
			data = null;
		} 
		catch (IOException ioe) {
			System.out.println("Buggered up while getting file size.");
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
		String in = null;
		try {
			in = Integer.toBinaryString(data.readUnsignedByte());
			bytesread++;

			//Prepend zeroes to fit byte-width
			while (in.length() < 8) {
				in = "0" + in;
			}
		}
		catch (IOException ioe) {
			System.out.println(
				"Attempting to read beyond EOF"
			);
		}

		return in;
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

				//If something broke...
				if (buffer == null) {
					break;
				}
			}

			bits += buffer.substring(cursor++, cursor);
		}
		
		return bits;
	}


	/**
	 * Returns true if all bytes in file have been read.
	 *
	 * The cursor has to be checked because the buffer runs out after the file
	 * read has finished.
	 **/
	public boolean eof() {
		return bytesread == filesize && cursor == 8;
	}


	/**
	 * Closes the loaded input file.
	 **/
	public void close() {
		try {
			data.close();
		} catch (Exception e) {
			System.out.println("File broke while closing");
		}
	}


	/**
	 * "Resets" the file to the beginning. Actually it just closes and reopens it.
	 **/
	public void reset() {
		try {
			data.close();

			FileInputStream fin = new FileInputStream(file);
			data = new DataInputStream(new BufferedInputStream(fin));

			bytesread = cursor = 0;
			buffer = "";
		} catch (Exception e) {
			System.out.println("Something broke while resetting.");
		}
	}
}