import java.io.*;
import java.util.PriorityQueue;
import java.util.LinkedList;

public class Encode {
	private static int bitstring;
	private static int bitcount;

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Give filename as command-line arg.");
			return;
		}

		/*
		 * All characters are read into a binary search tree of Ascii objects,
		 * which tracks the number of occurrences and total letters in the text
		 * file.
		 * This tree is used to generate the PriorityQueue as well as for
		 * look-ups during the encoding process. 
		 */
		AsciiBST letterTree = new AsciiBST();
		try {
			FileReader fin = new FileReader(args[0]);
			BufferedReader reader = new BufferedReader(fin);

			//Read every character until EOF
			int curr = reader.read();
			while (curr != -1) {
				letterTree.update(new Ascii((char)curr));
				curr = reader.read();
			}

			//Add EOF character
			letterTree.finalize();

			reader.close();
		} catch (Exception e) {
			System.out.println("Error while reading file.");
			return;
		}

		if (letterTree.isEmpty()) {
			System.out.println("File appears to have been empty...");
			return;
		}

		/*
		 * Probability tree is constructed. Characters in the BST are sorted
		 * by frequency, and then joined together into a tree, with lowest
		 * frequency terms most distant from the root node
		 */
		PriorityQueue<Ascii> freq = AsciiBST.queueByFrequency(letterTree);
		PriorityQueue<Ascii> headerQueue = new PriorityQueue<>(freq);

		//Create huffman tree
		LinkedList<BinaryTree<Ascii>> huffman = new LinkedList<>();

		//Process first elements
		BinaryTree<Ascii> left = new BinaryTree<>();
		BinaryTree<Ascii> right = new BinaryTree<>();
		left.makeRoot(freq.poll());
		right.makeRoot(freq.poll());

		//Add elements as first tree
		huffman.add(joinTree(left, right));

		/* 
		 * Process the rest of the priority queue
		 */
		while (freq.size() > 0) {
			/* Set up left branch */
			left = findProbTree(freq, huffman);

			/* Set up right branch */
			right = findProbTree(freq, huffman);

			/* Join branches into new tree */
			if (left != null && right != null) {
				huffman.add(joinTree(left, right));
			} else if (left != null && right == null) {
				huffman.add(left);
				break;
			}
		}

		/*
		 * Join the remainder of branches
		 */
		while (huffman.size() > 1) {
			left = huffman.poll();
			right = huffman.poll();
			huffman.add(joinTree(left, right));
		}

		/*
		 * Generate Huffman codes from finalized probability tree.
		 * Codes are stored in Ascii objects, which are accessed from
		 * letterTree.
		 */
		findEncoding(huffman.poll());

		//Track compression ratio
		long codes = 0;
		long numCodes = 0;

		/*
		 * Encode the file.
		 *
		 * Re-open the original file, and read it letter-by-letter. For each,
		 * find the Huffman code for the character and pass it to the write
		 * buffer.
		 */
		try {
			/*
			 * Open source file, encoded text file, and target binary file
			 */
			FileReader fin = new FileReader(args[0]);
			BufferedReader reader = new BufferedReader(fin);

			FileOutputStream fenc = new FileOutputStream(args[0] + "-encoded.dat");
			DataOutputStream encoder = new DataOutputStream(fenc);

			/*
			 * Write header.
			 */
			writeHeader(headerQueue, encoder);

			/*
			 * Read every character until EOF, write encoding to target file
			 */
			int curr = reader.read();
			while (curr != -1) {
				//Look up Huffman code for character
				Ascii key = letterTree.find(new Ascii((char)curr)).getData();
				String code = key.getCode();

				//Write to binary file
				writeByte(code, encoder);

				//Grab next character
				curr = reader.read();

				//Update code stuff
				codes += code.length();
				numCodes++;
			}

			/*
			 * Write EOF and clear write buffer
			 */
			Ascii key = letterTree.find(new Ascii('\0')).getData();
			String code = key.getCode();
			writeByte(code, encoder);
			writeByte(null, encoder);


			/*
			 * Close all files
			 */
			reader.close();
			encoder.close();
		} catch (Exception e) {
			return;
		}

		System.out.println("Average code length: " + (double)codes/(double)numCodes);
	}


	public static BinaryTree<Ascii> findProbTree(PriorityQueue<Ascii> s, 
											LinkedList<BinaryTree<Ascii>> t) {
		BinaryTree<Ascii> tree = new BinaryTree<>();

		/*
		 * Make sure there is something left to process
		 */
		Ascii letter1 = (s.peek() != null) ? s.peek() : null;
		Ascii letter2 = (t.peek() != null) ? t.peek().getData() : null;

		if (letter1 == null && letter2 == null) {
			return null;
		}

		/*
		 * Pick the character to return based on frequency. If there are no
		 * trees left in the linkedlist, return a new node.
		 */
		if (letter2 != null) {
			if (letter1 == null || 
					letter1.getProbability() > letter2.getProbability()) {
				return t.poll();
			}
		}

		tree.makeRoot(letter1);
		s.poll();
		return tree;
	}


	/**
	 * Takes two BinaryTrees and joins them to a new dummy node whose
	 * probability reflects the combined probability of both trees.
	 *
	 * @param	left	BinaryTree with lowest frequency
	 * @param	right	BinaryTree with highest frequency
	 * @return	The composite BinaryTree
	 **/
	public static BinaryTree<Ascii> joinTree(BinaryTree<Ascii> left,
											BinaryTree<Ascii> right) {
		BinaryTree<Ascii> tree = new BinaryTree<>();

		//Create dummy node and assign probability
		Ascii join = new Ascii('0');
		join.setProbability(left.getData().getProbability() 
						+ right.getData().getProbability());

		//Join trees and return
		tree.makeRoot(join);
		tree.attachLeft(left);
		tree.attachRight(right);

		return tree;
	}


	/**
	 * Parses strings into single byte packets and writes to a target file. A
	 * buffer contains any unwritten bits until the eighth bit is supplied, 
	 * at which point the buffer is written to file and cleared.
	 *
	 * Passing a null string writes out any leftover bits in the buffer, right-
	 * padded with zeros. Don't pass a null string if you don't want this!
	 *
	 * @param	code	String containing binary data
	 * @param	encoder	DataOutputStream reference to target file
	 **/
	public static void writeByte(String code, DataOutputStream encoder) throws Exception {
		//Clear buffer if null is received
		if (code == null) {
			for (int i = bitcount; i < 8; i++) {
				bitstring <<= 1;
			}

			encoder.write(bitstring);
			return;
		}

		/*
		 * Null not received; continue filling buffer
		 */
		char[] bits = code.toCharArray();

		//Convert bits to Ascii 0s and 1s
		for (int i = 0; i < bits.length; i++) {
			bits[i] = (bits[i] == '1') ? (char)1 : (char)0; 
		}

		for (int i = 0; i < bits.length; i++) {
			if (bitcount == 8) {
				encoder.write(bitstring);
				bitcount = 0;
				bitstring = 0;
			}

			bitstring <<= 1;
			bitstring |= (int)bits[i];
			bitcount++;
		}
	}

	/**
	 * Writes Huffman encoding table to the target file as a header. This is
	 * used to rebuild the Huffman tree during decompression.
	 *
	 * The header begins with an SOH byte (0x01). This is followed by a byte
	 * representing the number of huffman codes in the header. The look-up 
	 * items are subsequently listed, which link an ASCII code with
	 * the corresponding Huffman code. Look-up entries have the form:
	 *
	 * 00000000 00000000 0000...0000
	 *   ASCII   Length  Huffman code
	 *
	 * The length entry specifies the length of the code (the maximum
	 * length is arbitrarily 256 bits).
	 *
	 * The header concludes with an STX byte (0x02).
	 * 
	 * @param	queue	PriorityQueue of Ascii objects
	 * @param	out	DataOutputStream linked to target file
	 **/
	public static void writeHeader(PriorityQueue<Ascii> queue, 
									DataOutputStream out) throws Exception {
		//Write SOH byte
		String writebits = "00000001";
		writeByte(writebits, out);

		//Write number of codes
		writebits = formatWidth(queue.size());
		writeByte(writebits, out);

		Ascii curr = queue.poll();
		while (curr != null)
		{
			//Convert Ascii to binary string and send to buffer
			writebits = formatWidth(curr.getLetter());
			writeByte(writebits, out);

			//Convert code length to binary string and send to buffer
			writebits = formatWidth(curr.getCode().length());
			writeByte(writebits, out);

			//Send Huffman code to buffer
			writeByte(curr.getCode(), out);

			//Process next Ascii object
			curr = queue.poll();
		}

		//Write STX byte
		writebits = "00000010";
		writeByte(writebits, out);
	}


	public static String formatWidth(int n) {
		String code = Integer.toBinaryString(n);
		while (code.length() < 8) {
			code = "0" + code;
		}

		return code;
	}


	public static void findEncoding(BinaryTree<Ascii> t, String prefix)	{
		if (t.getLeft() == null && t.getRight() == null) {
			t.getData().setCode(prefix);
		} else {
			findEncoding(t.getLeft(), prefix + "0");
			findEncoding(t.getRight(), prefix + "1");
		}
	}

	public static void findEncoding(BinaryTree<Ascii> t) {
		findEncoding(t, "");
	}

}