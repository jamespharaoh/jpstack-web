package wbs.framework.codegen;

import wbs.framework.utils.formatwriter.FormatWriter;

@FunctionalInterface
public 
interface JavaBlockWriter {

	void writeBlock (
			JavaImportRegistry imports,
			FormatWriter formatWriter);

}
