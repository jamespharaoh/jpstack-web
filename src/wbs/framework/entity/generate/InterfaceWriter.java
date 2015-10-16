package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormatArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
public
class InterfaceWriter {

	@Getter @Setter
	String packageName;

	@Getter @Setter
	String name;

	List<String> imports =
		new ArrayList<String> ();

	List<String> interfaces =
		new ArrayList<String> ();

	public
	InterfaceWriter addImport (
			String... args) {

		imports.add (
			stringFormatArray (args));

		return this;

	}

	public
	InterfaceWriter addInterface (
			String... args) {

		interfaces.add (
			stringFormatArray (args));

		return this;

	}

	public
	void write (
			String filename)
		throws IOException {

		@Cleanup
		FormatWriter writer =
			new AtomicFileWriter (
				filename);

		writer.writeFormat (
			"package %s;\n",
			packageName);

		writer.writeFormat (
			"\n");

		if (! imports.isEmpty ()) {

			for (
				String importValue
					: imports
			) {

				writer.writeFormat (
					"import %s;\n",
					importValue);

			}

			writer.writeFormat (
				"\n");

		}

		writer.writeFormat (
			"public\n");

		writer.writeFormat (
			"interface %s",
			name);

		if (! interfaces.isEmpty ()) {

			writer.writeFormat (
				"\n\textends %s",
				joinWithSeparator (
					",\n\t\t",
					interfaces));

		}

		writer.writeFormat (
			" {\n");

		writer.writeFormat (
			"\n");

		writer.writeFormat (
			"}\n");

		writer.writeFormat (
			"\n");

		writer.close ();

	}

}
