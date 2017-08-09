package wbs.console.forms.text;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.ResultUtils.successResultAbsent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldInterfaceMapping;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.LazyFormatWriter;

import fj.data.Either;
import wbs.web.utils.JsonToHtml;

@PrototypeComponent ("jsonFormFieldInterfaceMapping")
public
class JsonFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, String, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ();

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {
				return successResultAbsent ();
			}

			formatWriter.writeFormat (
				"<pre>\n");

			JsonToHtml jsonToHtml =
				new JsonToHtml ()

				.formatWriter (
					formatWriter);

			jsonToHtml.write (
				genericValue.get ());

			formatWriter.writeFormat (
				"\n</pre>");

			return successResult (
				optionalOf (
					formatWriter.toString ()));

		}

	}

}
