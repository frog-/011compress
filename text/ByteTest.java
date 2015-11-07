import java.io.*;

public class ByteTest {
	public static void main(String[] args) throws IOException {
		FileOutputStream fout = new FileOutputStream("onebyte.dat");
		DataOutputStream printer = new DataOutputStream(fout);

		char n = 0;
		System.out.println(n);

		n <<= 1; n |= 1;
		n <<= 1;
		n <<= 1; n |= 1;
		n <<= 1;
		n <<= 1; n |= 1;
		n <<= 1;
		n <<= 1; n |= 1;
		n <<= 1;
		n <<= 1; n |= 1; //Drop here
		n <<= 1;
		n <<= 1; n |= 1;
		n <<= 1;
		n <<= 1; n |= 1;
		n <<= 1;
		n <<= 1; n |= 1;
		n <<= 1;
		System.out.println(n);
		printer.writeInt(n);
		printer.close();

		FileInputStream fin = new FileInputStream("onebyte.dat");
		DataInputStream reader = new DataInputStream(fin);

		for (int j = 0; j < 4; j++) {
			byte m = reader.readByte();
			for (int i = 0; i < 8; i++) {
				System.out.print((m >> 7) & 1);
				m <<= 1;
			}
			System.out.println();
		}
	}
}