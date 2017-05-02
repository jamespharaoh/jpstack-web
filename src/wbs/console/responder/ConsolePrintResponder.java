package wbs.console.responder;

import static wbs.utils.string.FormatWriterUtils.clearCurrentFormatWriter;
import static wbs.utils.string.FormatWriterUtils.setCurrentFormatWriter;

import lombok.NonNull;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

public abstract
class ConsolePrintResponder
	extends ConsoleResponder {

	// dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	protected
	ConsoleRequestContext requestContext;

	// state

	protected
	FormatWriter formatWriter;

	// implenentation

	@Override
	protected
	void setup (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setup");

		) {

			formatWriter =
				requestContext.formatWriter ()

				.indentString (
					"  ");

			setCurrentFormatWriter (
				formatWriter);

		}

	}

	@Override
	protected
	void cleanup () {

		clearCurrentFormatWriter (
			formatWriter);

	}

}
