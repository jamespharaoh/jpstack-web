package wbs.framework.utils.etc;

public
interface FormatWriter {

	void writeFormat (
			Object... arguments);

	void writeFormatArray (
			Object[] arguments);

	void close ();

	void indent ();

	void unindent ();

}
