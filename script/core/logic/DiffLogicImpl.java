package wbs.platform.script.core.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import lombok.SneakyThrows;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("diffLogic")
public
class DiffLogicImpl
	implements DiffLogic {

	@Override
	@SneakyThrows ({
		IOException.class,
		InterruptedException.class
	})
	public
	String unidiff (
			String oldString,
			String newString) {

		File oldFile = null;
		File newFile = null;

		try {

			oldFile =
				File.createTempFile (
					"wbs",
					null);

			newFile =
				File.createTempFile (
					"wbs",
					null);

			// write the old file

			OutputStream os = new FileOutputStream(oldFile);
			Writer w = new OutputStreamWriter(os, "utf-8");
			w.write(oldString);
			w.close();

			// write the new file

			os = new FileOutputStream(newFile);
			w = new OutputStreamWriter(os, "utf-8");
			w.write(newString);
			w.close();

			// run diff

			Process process =
				Runtime.getRuntime ().exec (
					new String [] {
						"diff",
						"-u",
						oldFile.getAbsolutePath (),
						newFile.getAbsolutePath ()
					});

			// read the output

			InputStream inputStream =
				process.getInputStream ();

			Reader reader =
				new InputStreamReader (
					inputStream,
					"utf-8");

			char[] buf =
				new char [1024];

			int numread;

			StringBuilder stringBuilder =
				new StringBuilder ();

			while ((numread = reader.read (buf, 0, 1024)) >= 0) {

				stringBuilder.append (
					buf,
					0,
					numread);

			}

			inputStream.close ();

			// make sure the process is ended

			process.waitFor ();

			// drop the first two lines

			String string =
				stringBuilder.toString ();

			string =
				string.substring (
					string.indexOf (
						'\n',
						string.indexOf ('\n') + 1
					) + 1);

			// and return

			return string;

		} finally {

			if (oldFile != null)
				oldFile.delete ();

			if (newFile != null)
				newFile.delete ();

		}

	}

}
