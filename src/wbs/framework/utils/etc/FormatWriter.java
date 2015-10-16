package wbs.framework.utils.etc;

import java.io.IOException;

public
interface FormatWriter {

	void writeFormat (
			Object... arguments)
		throws IOException;

	public
	void close ()
		throws IOException;

}
