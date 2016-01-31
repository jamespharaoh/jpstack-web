package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.getError;
import static wbs.framework.utils.etc.Misc.getValue;
import static wbs.framework.utils.etc.Misc.isError;
import static wbs.framework.utils.etc.Misc.isNotPresent;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.Range;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.forms.FormField.FormType;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("rangeFormFieldRenderer")
public
class RangeFormFieldRenderer<
		Container,
		Interface extends Comparable<Interface>
	> implements FormFieldRenderer<
		Container,
		Range<Interface>
	> {

	// properties

	@Getter @Setter
	FormFieldRenderer<Container,Interface> minimum;

	@Getter @Setter
	FormFieldRenderer<Container,Interface> maximum;

	// implementation

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Interface>> interfaceValue,
			@NonNull FormType formType) {

		minimum.renderFormTemporarilyHidden (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			formType);

		maximum.renderFormTemporarilyHidden (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMaximum ())
				: Optional.absent (),
			formType);

	}

	@Override
	public void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Interface>> interfaceValue,
			@NonNull FormType formType) {

		minimum.renderFormInput (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			formType);

		htmlWriter.writeFormat (
			" to ");

		maximum.renderFormInput (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMaximum ())
				: Optional.absent (),
			formType);

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter htmlWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Range<Interface>> interfaceValue,
			@NonNull FormType formType) {

		minimum.renderFormReset (
			htmlWriter,
			indent,
			container,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			formType);

		maximum.renderFormReset (
			htmlWriter,
			indent,
			container,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMaximum ())
				: Optional.absent (),
			formType);

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {

		return (

			minimum.formValuePresent (
				submission)

			&& maximum.formValuePresent (
				submission)

		);

	}

	@Override
	public
	Either<Optional<Range<Interface>>,String> formToInterface (
			@NonNull FormFieldSubmission submission) {

		Either<Optional<Interface>,String> leftResult =
			minimum.formToInterface (
				submission);

		if (
			isError (
				leftResult)
		) {

			return errorResult (
				getError (
					leftResult));

		}

		Either<Optional<Interface>,String> rightResult =
			maximum.formToInterface (
				submission);

		if (
			isError (
				rightResult)
		) {

			return errorResult (
				getError (
					rightResult));

		}

		if (

			isNotPresent (
				getValue (
					leftResult))

			|| isNotPresent (
				getValue (
					rightResult))
		) {

			return successResult (
				Optional.absent ());

		}

		return successResult (
			Optional.of (
				Range.between (

			optionalRequired (
				getValue (
					minimum.formToInterface (
						submission))),

			optionalRequired (
				getValue (
					maximum.formToInterface (
						submission)))

		)));

	}

	@Override
	public
	String interfaceToHtmlSimple (
			@NonNull Container container,
			@NonNull Optional<Range<Interface>> interfaceValue,
			boolean link) {

		return stringFormat (
			"%s to %s",

			minimum.interfaceToHtmlSimple (
				container,
				interfaceValue.isPresent ()
					? Optional.of (
						interfaceValue.get ().getMinimum ())
					: Optional.absent (),
				link),

			maximum.interfaceToHtmlSimple (
				container,
				interfaceValue.isPresent ()
					? Optional.of (
						interfaceValue.get ().getMinimum ())
					: Optional.absent (),
				link)

		);

	}

	@Override
	public
	String interfaceToHtmlComplex (
			@NonNull Container container,
			@NonNull Optional<Range<Interface>> interfaceValue) {

		return stringFormat (
			"%s to %s",

			minimum.interfaceToHtmlComplex (
				container,
				interfaceValue.isPresent ()
					? Optional.of (
						interfaceValue.get ().getMinimum ())
					: Optional.absent ()),

			maximum.interfaceToHtmlComplex (
				container,
				interfaceValue.isPresent ()
					? Optional.of (
						interfaceValue.get ().getMinimum ())
					: Optional.absent ())

		);

	}

}
