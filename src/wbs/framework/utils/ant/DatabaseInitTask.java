package wbs.framework.utils.ant;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import wbs.framework.component.config.WbsConfig;

import com.google.common.base.Joiner;

import lombok.Data;

public
class DatabaseInitTask
	extends Task {

	WbsConfig wbsConfig;

	List<ScriptElement> scriptElements =
		new ArrayList<ScriptElement> ();

	public
	void addScript (
			ScriptElement scriptElement) {

		scriptElements.add (
			scriptElement);

	}

	@Override
	public
	void init () {

		String configFilename =
			System.getenv (
				"WBS_CONFIG_XML");

		if (configFilename == null) {

			throw new RuntimeException (
				stringFormat (
					"Please set WBS_CONFIG_XML"));

		}

		wbsConfig =
			WbsConfig.readFilename (
				configFilename);

	}

	@Override
	public
	void execute ()
		throws BuildException {

		try {

			initDatabase ();

		} catch (InterruptedException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	private
	void shell (
			String command)
		throws InterruptedException {

		try {

			String args[] = {
				"bash",
				"-c",
				command
			};

			Process proc =
				new ProcessBuilder ()
//					.inheritIO ()
					.directory (getProject ().getBaseDir ())
					.command (args)
					.start ();

			gobbleStream (
				proc.getInputStream (),
				"OUT");

			gobbleStream (
				proc.getErrorStream (),
				"ERR");

			int result =
				proc.waitFor ();

			if (result != 0) {

				throw new RuntimeException (
					stringFormat (
						"Command \"%s\" returned %s",
						command,
						result));

			}

		} catch (IOException ioException) {

			throw new RuntimeException (
				ioException);

		}

	}

	public
	void initDatabase ()
		throws InterruptedException {

		List<String> parts =
			new ArrayList<String> ();

		for (ScriptElement scriptElement
				: scriptElements) {

			File scriptFile =
				new File (
					stringFormat (
						"%s/%s",
						getProject ().getBaseDir (),
						scriptElement.getName ()));

			if (! scriptFile.exists ()) {

				throw new RuntimeException (
					stringFormat (
						"No such file: %s",
						scriptElement.getName ()));

			}

			parts.add (
				stringFormat (
					Joiner.on ("; ").join (
						"echo '\\echo'",
						stringFormat (
							"echo '\\echo RUN SCRIPT %s'",
							scriptElement.getName ()),
						"echo '\\echo'",
						stringFormat (
							"cat %s",
							scriptElement.getName ()))));

		}

		String command =
			stringFormat (
				"( %s; %s ) | %s",
				"echo '\\set ON_ERROR_STOP 1'",
				Joiner.on ("; ").join (parts),
				stringFormat (
					"psql %s",
					wbsConfig.database ().databaseName ()));

		System.out.println (
			command);

		shell (
			command);

	}

	private
	void gobbleStream (
			InputStream in,
			String type) {

		StreamGobbler gobbler =
			new StreamGobbler (in, type);

		gobbler.start ();

	}

	private
	class StreamGobbler
		extends Thread {

		InputStream inputStream;
		String type;

		private
		StreamGobbler (
				InputStream inputStream,
				String type) {

			this.inputStream =
				inputStream;

			this.type =
				type;

		}

		@Override
		public
		void run () {

			try {

				InputStreamReader inputStreamReader =
					new InputStreamReader (inputStream);

				BufferedReader bufferedReader =
					new BufferedReader (inputStreamReader);

				String line =
					null;

				while ((line = bufferedReader.readLine ()) != null) {

					System.out.println (type + "> " + line);

				}

			} catch (IOException exception) {

				exception.printStackTrace ();

			}

		}

	}

	@Data
	public static
	class ScriptElement {
		String name;
	}

}