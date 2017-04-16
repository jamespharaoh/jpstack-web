package wbs.console.forms;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.etc.Misc.eitherGetLeft;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.isRight;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentWithClass;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.ResultUtils.getError;
import static wbs.utils.etc.ResultUtils.isError;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.string.StringUtils.stringSplitColon;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.feature.FeatureChecker;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.string.FormatWriter;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("updatableFormField")
@DataClass ("updatablesform-field")
public
class UpdatableFormField <Container, Generic, Native, Interface>
	implements FormField <Container, Generic, Native, Interface> {

	// singleton dependencies

	@SingletonDependency
	FeatureChecker featureChecker;

	@ClassSingletonDependency
	LogContext logContext;

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
	String name;

	@DataAttribute
	@Getter @Setter
	String label;

	@DataAttribute
	@Getter @Setter
	String viewPriv;

	@DataAttribute
	@Getter @Setter
	String managePriv;

	@DataAttribute
	@Getter @Setter
	String featureCode;

	@Getter @Setter
	Set <ScriptRef> scriptRefs =
		new LinkedHashSet<> ();

	@Getter @Setter
	FormFieldAccessor <Container, Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping <Container, Generic, Native> nativeMapping;

	@Getter @Setter
	List<FormFieldValueValidator <Generic>> valueValidators;

	@Getter @Setter
	FormFieldConstraintValidator <Container, Native> constraintValidator;

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, Interface> interfaceMapping;

	@Getter @Setter
	Map <String, FormFieldInterfaceMapping <Container, Generic, String>>
	shortcutInterfaceMappings;

	@Getter @Setter
	FormFieldInterfaceMapping <Container, Generic, String> csvMapping;

	@Getter @Setter
	FormFieldRenderer <Container, Interface> renderer;

	@Getter @Setter
	FormFieldUpdateHook <Container, Generic, Native> updateHook;

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
			@NonNull Map <String, Object> hints) {

		// check feature

		if (

			isNotNull (
				featureCode)

			&& ! featureChecker.checkFeatureAccess (
				featureCode)

		) {
			return false;
		}

		// check view priv

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
			collectionHasOneElement (
				privParts)
		) {

			String privCode =
				privParts.get (0);

			return privChecker.canRecursive (
				(Record<?>)
				container,
				privCode);

		} else if (
			collectionHasTwoElements (
				privParts)
		) {

			String delegatePath =
				privParts.get (0);

			String privCode =
				privParts.get (1);

			Record <?> delegate =
				(Record<?>)
				objectManager.dereferenceObsolete (
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
	void init (
			String fieldSetName) {

	}

	@Override
	public
	void renderTableCellList (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Boolean link,
			@NonNull Long columnSpan) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderTableCellList");

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional <Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						taskLogger,
						container,
						hints,
						genericValue)));

		renderer.renderHtmlTableCellList (
			taskLogger,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			link,
			columnSpan);

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map<String,Object> hints) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderTableCellProperties");

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional <Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						taskLogger,
						container,
						hints,
						genericValue)));

		renderer.renderHtmlTableCellProperties (
			taskLogger,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			true,
			1l);

	}

	@Override
	public
	void renderFormTemporarilyHidden (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderFormTemporarilyHidden");

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional <Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						taskLogger,
						container,
						hints,
						genericValue)));

		renderer.renderFormTemporarilyHidden (
			submission,
			htmlWriter,
			container,
			hints,
			interfaceValue,
			formType,
			formName);

	}

	@Override
	public
	void renderFormRow (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter formatWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <String> error,
			@NonNull FormType formType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderFormRow");

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional <Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						taskLogger,
						container,
						hints,
						genericValue)));

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label ());

		htmlTableCellOpen (
			formatWriter,
			htmlStyleAttribute (
				htmlStyleRuleEntry (
					"text-align",
					renderer.propertiesAlign ().name ())));

		renderer.renderFormInput (
			taskLogger,
			submission,
			formatWriter,
			container,
			hints,
			interfaceValue,
			formType,
			formName);

		if (
			optionalIsPresent (
				error)
		) {

			formatWriter.writeLineFormat (
				"<br>");

			formatWriter.writeLineFormat (
				"%h",
				error.get ());

		}

		htmlTableCellClose (
			formatWriter);

		htmlTableRowClose (
			formatWriter);

	}

	@Override
	public
	void renderFormReset (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter javascriptWriter,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull FormType formType,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderFormReset");

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		Optional <Interface> interfaceValue =
			requiredValue (
				eitherGetLeft (
					interfaceMapping.genericToInterface (
						taskLogger,
						container,
						hints,
						genericValue)));

		renderer.renderFormReset (
			javascriptWriter,
			container,
			interfaceValue,
			formType,
			formName);

	}

	@Override
	public
	void renderCsvRow (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter out,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderCsvRow");

		Optional <Native> nativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> genericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					nativeValue));

		String csvValue =
			optionalOr (
				eitherGetLeft (
					csvMapping.genericToInterface (
						taskLogger,
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
	UpdateResult <Generic, Native> update (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull Container container,
			@NonNull Map <String,Object> hints,
			@NonNull String formName) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"update");

		// do nothing if no value present in form

		if (
			! renderer.formValuePresent (
				submission,
				formName)
		) {

			return new UpdateResult <Generic, Native> ()

				.updated (
					false)

				.error (
					optionalAbsent ());

		}

		// get interface value from form

		Either <Optional <Interface>, String> newInterfaceValue =
			requiredValue (
				renderer.formToInterface (
					taskLogger,
					submission,
					formName));

		if (
			isError (
				newInterfaceValue)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					optionalOf (
						getError (
							newInterfaceValue)));

		}

		// convert to generic

		Either <Optional <Generic>, String> interfaceToGenericResult =
			interfaceMapping.interfaceToGeneric (
				container,
				hints,
				resultValueRequired (
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
				optionalIsPresent (
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

		Optional <Native> newNativeValue =
			requiredValue (
				nativeMapping.genericToNative (
					taskLogger,
					container,
					newGenericValue));

		// check new value

		Optional <String> constraintError =
			constraintValidator.validate (
				container,
				newNativeValue);

		if (
			optionalIsPresent (
				constraintError)
		) {

			return new UpdateResult<Generic,Native> ()

				.updated (
					false)

				.error (
					constraintError);

		}

		// get the current value, if it is the same, do nothing

		Optional <Native> oldNativeValue =
			requiredValue (
				accessor.read (
					taskLogger,
					container));

		Optional <Generic> oldGenericValue =
			requiredValue (
				nativeMapping.nativeToGeneric (
					container,
					oldNativeValue));

		if (
			optionalEqualOrNotPresentWithClass (
				Object.class,
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
			taskLogger,
			container,
			newNativeValue);

		return new UpdateResult <Generic, Native> ()

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UpdateResult <Generic, Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		updateHook.onUpdate (
			parentTaskLogger,
			updateResult,
			container,
			linkObject,
			objectRef,
			objectType);

	}

}
