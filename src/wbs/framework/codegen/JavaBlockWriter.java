package wbs.framework.codegen;

import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

@FunctionalInterface
public
interface JavaBlockWriter {

	void writeBlock (
			TaskLogger taskLogger,
			JavaImportRegistry imports,
			FormatWriter formatWriter);

}
