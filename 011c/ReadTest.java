public class ReadTest {
	public static void main(String[] args) {
		ByteReader br = new ByteReader("test.txt");

		for (;;) {
			System.out.println(br.grabBits(4));
		}
	}
}