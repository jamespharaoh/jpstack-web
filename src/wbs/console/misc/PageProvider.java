package wbs.console.misc;

import java.io.PrintWriter;

public
interface PageProvider<DataType> {

	int pages (
			DataType data);

	void writePage (
			PrintWriter writer,
			DataType data,
			int page);

}
