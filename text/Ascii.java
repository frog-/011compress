public class Ascii implements Comparable<Ascii> {
	private static long total;		//Number of letters in document
	private char letter;			//Letter associated with object
	private long count;				//Number of occurrences of letter
	private double prob;			//Frequency of occurence
	private String code;			//Huffman code for character

	public Ascii(char letter) {
		this.letter = letter;
		count = 0;
	}

	public static long getTotalChars() {
		return total;
	}

	public void addInstance() {
		count++;
		total++;
		prob = (double)count / (double)total;
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

	public char getLetter() {
		return letter;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public int compareTo(Ascii other) {
		if (this.letter < other.getLetter()) {
			return -1;
		} else if (this.letter > other.getLetter()) {
			return 1;
		} else {
			return 0;
		}
	}

	public boolean equals(Ascii other) {
		return this.letter == other.letter;
	}

	public String toString() {
		if (letter == '\0') {
			System.out.print("Null");
		}

		if (letter == '\n') {
			System.out.print("Newline");
		}
		return letter + "\t" + prob + "\t" + code;
	}
}