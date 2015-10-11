package wbs.framework.entity.generate;

import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormatArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
		OutputStream outputStream =
			new FileOutputStream (
				filename);

		@Cleanup
		FormatWriter writer =
			new FormatWriter (
				new OutputStreamWriter (
					outputStream));

		writer.write (

			"\n",

			"package %s;\n",
			packageName,

			"\n");

		if (! imports.isEmpty ()) {

			for (
				String importValue
					: imports
			) {

				writer.write (

					"import %s;\n",
					importValue);

			}

			writer.write (

				"\n");

		}

		writer.write (

			"public\n",

			"interface %s",
			name);

		if (! interfaces.isEmpty ()) {

			writer.write (

				"\n\textends %s",
				joinWithSeparator (
					",\n\t\t",
					interfaces));

		}

		writer.write (

			" {\n",

			"\n",

			"}\n",

			"\n");

		writer.close ();

	}

}
