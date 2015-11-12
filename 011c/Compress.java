import java.io.*;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Comparator;

public class Compress {
	private ByteReader br;
	private ByteWriter bw;
	private Bits[] byteTable;


	/**
	 * Returns a Compress object, which contains references to the source and
	 * target files.
	 **/
	public Compress(String file) {
		br = new ByteReader(file);
		bw = new ByteWriter(file + ".011");
		byteTable = new Bits[257];

		/* Create Bits objects for all values from 0 to 255 */
		for (int i = 0; i < 256; i++) {
			String asByte = 
				String.format("%8s", 
					Integer.toBinaryString(i)).replace(' ', '0');
			byteTable[i] = new Bits(asByte);
		}

		/* Add null byte to mark EOF */
		byteTable[256] = new Bits("0000");
	}


	/**
	 * Collects and counts all bytes, returns them in ascending order of
	 * frequency.
	 *
	 * @return	A PriorityQueue with all Bits from least to most common
	 **/
	public PriorityQueue<Bits> findFrequencies() {
		/* 
		 * Read and count every byte until EOF 
		 */
		while (!br.eof()) {
			/* Convert byte to integer value */
			int value = Integer.parseInt(br.grabBits(8), 2);

			/* Record byte */
			byteTable[value].addInstance();
		}

		/*
		 * Bits are ordered by frequency, from least to most common
		 */
		PriorityQueue<Bits> queue = 
			new PriorityQueue<>(256, new FreqComparator());

		for (int i = 0; i < 256; i++) {
			if (byteTable[i].getCount() > 0) {
				queue.add(byteTable[i]);
			}
		}

		//Add null byte, which is an exception to the positive count rule
		queue.add(byteTable[256]);

		return queue;
	}


	/**
	 * Constructs a Huffman tree of all read Bits, which places less frequent
	 * nodes further from the root.
	 *
	 * @return	BinaryTree to be used for encoding
	 **/
	public BinaryTree<Bits> generateHuffmanTree(PriorityQueue<Bits> queue) {
		//Process first elements
		BinaryTree<Bits> left = new BinaryTree<>();
		BinaryTree<Bits> right = new BinaryTree<>();
		left.makeRoot(queue.poll());
		right.makeRoot(queue.poll());

		//Add elements as first tree
		LinkedList<BinaryTree<Bits>> huffman = new LinkedList<>();
		huffman.add(joinTree(left, right));

		/* 
		 * Process the rest of the priority queue
		 */
		while (queue.size() > 0) {
			/* Set up left branch */
			left = findProbTree(queue, huffman);

			/* Set up right branch */
			right = findProbTree(queue, huffman);

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

		return huffman.poll();
	}


	/**
	 * Takes two BinaryTrees and joins them to a new dummy node whose
	 * probability reflects the combined probability of both trees.
	 *
	 * @param	left	BinaryTree with lowest frequency
	 * @param	right	BinaryTree with highest frequency
	 * @return	The composite BinaryTree
	 **/
	public static BinaryTree<Bits> joinTree(BinaryTree<Bits> left,
											BinaryTree<Bits> right) {
		BinaryTree<Bits> tree = new BinaryTree<>();

		//Create dummy node and assign probability
		Bits join = new Bits("00");
		join.setCount(left.getData().getCount() 
						+ right.getData().getCount());

		//Join trees and return
		tree.makeRoot(join);
		tree.attachLeft(left);
		tree.attachRight(right);

		return tree;
	}


	/**
	 * Chooses the least-frequently-occurring tree from the "tree" list and
	 * the "leaf" list.
	 *
	 * @return	BinaryTree with the lowest root frequency
	 **/
	public static BinaryTree<Bits> findProbTree(
			PriorityQueue<Bits> s, LinkedList<BinaryTree<Bits>> t) {
		/*
		 * Make sure there is something left to process
		 */
		Bits letter1 = (s.peek() != null) ? s.peek() : null;
		Bits letter2 = (t.peek() != null) ? t.peek().getData() : null;

		if (letter1 == null && letter2 == null) {
			return null;
		}

		/*
		 * Pick the character to return based on frequency. If there are no
		 * trees left in the linkedlist, return a new node.
		 */
		if (letter2 != null) {
			if (letter1 == null || 
					letter1.getCount() > letter2.getCount()) {
				return t.poll();
			}
		}

		//Set leaf as base of new branch
		BinaryTree<Bits> tree = new BinaryTree<>();
		tree.makeRoot(letter1);
		s.poll();

		return tree;
	}


	/**
	 * Writes Huffman encoding table to the target file as a header. This is
	 * used to rebuild the Huffman tree during decompression.
	 *
	 * The header begins with an SOH byte (0x01). This is followed by a byte
	 * representing the number of huffman codes in the header, and the encoding
	 * of the NULL byte.
	 * 
	 * The look-up items are subsequently listed, which link an 8bit bitstring 
	 * with the corresponding Huffman code. Look-up entries have the form:
	 * 			00000000 00000000 0000...0000
	 *  		   Bits   Length  Huffman code
	 *
	 * The length entry specifies the length of the code (the maximum
	 * length is arbitrarily 256 bits).
	 *
	 * The header concludes with an STX byte (0x02).
	 * 
	 * @param	queue	PriorityQueue of Ascii objects
	 **/
	public void writeHeader(PriorityQueue<Bits> queue) {
		//Write SOH byte
		bw.writeByte("00000001");

		/*
		 * Write number of codes.
		 * This is antiquated and should be replaced by a permanently 257 code
		 * system.
		 * At present, the -2 is to allow 256 unique bytes to be expressed in
		 * 8 bits, as well as leaving room for the EOF code.
		 */
		bw.fillByte(Integer.toBinaryString(queue.size() - 2));

		Bits curr = queue.poll();
		while (curr != null) {
			/*
			 * Write underlying byte to file. If the null byte is encountered,
			 * it is written without padding zeroes, to allow for a 257th
			 * value. Not pretty.
			 */
			String bitstring = curr.getBitstring();
			if (bitstring.equals("0000")) {
				bw.writeByte(bitstring);			//Write null byte
			} else {
				bw.fillByte(curr.getBitstring());	//Write normal byte
			}
			System.out.print(curr.getBitstring() + "\t");

			//Convert code length to binary string and send to buffer
			bw.fillByte(Integer.toBinaryString(curr.getCode().length()));
			System.out.print(Integer.toBinaryString(curr.getCode().length()));

			//Send Huffman code to buffer
			bw.writeByte(curr.getCode());
			System.out.println("\t" + curr.getCode());

			//Process next Ascii object
			curr = queue.poll();
		}

		//Write STX byte
		bw.writeByte("00000010");
	}


	/**
	 * Encodes each byte of the source file and writes to the target file.
	 **/
	public void writeBody() {
		//Return to beginning of source file
		br.reset();

		//Track compression ratio
		long codes = 0;
		long numCodes = 0;

		/*
		 * Read every character until EOF, write encoding to target file
		 */
		while (!br.eof()) {
			//Grab next byte
			String curr = br.grabBits(8);

			//Look up Huffman code for byte
			int index = Integer.parseInt(curr, 2);
			String code = byteTable[index].getCode();

			//Write to binary file
			bw.writeByte(code);

			//Update code stuff
			codes += code.length();
			numCodes++;
		}

		/*
		 * Write EOF and clear write buffer
		 */
		String code = byteTable[256].getCode();
		System.out.println("Writing EOF @" + code);
		bw.writeByte(code);
		bw.close();

		System.out.println("Average code length: " + (double)codes / (double)numCodes);
	}


	/**
	 * Comparator to allow sorting by frequency. Less frequent items should
	 * appear "before" more frequent items.
	 **/
	private class FreqComparator implements Comparator<Bits> {
		@Override
		public int compare(Bits arg1, Bits arg2) {
			if (arg1.getCount() < arg2.getCount()) {
				return -1;
			} else if (arg1.getCount() > arg2.getCount()) {
				return 1;
			} else {
				return 0;
			}
		}
	}


	public static void main(String[] args) {
		//Track compression time
		long starttime = System.currentTimeMillis();

		/* 
		 * Ensure source and target files can be opened
		 */
		Compress c = new Compress(args[0]);
		if (c.br.loadFailure()) {
			return;
		}

		//Read the source file, analyze for byte frequency
		PriorityQueue<Bits> allCodes = c.findFrequencies();
		PriorityQueue<Bits> headerQueue = new PriorityQueue<Bits>(allCodes);

		//Find the Huffman codes for the generated tree
		Bits.findEncoding(c.generateHuffmanTree(allCodes));

		/*
		 * If there are less than two codes, something likely went wrong.
		 */
		if (headerQueue.size() < 2) {
			System.out.println("Unable to compress, no codes generated.");
			return;
		}

		/*
		 * Write the header to the target file, and then process the source
		 * file entirely.
		 */
		c.writeHeader(headerQueue);
		c.writeBody();

		long exectime = System.currentTimeMillis() - starttime;
		System.out.println("Execution time: " + exectime + "ms");
	}
}