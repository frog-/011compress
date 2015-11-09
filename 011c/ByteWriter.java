import java.io.*;

public class ByteWriter {
	private int buffer;					//Byte currently being written
	private int buffersize;				//Number of bits filled in buffer
	private DataOutputStream data;		//File to write to


	/**
	 * Creates a new ByteWriter object, writing to the supplied file
	 *
	 * Data will be NULL if the target file could not be created. Check with
	 * loadFailure() after instantiating a new ByteWriter.
	 *
	 * @param	file	The target file
	 **/
	public ByteWriter(String file) {
		buffer = 0;
		buffersize = 0;
		data = null;

		try {
			FileOutputStream fout = 
				new FileOutputStream(file);
			data = new DataOutputStream(fout);
		} catch (FileNotFoundException fnfe) {
			System.out.println("Couldn't create output file.");
		}
	}


	/**
	 * Adds the supplied huffman code to the write buffer
	 *
	 * @param	code	The huffman code to write
	 **/
	public void writeByte(String code) {
		if (code == null) {
			System.err.println("Error: Trying to write null string.");
			return;
		}

		/*
		 * Convert code to 0s and 1s
		 */
		char[] bits = code.toCharArray();
		for (int i = 0; i < bits.length; i++) {
			bits[i] = (bits[i] == '1') ? (char)1 : (char)0; 
		}

		for (int i = 0; i < bits.length; i++) {
			//Flush buffer and reset if byte is full
			if (buffersize == 8) {
				try {
					data.write(buffer);
				} catch (IOException ioe) {
					System.err.println("Error writing to file.");
				}
				buffersize = 0;
				buffer = 0;
			}

			//Shift in a new bit
			buffer <<= 1;
			buffer |= (int)bits[i];
			buffersize++;
		}
	}


	/**
	 * Prepends zeroes to code and sends off to be written
	 *
	 * @param	code	The code to pad as a single byte
	 **/
	public void fillByte(String code) {
		if (code != null) {
			while (code.length() < 8) {
				code = "0" + code;
			}
			writeByte(code);
		} else {
			System.err.println("Error: Trying to format null string.");
		}
	}


	/**
	 * Checks if the target file could be created
	 *
	 * @return	True if file could not be loaded
	 **/
	public boolean loadFailure() {
		return data == null;
	}


	/**
	 * Clear the buffer and close the output file
	 **/
	public void close() {
		while (buffersize < 8) {
			buffer <<= 1;
			buffersize++;
		}

		try {
			data.write(buffer);
			data.close();
		} catch (Exception e) {
			System.err.println("File broke while closing.");
		}
	}
}