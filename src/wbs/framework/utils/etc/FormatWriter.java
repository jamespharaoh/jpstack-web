package wbs.framework.utils.etc;

public
interface FormatWriter {

	void writeFormat (
			Object... arguments);

	void writeFormatArray (
			Object[] arguments);

	public
	void close ();

}
