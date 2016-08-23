package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.errorResult;
import static wbs.framework.utils.etc.Misc.getError;
import static wbs.framework.utils.etc.Misc.getValue;
import static wbs.framework.utils.etc.Misc.isError;
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
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalGetRequired;

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
			@NonNull FormType formType,
			@NonNull String formName) {

		minimum.renderFormTemporarilyHidden (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			formType,
			formName);

		maximum.renderFormTemporarilyHidden (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMaximum ())
				: Optional.absent (),
			formType,
			formName);

	}

	@Override
	public void renderFormInput (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Interface>> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		minimum.renderFormInput (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			formType,
			formName);

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
			formType,
			formName);

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter htmlWriter,
			@NonNull String indent,
			@NonNull Container container,
			@NonNull Optional<Range<Interface>> interfaceValue,
			@NonNull FormType formType,
			@NonNull String formName) {

		minimum.renderFormReset (
			htmlWriter,
			indent,
			container,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			formType,
			formName);

		maximum.renderFormReset (
			htmlWriter,
			indent,
			container,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMaximum ())
				: Optional.absent (),
			formType,
			formName);

	}

	@Override
	public
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return (

			minimum.formValuePresent (
				submission,
				formName)

			&& maximum.formValuePresent (
				submission,
				formName)

		);

	}

	@Override
	public
	Either<Optional<Range<Interface>>,String> formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		Either<Optional<Interface>,String> leftResult =
			minimum.formToInterface (
				submission,
				formName);

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
				submission,
				formName);

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

			optionalGetRequired (
				getValue (
					minimum.formToInterface (
						submission,
						formName))),

			optionalGetRequired (
				getValue (
					maximum.formToInterface (
						submission,
						formName)))

		)));

	}

	@Override
	public
	void renderHtmlSimple (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Interface>> interfaceValue,
			boolean link) {

		minimum.renderHtmlSimple (
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			link);

		htmlWriter.writeFormat (
			" to ");

		maximum.renderHtmlSimple (
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent (),
			link);

	}

	@Override
	public
	void renderHtmlComplex (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Range<Interface>> interfaceValue) {

		minimum.renderHtmlComplex (
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent ());

		htmlWriter.writeFormat (
			" to ");

		maximum.renderHtmlComplex (
			htmlWriter,
			container,
			hints,
			interfaceValue.isPresent ()
				? Optional.of (
					interfaceValue.get ().getMinimum ())
				: Optional.absent ());

	}

}
