public class Bits implements Comparable<Bits> {
	private static long total;		//Number of letters in document
	private String bitstring;		//Byte associated with object
	private long count;				//Number of occurrences of byte
	private double prob;			//Frequency of occurence
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
	 * Determines the frequency of the byte relative to total number of bytes.
	 *
	 * This should be called only after the source document has been parsed.
	 **/
	public void calculateProbability() {
		setProbability( (double)count / (double)total );
	}


	/**
	 * Increments the count of the calling byte, and document total, by one.
	 **/
	public void addInstance() {
		count++;
		total++;
	}


	/**
	 * Sets the probability of the calling object.
	 *
	 * This _should not_ be used manually for true Bits objects. Its purpose is
	 * to allow dummy nodes to have probabilities during construction of the
	 * Huffman tree. Use calculateProbability() to determine the frequency of a
	 * true node.
	 *
	 * @param	prob	The probability to set
	 **/
	public void setProbability(double prob) {
		this.prob = prob;
	}


	/**
	 * Sets the count to zero, forcing this node to the top of the header.
	 **/
	public void setNullByte() {
		count = 0;
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
		return bitstring + "\t" + code + "\t\t\t" + String.format("%.6f", prob);
	}


	/**
	 * Accessors and Mutators
	 **/
	public static long getInstances() {
		return total;
	}

	public long getCount() {
		return count;
	}

	public double getProbability() {
		return prob;
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