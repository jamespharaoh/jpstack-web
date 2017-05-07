package wbs.framework.logging;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringSplitNewline;
import static wbs.utils.string.StringUtils.uppercase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionUtils;

import wbs.utils.string.FormatWriter;

public
class FormatWriterLogTarget
	implements LogTarget {

	// singleton dependencies

	@SingletonDependency
	ExceptionUtils exceptionUtils;

	@ClassSingletonDependency
	LogContext logContext;

	// state

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull LogSeverity severity,
			@NonNull CharSequence message,
			@NonNull Optional <Throwable> exception) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"writeToLog");

		) {

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

				exceptionUtils.writeThrowable (
					taskLogger,
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

	}

	@Override
	public
	Boolean debugEnabled () {
		return true;
	}

}