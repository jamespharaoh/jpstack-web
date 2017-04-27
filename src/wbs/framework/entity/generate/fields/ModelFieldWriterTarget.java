package wbs.framework.entity.generate.fields;

import lombok.NonNull;

import wbs.framework.codegen.JavaImportRegistry;

import wbs.utils.string.FormatWriter;

public
class ModelFieldWriterTarget {

	private
	JavaImportRegistry imports;

	private
	FormatWriter formatWriter;

	// property setters

	public
	ModelFieldWriterTarget imports (
			@NonNull JavaImportRegistry imports) {

		this.imports = imports;

		return this;

	}

	public
	ModelFieldWriterTarget formatWriter (
			@NonNull FormatWriter formatWriter) {

		this.formatWriter = formatWriter;

		return this;

	}

	// property accessors

	public
	JavaImportRegistry imports () {
		return imports;
	}

	public
	FormatWriter formatWriter () {
		return formatWriter;
	}

}
