import java.io.*;
import java.util.PriorityQueue;
import java.util.LinkedList;

public class Compress {
	private ByteReader br;
	private ByteWriter bw;
	private BST bytetree;

	public static void main(String[] args) {
		long starttime = System.currentTimeMillis();
		Compress c = new Compress(args[0]);
		if (c.br.loadFailure()) {
			return;
		}

		PriorityQueue<Bits> allCodes = c.findFrequencies();
		PriorityQueue<Bits> headerQueue = new PriorityQueue<Bits>(allCodes);
		Bits.findEncoding(c.generateHuffmanTree(allCodes));

		if (headerQueue.size() < 2) {
			System.out.println("Unable to compress, no codes generated.");
			return;
		}

		c.writeHeader(headerQueue);
		c.writeBody();
		long endtime = System.currentTimeMillis();
		long fulltime = endtime - starttime;
		System.out.println("Execution time: " + fulltime + "ms");
	}


	public Compress(String file) {
		br = new ByteReader(file);
		bw = new ByteWriter(file + "-gerbils");
		bytetree = new BST();
	}

	/**
	 * Collects and counts all bytes 
	 **/
	public PriorityQueue<Bits> findFrequencies() {
		/* Read every byte until EOF */
		while (!br.eof()) {
			String bits = br.grabBits(8);
			bytetree.update(new Bits(bits));
		}

		//Add EOF character
		bytetree.finalize();

		if (bytetree.isEmpty()) {
			System.out.println("File appears to have been empty...");
		}

		//Done with file for now
		br.close();

		/*
		 * Probability tree is constructed. Characters in the BST are sorted
		 * by frequency, and then joined together into a tree, with lowest
		 * frequency terms most distant from the root node
		 */
		return BST.queueByFrequency(bytetree);
	}


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
		join.setProbability(left.getData().getProbability() 
						+ right.getData().getProbability());

		//Join trees and return
		tree.makeRoot(join);
		tree.attachLeft(left);
		tree.attachRight(right);

		return tree;
	}


	public static BinaryTree<Bits> findProbTree(PriorityQueue<Bits> s, 
											LinkedList<BinaryTree<Bits>> t) {
		BinaryTree<Bits> tree = new BinaryTree<>();

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
					letter1.getProbability() > letter2.getProbability()) {
				return t.poll();
			}
		}

		tree.makeRoot(letter1);
		s.poll();
		return tree;
	}


	/**
	 * Writes Huffman encoding table to the target file as a header. This is
	 * used to rebuild the Huffman tree during decompression.
	 *
	 * The header begins with an SOH byte (0x01). This is followed by a byte
	 * representing the number of huffman codes in the header. The look-up 
	 * items are subsequently listed, which link an 8bit bitstring with
	 * the corresponding Huffman code. Look-up entries have the form:
	 *
	 * 00000000 00000000 0000...0000
	 *   Bits   Length  Huffman code
	 *
	 * The length entry specifies the length of the code (the maximum
	 * length is arbitrarily 256 bits).
	 *
	 * The header concludes with an STX byte (0x02).
	 * 
	 * @param	queue	PriorityQueue of Ascii objects
	 * @param	out	DataOutputStream linked to target file
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
				bw.writeByte(bitstring);	//Write null byte
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


	public void writeBody() {
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
			Bits key = bytetree.find(new Bits(curr)).getData();
			String code = key.getCode();

			//Write to binary file
			bw.writeByte(code);

			//Update code stuff
			codes += code.length();
			numCodes++;
		}

		/*
		 * Write EOF and clear write buffer
		 */
		Bits key = bytetree.find(new Bits("0000")).getData();
		String code = key.getCode();
		System.out.println("Writing EOF @" + code);
		bw.writeByte(code);
		bw.close(true);

		System.out.println("Average code length: " + (double)codes / (double)numCodes);
	}
}