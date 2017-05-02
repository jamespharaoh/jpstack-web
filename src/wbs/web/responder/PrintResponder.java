package wbs.web.responder;

import static wbs.utils.string.FormatWriterUtils.setCurrentFormatWriter;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

public abstract
class PrintResponder
	extends AbstractResponder {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// state

	protected
	FormatWriter formatWriter;

	// implementation

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

			super.setup (
				transaction);

			formatWriter =
				requestContext.formatWriter ()

				.indentString (
					"  ");

			setCurrentFormatWriter (
				formatWriter);

		}

	}

}
