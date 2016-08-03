package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.io.StringWriter;
import java.util.Map;

import lombok.NonNull;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;
import wbs.framework.utils.etc.FormatWriterWriter;
import wbs.framework.utils.etc.JsonToHtml;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

@PrototypeComponent ("jsonFormFieldInterfaceMapping")
public
class JsonFormFieldInterfaceMapping<Container>
	implements FormFieldInterfaceMapping<Container,String,String> {

	@Override
	public
	Either<Optional<String>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException (
			stringFormat (
				"%s.%s",
				getClass ().getSimpleName (),
				"interfaceToGeneric (...)"));

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> genericValue) {

		if (
			isNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		StringWriter stringWriter =
			new StringWriter ();

		FormatWriter formatWriter =
			new FormatWriterWriter (
				stringWriter);

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
			Optional.of (
				stringWriter.toString ()));

	}

}
