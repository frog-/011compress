public class ReadTest {
	public static void main(String[] args) {
		ByteReader br = new ByteReader("test.txt");

		for (; !br.eof() ;) {
			String a = br.grabBits(4);
			System.out.print(a + " ");
			int foo = Integer.parseInt(a, 2);
			System.out.println(foo);
		}
	}
}