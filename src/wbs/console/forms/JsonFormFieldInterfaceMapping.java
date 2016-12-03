package wbs.console.forms;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;

import java.io.StringWriter;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.PrototypeComponent;

import wbs.utils.string.FormatWriter;
import wbs.utils.string.WriterFormatWriter;

import fj.data.Either;
import wbs.web.utils.JsonToHtml;

@PrototypeComponent ("jsonFormFieldInterfaceMapping")
public
class JsonFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <Container, String, String> {

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.absent ());

		}

		StringWriter stringWriter =
			new StringWriter ();

		FormatWriter formatWriter =
			new WriterFormatWriter (
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
			optionalOf (
				stringWriter.toString ()));

	}

}
