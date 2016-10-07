package wbs.console.forms;

import static wbs.utils.etc.LogicUtils.equalSafe;
import static wbs.utils.etc.Misc.eitherGetLeft;
import static wbs.utils.etc.Misc.isError;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NumberUtils.equalToOne;
import static wbs.utils.etc.NumberUtils.equalToTwo;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitColon;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.Record;
import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("hiddenFormField")
@DataClass ("hidden-form-field")
public
class HiddenFormField <Container, Generic, Native>
	implements FormField <Container, Generic, Native, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	@Getter
	Boolean virtual = false;

	@Getter
	Boolean group = false;

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
	Optional <Optional <Generic>> implicitValue;

	@DataAttribute
	@Getter @Setter
	String viewPriv;

	@Getter @Setter
	Set <ScriptRef> scriptRefs =
		new LinkedHashSet<> ();

	@Getter @Setter
	FormFieldAccessor <Container, Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping <Container, Generic, Native> nativeMapping;

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, String> csvMapping;

	// implementation

	@Override
	public
	boolean canView (
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		if (
			isNull (
				viewPriv)
		) {
			return true;
		}

		List <String> privParts =
			stringSplitColon (
				viewPriv);

		if (
			equalToOne (
				privParts.size ())
		) {

			String privCode =
				privParts.get (0);

			return privChecker.canRecursive (
				(Record <?>)
				container,
				privCode);

		} else if (
			equalToTwo (
				privParts.size ())
		) {

			String delegatePath =
				privParts.get (0);

			String privCode =
				privParts.get (1);

			Record <?> delegate =
				(Record <?>)
				objectManager.dereference (
					container,
					delegatePath,
					hints);

			return privChecker.canRecursive (
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
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional <String> interfaceValue =
			requiredValue (
				eitherGetLeft (
					csvMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		if (
			formValuePresent (
				submission,
				formName)
		) {

			interfaceValue =
				Optional.of (
					formToInterface (
						submission,
						formName));

		}

		htmlWriter.writeFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.or (""),
			">\n");

	}

	@Override
	public
	void implicit (
			@NonNull Container container) {

		if (
			optionalIsNotPresent (
				implicitValue)
		) {
			return;
		}

		Optional <Native> nativeValue =
			requiredValue (
				nativeMapping.genericToNative (
					container,
					implicitValue.get ()));

		accessor.write (
			container,
			nativeValue);

	}

	@Override
	public
	UpdateResult<Generic,Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull String formName) {

		// do nothing if no value present in form

		if (
			! formValuePresent (
				submission,
				formName)
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
				submission,
				formName);

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

		Optional <Native> oldNativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional <Generic> oldGenericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					oldNativeValue));

		if (
			equalSafe (
				oldGenericValue,
				newGenericValue)
		) {

			return new UpdateResult <Generic, Native> ()

				.updated (
					false)

				.error (
					Optional.absent ());

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
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.hasParameter (
			stringFormat (
				"%h-%h",
				formName,
				name ()));

	}

	private
	String formToInterface (
			@NonNull FormFieldSubmission submission,
			@NonNull String formName) {

		return submission.parameter (
			stringFormat (
				"%h-%h",
				formName,
				name ()));

	}

}
