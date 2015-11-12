public class Bits implements Comparable<Bits> {
	private String bitstring;		//Byte associated with object
	private long count;				//Number of occurrences of byte
	private String code;			//Huffman code for byte


	/**
	 * Returns a new Bits object with arg as value and no instances.
	 *
	 * @param	bitstring	Sequence of bits to later encode
	 **/
	public Bits(String bitstring) {
		this.bitstring = bitstring;
		count = 0;
	}


	/**
	 * Increments the count of the calling byte, and document total, by one.
	 **/
	public void addInstance() {
		count++;
	}


	/**
	 * Sets the count of the calling object.
	 *
	 * This _should not_ be used manually for true Bits objects. Its purpose is
	 * to allow dummy nodes to have probabilities during construction of the
	 * Huffman tree. Generally, the argument passed should be the sum of the 
	 * two nodes being added.
	 *
	 * @param	count	The combined counts to set for the dummy node
	 **/
	public void setCount(long count) {
		this.count = count;
	}


	/**
	 * Generates Huffman codes for all leaves in the passed BinaryTree.
	 *
	 * @param	tree	Root node of the tree to process
	 **/
	public static void findEncoding(BinaryTree<Bits> tree) {
		findEncoding(tree, "");
	}


	/**
	 * Recursive function for encoding, not to be called manually.
	 **/
	private static void findEncoding(BinaryTree<Bits> tree, String prefix) {
		if (tree.getLeft() == null && tree.getRight() == null) {
			tree.getData().setCode(prefix);
		} else {
			findEncoding(tree.getLeft(), prefix + "0");
			findEncoding(tree.getRight(), prefix + "1");
		}
	}


	/**
	 * Utility methods
	 **/
	@Override
	public int compareTo(Bits other) {
		return bitstring.compareTo(other.getBitstring());
	}

	public boolean equals(Bits other) {
		return bitstring.equals(other.getBitstring());
	}

	public String toString() {
		return bitstring + "\t\t" + code;
	}


	/**
	 * Accessors and Mutators
	 **/
	public long getCount() {
		return count;
	}

	public String getBitstring() {
		return bitstring;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}