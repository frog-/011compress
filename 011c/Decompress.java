import java.util.LinkedList;
import java.io.*;

public class Decompress {
	private String proc;		//Bits currently being processed
	private ByteReader data;	//Handles file read operations
	private BST tree;			//Stores huffman codes


	/**
	 * Start a new decompression process.
	 *
	 * @param	file	Name of the file to decompress
	 **/
	public Decompress(String file) {
		proc = "";
		data = new ByteReader(file);
		tree = null;
	}


	/**
	 * Reads the header from the compressed file, and packages the info into
	 * Ascii objects, each within their own single-node BST.
	 *
	 * @return	A LinkedList containing all Ascii BSTs, or NULL on failure
	 **/
	public LinkedList<BST> readHeader() {
		LinkedList<BST> leaves = new LinkedList<>();
	
		int codesProcessed;		//Number of look-up items added to tree

		/*
		 * Basic check: does header start with SOH byte?
		 */
		grabBits(8);
		if (!proc.equals("00000001")) {
			System.out.println("File is corrupted or not compressed");
			return null;
		}
		System.out.println(proc);
		clear();

		/*
		 * Determine number of codes to process.
		 * Offset by +2 to allow for 257 unique codes.
		 */
		grabBits(8);
		int numCodes = Integer.parseInt(proc, 2) + 2;
		System.out.println(proc);
		clear();

		/*
		 * Process look-up codes, reading NULL byte first
		 */
		for (int i = 0; i < numCodes; i++) {
			/* Get bitstring character code */
			if (i == 0) {
				grabBits(4);	//Null byte
			} else {
				grabBits(8);	//Regular byte
			}
			String bitstring = proc;
			System.out.print(bitstring + "\t");
			clear();

			/* Get Huffman code length */
			grabBits(8);
			int length = Integer.parseInt(proc, 2);
			System.out.print(length + "\t");
			clear();

			/* Get Huffman code */
			grabBits(length);
			String code = proc;
			System.out.println(code);
			clear();

			/* Build BST leaf for letter */
			BST leaf = new BST();
			Bits symbol = new Bits(bitstring);
			symbol.setCode(code);
			leaf.update(symbol);
			leaves.add(leaf);
		}

		/*
		 * Does header end with STX byte?
		 */
		grabBits(8);
		if (!proc.equals("00000010")) {
			System.out.println("Header corrupt: end of header without STX");
			return null;
		}
		clear();

		return leaves;
	}


	/**
	 * Takes a list of single-node BSTs and reconstructs the original Huffman
	 * tree.
	 *
	 * @param	leaves	A LinkedList containing single-node BSTs of Ascii objects
	 **/
	public void rebuildTree(LinkedList<BST> leaves) {
		tree = new BST();
		tree.update(new Bits("00"));

		/*
		 * Loop through all nodes in the passed tree.
		 */
		while (leaves.size() > 0) {
			/* Remove a node from the list */
			BST branch = tree;
			Bits curr = leaves.poll().getData();
			int codeLength = curr.getCode().length();

			/* 
			 * Follow the node's code through the tree until the last digit
			 */
			for (int i = 0; i < codeLength - 1; i++) {
				/* Prepare a dummy branch, in case the next node is missing */
				BST newBranch = new BST();
				newBranch.update(new Bits("00"));
				
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
			 * Add new leaf with Ascii object, using the last code digit
			 */
			BST newLeaf = new BST();
			newLeaf.update(curr);
			if (curr.getCode().substring(codeLength-1, codeLength).equals("0")) {
				branch.setLeft(newLeaf);
			} else {
				branch.setRight(newLeaf);
			}
		}
	}


	/**
	 * Reads in bits, one at a time, navigating the rebuilt BST according to
	 * the parity of the bit. When a node is reached that has no child nodes,
	 * its bitstring is taken and written to file.
	 *
	 * @param	name	Name of the file to write to
	 **/
	public void decode(String name) throws Exception {
		ByteWriter bw = new ByteWriter(name + "-decoded.txt");

		while (!data.eof()) {
			BST node = tree;
			String decoded = "";
			clear();

			while (decoded.equals("")) {
				grabBits(1);

				String end = proc.substring(proc.length()-1, proc.length());
				node = (end.equals("0")) ? node.getLeft() : node.getRight();

				if (node.getRight() == null && node.getLeft() == null) {
					decoded = node.getData().getBitstring();
				}
			}
			
			//If EOF byte is found, quit writing to file.
			if(!decoded.equals("0000")) {
				bw.writeByte(decoded);
			} else {
				System.out.println("Found the null byte!");
				break;
			}
		}
		bw.close(true);
	}


	/**
	 * Clears the bitstring currently being processed. Simple helper function,
	 * keeps code cleaner.
	 **/
	public void clear() {
		proc = "";
	}


	/**
	 * Updates proc when grabBits is called. Helper function.
	 **/
	public void grabBits(int n) {
		proc = data.grabBits(n);
	}


	public static void main(String[] args) throws Exception {
		//Load target file for processing.
		Decompress d = new Decompress(args[0]);
		if(d.data.loadFailure()) {
			return;
		}

		//Attempt to read the header and rebuild the original Huffman tree
		d.rebuildTree(d.readHeader());

		//
		d.decode(args[0]);
	}
}