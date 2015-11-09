public class Bits implements Comparable<Bits> {
	private static long total;		//Number of letters in document
	private String bitstring;		//Value associated with object
	private long count;				//Number of occurrences of letter
	private double prob;			//Frequency of occurence
	private String code;			//Huffman code for character

	public Bits(String bitstring) {
		this.bitstring = bitstring;
		count = 0;
	}

	public static long getInstances() {
		return total;
	}

	public void calculateProbability() {
		setProbability( (double)count / (double)total );
	}

	public void addInstance() {
		count++;
		total++;
	}

	public long getCount() {
		return count;
	}

	public double getProbability() {
		return prob;
	}

	public void setProbability(double prob) {
		this.prob = prob;
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

	public int compareTo(Bits other) {
		return bitstring.compareTo(other.getBitstring());
	}

	public boolean equals(Bits other) {
		return bitstring.equals(other.getBitstring());
	}

	public String toString() {
		return bitstring + "\t" + code + "\t\t\t" + String.format("%.8f", prob);
	}


	public static void findEncoding(BinaryTree<Bits> t, String prefix)	{
		if (t.getLeft() == null && t.getRight() == null) {
			t.getData().setCode(prefix);
		} else {
			findEncoding(t.getLeft(), prefix + "0");
			findEncoding(t.getRight(), prefix + "1");
		}
	}

	public static void findEncoding(BinaryTree<Bits> t) {
		findEncoding(t, "");
	}
}