public class BuilderTest {
	public static void main(String[] args) {
		long starttime = System.currentTimeMillis();
		for (long i = 0; i < 50000000; i++) {
			String s = "";
			for (int j = 0; j < 8; j++) {
				s += "0";
			}
		}
		long totalTime = System.currentTimeMillis() - starttime;
		System.out.println("Execution time for concatenation: " + totalTime);

		starttime = System.currentTimeMillis();
		for (long i = 0; i < 50000000; i++) {
			StringBuilder s = new StringBuilder(8);
			for (int j = 0; j < 8; j++) {
				s.append("0");
			}
		}
		totalTime = System.currentTimeMillis() - starttime;
		System.out.println("Execution time for StringBuilder: " + totalTime);
	}
}