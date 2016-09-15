package wbs.framework.codegen;

import wbs.utils.string.FormatWriter;

@FunctionalInterface
public
interface JavaBlockWriter {

	void writeBlock (
			JavaImportRegistry imports,
			FormatWriter formatWriter);

}
