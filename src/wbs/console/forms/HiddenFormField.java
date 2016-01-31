package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.eitherGetLeft;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isError;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.requiredValue;
import static wbs.framework.utils.etc.Misc.split;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.priv.PrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("hiddenFormField")
@DataClass ("hidden-form-field")
public 
class HiddenFormField<Container,Generic,Native>
	implements FormField<Container,Generic,Native,String> {

	// dependencies
	
	@Inject
	ConsoleObjectManager objectManager;
	
	@Inject
	PrivChecker privChecker;

	// properties

	@Getter
	Boolean virtual = false;

	@DataAttribute
	@Getter @Setter
	Boolean large = false;

	@DataAttribute
	@Getter @Setter
	Boolean fileUpload = false;

	@DataAttribute
	@Getter @Setter
	String name;

	@DataAttribute
	@Getter @Setter
	String label;

	@DataAttribute
	@Getter @Setter
	String viewPriv;

	@Getter @Setter
	Set<ScriptRef> scriptRefs =
		new LinkedHashSet<ScriptRef> ();

	@Getter @Setter
	FormFieldAccessor<Container,Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping<Container,Generic,Native> nativeMapping;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,String> csvMapping;

	// implementation

	@Override
	public 
	boolean canView (
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		if (
			isNull (
				viewPriv)
		) {
			return true;
		}

		List<String> privParts =
			split (
				viewPriv,
				":");

		if (
			equal (
				privParts.size (),
				1)
		) {
	
			String privCode =
				privParts.get (0);

			return privChecker.can (
				(Record<?>)
				container,
				privCode);

		} else if (
			equal (
				privParts.size (),
				2)
		) {

			String delegatePath =
				privParts.get (0);

			String privCode =
				privParts.get (1);

			Record<?> delegate =
				(Record<?>)
				objectManager.dereference (
					container,
					delegatePath,
					hints);

			return privChecker.can (
				delegate,
				privCode);

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	void renderFormAlwaysHidden (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull FormType formType) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional<String> interfaceValue =
			requiredValue (
				eitherGetLeft (
					csvMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		if (
			formValuePresent (
				submission)
		) {

			interfaceValue =
				Optional.of (
					formToInterface (
						submission));

		}

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h\"",
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">\n");

	}

	@Override
	public
	UpdateResult<Generic,Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		// do nothing if no value present in form

		if (
			! formValuePresent (
				submission)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					Optional.<String>absent ());

		}

		// get interface value from form

		String newInterfaceValue =
			formToInterface (
				submission);

		// convert to generic

		Either<Optional<Generic>,String> interfaceToGenericResult =
			csvMapping.interfaceToGeneric (
				container,
				hints,
				Optional.of (
					newInterfaceValue));

		if (
			isError (
				interfaceToGenericResult)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					Optional.of (
						interfaceToGenericResult.right ().value ()));

		}

		Optional<Generic> newGenericValue =
			interfaceToGenericResult.left ().value ();

		// convert to native

		Optional<Native> newNativeValue =
			requiredValue (
				nativeMapping.genericToNative (
					container,
					newGenericValue));

		// get the current value, if it is the same, do nothing

		Optional<Native> oldNativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> oldGenericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					oldNativeValue));

		if (
			equal (
				oldGenericValue,
				newGenericValue)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					Optional.<String>absent ());

		}

		// set the new value

		accessor.write (
			container,
			newNativeValue);

		return new UpdateResult<Generic,Native> ()

			.updated (
				true)

			.oldGenericValue (
				oldGenericValue)

			.newGenericValue (
				newGenericValue)

			.oldNativeValue (
				oldNativeValue)

			.newNativeValue (
				newNativeValue)

			.error (
				Optional.<String>absent ());

	}

	private
	boolean formValuePresent (
			@NonNull FormFieldSubmission submission) {
	
		return submission.hasParameter (
			name ());

	}

	private
	String formToInterface (
			@NonNull FormFieldSubmission submission) {
	
		return submission.parameter (
			name ());
	
	}

}
