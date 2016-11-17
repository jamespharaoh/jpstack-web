package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringSplitNewline;
import static wbs.utils.string.StringUtils.uppercase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.exception.ExceptionUtilsImplementation;
import wbs.utils.string.FormatWriter;

public
class FormatWriterLogTarget
	implements LogTarget {

	private final
	FormatWriter formatWriter;

	public
	FormatWriterLogTarget (
			@NonNull FormatWriter formatWriter) {

		this.formatWriter =
			formatWriter;

	}

	@Override
	public
	void writeToLog (
			@NonNull LogSeverity severity,
			@NonNull String message,
			@NonNull Optional <Throwable> exception) {

		formatWriter.writeLineFormat (
			"%s: %s",
			uppercase (
				severity.name ()),
			message);

		if (
			optionalIsPresent (
				exception)
		) {

			formatWriter.increaseIndent ();

			StringWriter stringWriter =
				new StringWriter ();

			ExceptionUtilsImplementation.writeThrowable (
				exception.get (),
				new PrintWriter (
					stringWriter));

			List <String> exceptionLines =
				stringSplitNewline (
					stringWriter.toString ());

			exceptionLines.forEach (
				exceptionLine ->
					formatWriter.writeLineFormat (
						"%s",
						exceptionLine));

			formatWriter.decreaseIndent ();

		}

	}

	@Override
	public
	boolean debugEnabled () {
		return true;
	}

}