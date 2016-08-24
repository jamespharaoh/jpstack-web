package wbs.platform.php;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public
class PhpUnserializer {

	private static
	int readInt (
			CountingInputStream in,
			char termChar)
		throws IOException {

		// NB this method works with negatives internally as the range is higher
		int c;
		int i = 0;
		boolean first = true;
		boolean negative = false;
		while ((c = in.read()) != termChar) {
			if (c == -1)
				throw new PhpUnserializeException("Unexpected end of input");
			if (c == '-' && first) {
				negative = true;
			} else if (c >= '0' && c <= '9') {
				i = (i * 10) - (c - '0');
			} else {
				throw new PhpUnserializeException("Expected digit or '"
						+ termChar + "' at " + in.getBytes());
			}
			first = false;
		}

		return negative ? i : -i;

	}

	private static
	void expect (
			CountingInputStream in,
			char expectChar)
			throws IOException {

		int i =
			in.read ();

		if (i == -1)
			throw new PhpUnserializeException("Unexpected end of input");
		if (i != expectChar)
			throw new PhpUnserializeException("Expected '" + expectChar
					+ "' at " + in.getBytes());
	}

	private static PhpEntity unserializeS (CountingInputStream in) throws IOException {
		expect (in, ':');
		int len = readInt (in, ':');
		if (len < 0)
			throw new PhpUnserializeException ("" + in.getBytes ()
					+ " Invalid string length " + len + " at " + in.getBytes ());
		expect (in, '"');
		byte[] data = new byte [len];
		int pos = 0;
		while (pos < len) {
			int num = in.read (data, pos, len - pos);
			if (num < 0)
				throw new PhpUnserializeException ("Unexpected end of input");
			pos += num;
		}
		expect (in, '"');
		expect (in, ';');
		return new PhpString (data);
	}

	private static PhpEntity unserializeB(CountingInputStream in)
			throws IOException {
		expect(in, ':');
		int i = readInt(in, ';');
		if (i < 0 || i > 1)
			throw new PhpUnserializeException("Expected 0 or 1 at "
					+ in.getBytes());
		return i == 1 ? PhpBoolean.pTrue : PhpBoolean.pFalse;
	}

	private static 
	PhpEntity unserializeI (
			CountingInputStream in)
		throws IOException {

		expect (in, ':');

		long i = readInt (in, ';');

		return new PhpInteger (i);

	}

	private static
	PhpEntity unserializeA (
			CountingInputStream in)
		throws IOException {

		expect(in, ':');
		int len = readInt(in, ':');
		expect(in, '{');
		Map<PhpEntity, PhpEntity> map = new LinkedHashMap<PhpEntity, PhpEntity>();
		while (len-- > 0) {
			map.put(unserialize(in), unserialize(in));
		}
		expect(in, '}');
		return new PhpArray(map);
	}

	private static
	PhpEntity unserializeN (
			CountingInputStream in)
		throws IOException {

		expect (
			in,
			';');

		return PhpNull.instance;

	}

	private static
	PhpEntity unserializeDouble (
			CountingInputStream countingInputStream)
		throws IOException {

		// read past the colon

		expect (
			countingInputStream,
			':');

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (;;) {

			// read one character

			int character =
				countingInputStream.read ();

			if (character == -1) {

				throw new PhpUnserializeException (
					"Unexpected end of input");

			}

			// detect semicolon

			if (character == ';') {

				return new PhpDouble (
					Double.parseDouble (
						stringBuilder.toString ()));

			}

			stringBuilder.append (
				(char) character);

		}

	}

	private static
	PhpEntity unserialize (
			CountingInputStream in)
		throws IOException {

		int c = in.read ();

		if (c < 0)
			throw new EOFException ();
		switch (c) {
		case 'b':
			return unserializeB (in);
		case 'i':
			return unserializeI (in);
		case 's':
			return unserializeS (in);
		case 'a':
			return unserializeA (in);
		case 'N':
			return unserializeN (in);
		case 'd':
			return unserializeDouble (in);
		}
		throw new PhpUnserializeException ("Unknown data type: '" + c + "' at " + in.getBytes ());
	}

	public static
	PhpEntity unserialize (
			InputStream inputStream)
		throws IOException {

		return unserialize (
			new CountingInputStream (
				inputStream));

	}

	private static
	class CountingInputStream
		extends FilterInputStream {

		private
		long bytes = 0;

		private
		long markBytes = 0;

		private
		CountingInputStream (
				InputStream newIn) {

			super (
				newIn);

		}

		@Override
		public
		int read ()
			throws IOException {

			int i =
				in.read ();

			if (i >= 0)
				bytes ++;

			return i;

		}

		@Override
		public
		int read (
				byte[] b,
				int off,
				int len)
			throws IOException {

			int ret =
				in.read (
					b,
					off,
					len);

			bytes +=
				ret;

			return ret;

		}

		@Override
		public
		long skip (
				long n)
			throws IOException {

			long ret =
				in.skip (n);

			bytes +=
				ret;

			return ret;

		}

		@Override
		public synchronized
		void mark (
				int readlimit) {

			in.mark (
				readlimit);

			markBytes =
				bytes;

		}

		@Override
		public synchronized
		void reset ()
			throws IOException {

			in.reset ();

			bytes =
				markBytes;

		}

		public
		long getBytes () {

			return bytes;

		}

	}

}
