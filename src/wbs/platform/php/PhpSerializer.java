package wbs.platform.php;

import static wbs.framework.utils.etc.StringUtils.stringToBytes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.joda.time.LocalDate;

public
class PhpSerializer {

	private
	String charsetName = "utf8";

	public
	PhpSerializer (
			String newCharsetName) {

		charsetName =
			newCharsetName;

	}

	private
	void serializeByteArray (
			OutputStream out,
			byte[] string)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"s:%d:\"",
					string.length),
				"iso-8859-1"));

		out.write (
			string);

		out.write (
			stringToBytes (
				"\";",
				"iso-8859-1"));

	}

	private
	void serializeBoolean (
			OutputStream out,
			Boolean value)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"b:%d;",
					value
						? 1
						: 0),
				"iso-8859-1"));

	}

	private
	void serializeInteger (
			OutputStream out,
			Integer value)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"i:%d;",
					value),
				"iso-8859-1"));

	}

	private
	void serializeLong (
			OutputStream out,
			Long value)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"i:%d;",
					value),
				"iso-8859-1"));

	}

	private
	void serializeString (
			OutputStream out,
			String string)
		throws IOException {

		serialize (
			out,
			stringToBytes (
				string,
				charsetName));

	}

	private
	void serializeDouble (
			OutputStream out,
			Double value)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"d:%s;",
					value),
				"iso-8859-1"));

	}

	private
	void serializeFloat (
			OutputStream out,
			Float value)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"d:%s;",
					value),
				"iso-8859-1"));

	}

	private
	void serializeMap (
			OutputStream out,
			Map<?,?> map)
		throws IOException {

		out.write (
			stringToBytes (
				String.format (
					"a:%d:{",
					map.size ()),
				"iso-8859-1"));

		for (Map.Entry<?,?> entry
				: map.entrySet ()) {

			serialize (
				out,
				PhpMisc.normaliseKey (
					entry.getKey ()));

			serialize (
				out,
				entry.getValue ());

		}

		out.write (
			stringToBytes (
				"}",
				"iso-8859-1"));

	}

	private void serializeCollection(OutputStream out, Collection<?> collection)
			throws IOException {
		out.write(String.format("a:%d:{", collection.size()).getBytes(
				"iso-8859-1"));
		int i = 0;
		for (Object obj : collection) {
			serialize(out, i++);
			serialize(out, obj);
		}
		out.write("}".getBytes("iso-8859-1"));
	}

	private void serializeArray(OutputStream out, Object[] array)
			throws IOException {
		out.write(String.format("a:%d:{", array.length).getBytes("iso-8859-1"));
		int i = 0;
		for (Object obj : array) {
			serialize(out, i++);
			serialize(out, obj);
		}
		out.write("}".getBytes("iso-8859-1"));
	}

	public
	void serialize (
			OutputStream out,
			Object object)
		throws IOException {

		if (object instanceof PhpEntity) {

			object =
				((PhpEntity) object).asObject();

		}

		if (object == null) {

			out.write (
				stringToBytes (
					"N;",
					"iso-8859-1"));

		} else if (object instanceof byte[]) {

			serializeByteArray (
				out,
				(byte[]) object);

		} else if (object instanceof Object[]) {

			serializeArray (
				out,
				(Object[]) object);

		} else if (object instanceof Boolean) {

			serializeBoolean (
				out,
				(Boolean) object);

		} else if (object instanceof Integer) {

			serializeInteger (
				out,
				(Integer) object);

		} else if (object instanceof Long) {

			serializeLong (
				out,
				(Long) object);

		} else if (object instanceof String) {

			serializeString (
				out,
				(String) object);

		} else if (object instanceof Map) {

			serializeMap (
				out,
				(Map<?,?>) object);

		} else if (object instanceof Double) {

			serializeDouble (
				out,
				(Double) object);

		} else if (object instanceof Float) {

			serializeFloat (
				out,
				(Float) object);

		} else if (object instanceof Collection) {

			serializeCollection (
				out,
				(Collection<?>) object);

		} else if (object instanceof LocalDate) {

			serializeString (
				out,
				object.toString ());

		} else {

			throw new PhpSerializeException (
				object.getClass ());

		}

	}

	public static
	void serialize (
			OutputStream out,
			Object object,
			String charsetName)
		throws IOException {

		PhpSerializer phpSerializer =
			new PhpSerializer (
				charsetName);

		phpSerializer.serialize (
			out,
			object);

	}

}
