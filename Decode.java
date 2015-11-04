import java.io.*;
import java.util.LinkedList;

public class Decode {
	private static int cursor;		//How many bits into the buffer we are
	private static String proc;		//Bits currently being processed
	private static String buffer;	//Last byte read from file

	public static void main(String[] args) throws Exception {
		FileInputStream fin = new FileInputStream(args[0]);
		DataInputStream data = new DataInputStream(fin);
		proc = "";
		cursor = 0;
		LinkedList<AsciiBST> leaves = readHeader(data);
		AsciiBST tree = rebuildTree(leaves);
		decompress(data, tree, args[0]);
	}

	/**
	 * Reads in bits, one at a time, navigating the rebuilt BST according to
	 * the parity of the bit. When a node is reached that has no child nodes,
	 * its ASCII code is taken and written to file.
	 **/
	public static void decompress(DataInputStream data, AsciiBST tree,
												String name) throws Exception {
		FileWriter fout = new FileWriter(name + "-decompressed.txt");
		BufferedWriter writer = new BufferedWriter(fout);

		//Set to true when EOF character is found
		boolean eof = false;

		while (!eof) {
			AsciiBST node = tree;
			char decoded = '\b';
			clear();

			while (decoded == '\b') {
				grabBits(1, data);

				String end = proc.substring(proc.length()-1, proc.length());
				node = (end.equals("0")) ? node.getLeft() : node.getRight();

				if (node.getRight() == null && node.getLeft() == null) {
					decoded = node.getData().getLetter();
				}
			}

			if (decoded == '\0') {
				eof = false;
				break;
			} else {
				writer.write(decoded);
			}
		}

		writer.close();
	}


	public static AsciiBST rebuildTree(LinkedList<AsciiBST> leaves) {
		AsciiBST tree = new AsciiBST();
		tree.update(new Ascii('0'));

		while (leaves.size() > 0) {
			AsciiBST branch = tree;
			Ascii curr = leaves.poll().getData();
			int codeLength = curr.getCode().length();

			for (int i = 0; i < codeLength - 1; i++) {
				/*
				 * Prepare a dummy branch, in case the next node is missing
				 */
				AsciiBST newBranch = new AsciiBST();
				newBranch.update(new Ascii('0'));
				

				/*
				 * Move left or right, based on a zero or one in the Huff. code
				 */
				if (curr.getCode().substring(i, i+1).equals("0")) {
					if (branch.getLeft() == null) {
						branch.setLeft(newBranch);
					}
					branch = branch.getLeft();
				} else {
					if (branch.getRight() == null) {
						branch.setRight(newBranch);
					}
					branch = branch.getRight();
				}
			}

			/*
			 * Add new leaf with current Ascii object
			 */
			AsciiBST newLeaf = new AsciiBST();
			newLeaf.update(curr);
			if (curr.getCode().substring(codeLength-1, codeLength).equals("0")) {
				branch.setLeft(newLeaf);
			} else {
				branch.setRight(newLeaf);
			}
		}

		return tree;
	}


	public static LinkedList<AsciiBST> readHeader(DataInputStream data) throws Exception {
		LinkedList<AsciiBST> leaves = new LinkedList<>();
	
		int codesProcessed;		//Number of look-up items added to tree

		/*
		 * Basic check: does header start with SOH byte?
		 */
		grabBits(8, data);
		if (!proc.equals("00000001")) {
			System.out.println("File is corrupted or not compressed");
			return null;
		}
		clear();

		/*
		 * Determine number of codes to process
		 */
		grabBits(8, data);
		int numCodes = Integer.parseInt(proc, 2);
		clear();

		/*
		 * Process look-up codes.
		 */
		for (int i = 0; i < numCodes; i++) {
			/* Get ASCII character code */
			grabBits(8, data);
			char charcode = (char)Integer.parseInt(proc, 2);
			clear();

			/* Get Huffman code length */
			grabBits(8, data);
			int length = Integer.parseInt(proc, 2);
			clear();

			/* Get Huffman code */
			grabBits(length, data);
			String code = proc;
			clear();

			/* Build BST leaf for letter */
			AsciiBST leaf = new AsciiBST();
			Ascii letter = new Ascii(charcode);
			letter.setCode(code);
			leaf.update(letter);
			leaves.add(leaf);
		}

		/*
		 * Does header end with STX byte?
		 */
		grabBits(8, data);
		if (!proc.equals("00000010")) {
			System.out.println("Header corrupt");
		}
		clear();

		return leaves;
	}


	/**
	 * Reads the next byte from the encoded file, ensuring it has the proper
	 * width. Bytes are read without leading zeroes, however all zeroes are
	 * significant in this encoding, so an appropriate number are prepended.
	 *
	 * If a failure occurs when reading a byte, either the header specified
	 * the wrong length for the code, or the header specified wrong codes.
	 **/
	public static String readByte(DataInputStream data) {
		try {
			/*
			 * Prepend zeroes to read byte
			 */
			String in = Integer.toBinaryString(data.readUnsignedByte());
			while (in.length() < 8) {
				in = "0" + in;
			}
			return in;
		} catch (IOException ioe) {
			System.out.println("Corrupted header.");
			return null;
		}
	}


	/**
	 * Retrieves n bits from the buffer, adding them to the currently processed
	 * bitstring.
	 **/
	public static void grabBits(int n, DataInputStream data) {
		for (int i = 0; i < n; i++) {
			/*
			 * If the byte has been completely read, fetch a new one, and start
			 * reading from the MSB
			 */
			if (cursor == 8 || cursor == 0) {
				buffer = readByte(data);
				cursor = 0;
			}

			/*
			 * Append the bit currently pointed to in the buffer.
			 */
			proc += buffer.substring(cursor++, cursor);
		}
	}

	public static void clear() {
		proc = "";
	}
}