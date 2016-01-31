package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.eitherGetLeft;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.getError;
import static wbs.framework.utils.etc.Misc.getValue;
import static wbs.framework.utils.etc.Misc.isError;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.isRight;
import static wbs.framework.utils.etc.Misc.optionalOr;
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
import wbs.framework.record.PermanentRecord;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("updatableFormField")
@DataClass ("updatable-form-field")
public
class UpdatableFormField<Container,Generic,Native,Interface>
	implements FormField<Container,Generic,Native,Interface> {

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
	List<FormFieldValueValidator<Generic>> valueValidators;

	@Getter @Setter
	FormFieldConstraintValidator<Container,Native> constraintValidator;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,Interface> interfaceMapping;

	@Getter @Setter
	Map<String,FormFieldInterfaceMapping<Container,Generic,String>>
	shortcutInterfaceMappings;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,String> csvMapping;

	@Getter @Setter
	FormFieldRenderer<Container,Interface> renderer;

	@Getter @Setter
	FormFieldUpdateHook<Container,Generic,Native> updateHook;

	// implementation

	@Override
	public
	Boolean fileUpload () {
		return renderer.fileUpload ();
	}

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
	void init (
			String fieldSetName) {

	}

	@Override
	public
	void renderTableCellList (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			boolean link,
			int colspan) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		htmlWriter.writeFormat (
			"%s",
			renderer.interfaceToHtmlTableCell (
				container,
				interfaceValue,
				link,
				colspan));

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		htmlWriter.writeFormat (
			"<td>%s</td>\n",
			renderer.interfaceToHtmlComplex (
				container,
				interfaceValue));

	}

	@Override
	public
	void renderFormTemporarilyHidden (
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

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		renderer.renderFormTemporarilyHidden (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			formType);

	}

	@Override
	public
	void renderFormRow (
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> error,
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

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		out.writeFormat (
			"<tr>\n",
			"<th>%h</th>\n",
			label (),
			"<td>");

		renderer.renderFormInput (
			submission,
			out,
			container,
			hints,
			interfaceValue,
			formType);

		if (
			isPresent (
				error)
		) {

			out.writeFormat (
				"<br>\n",
				"%h",
				error.get ());

		}

		out.writeFormat (
			"</td>\n",
			"</tr>\n");

	}

	@Override
	public
	void renderFormReset (
			@NonNull FormatWriter javascriptWriter,
			@NonNull String indent,
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

		Optional<Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						container,
						hints,
						genericValue)));

		renderer.renderFormReset (
			javascriptWriter,
			indent,
			container,
			interfaceValue,
			formType);

	}

	@Override
	public
	void renderCsvRow (
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		Optional<Native> nativeValue =
			requiredValue (
				accessor.read (
					container));

		Optional<Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		String csvValue =
			optionalOr (
				eitherGetLeft (
					csvMapping.genericToInterface (
						container,
						hints,
						genericValue)),
				"");

		out.writeFormat (
			"\"%s\"",
			csvValue.replace ("\"", "\"\""));

	}

	@Override
	public
	UpdateResult<Generic,Native> update (
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		// do nothing if no value present in form

		if (
			! renderer.formValuePresent (
				submission)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					Optional.<String>absent ());

		}

		// get interface value from form

		Either<Optional<Interface>,String> newInterfaceValue =
			requiredValue (
				renderer.formToInterface (
					submission));

		if (
			isError (
				newInterfaceValue)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					Optional.of (
						getError (
							newInterfaceValue)));

		}

		// convert to generic

System.out.println (
	"INTERFACE VALUE: " + newInterfaceValue.getClass ().getName ());

System.out.println (
	"INTERFACE MAPPIN: " + interfaceMapping.getClass ().getName ());

		Either<Optional<Generic>,String> interfaceToGenericResult =
			interfaceMapping.interfaceToGeneric (
				container,
				hints,
				getValue (
					newInterfaceValue));

		if (
			isRight (
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

		// perform value validation

		for (
			FormFieldValueValidator<Generic> valueValidator
				: valueValidators
		) {

			Optional<String> valueError =
				valueValidator.validate (
					newGenericValue);

			if (
				isPresent (
					valueError)
			) {

				return new UpdateResult<Generic,Native> ()

					.updated (
						false)

					.error (
						Optional.of (
							valueError.get ()));

			}

		}

		// convert to native

		Optional<Native> newNativeValue =
			requiredValue (
				nativeMapping.genericToNative (
					container,
					newGenericValue));

		// check new value

		Optional<String> constraintError =
			constraintValidator.validate (
				container,
				newNativeValue);

		if (
			isPresent (
				constraintError)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					constraintError);

		}

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

	@Override
	public
	void runUpdateHook (
			@NonNull UpdateResult<Generic,Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		updateHook.onUpdate (
			updateResult,
			container,
			linkObject,
			objectRef,
			objectType);

	}

}
