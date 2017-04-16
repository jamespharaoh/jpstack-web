package wbs.console.forms;

import static wbs.utils.collection.CollectionUtils.collectionHasOneElement;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoElements;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.eitherGetLeft;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitColon;
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

@Accessors (fluent = true)
@PrototypeComponent ("readOnlyFormField")
@DataClass ("read-only-form-field")
public
class ReadOnlyFormField <Container, Generic, Native, Interface>
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
	String featureCode;

	@Getter @Setter
	Set<ScriptRef> scriptRefs =
		new LinkedHashSet<ScriptRef> ();

	@Getter @Setter
	FormFieldAccessor<Container,Native> accessor;

	@Getter @Setter
	FormFieldNativeMapping<Container,Generic,Native> nativeMapping;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,Interface> interfaceMapping;

	@Getter @Setter
	FormFieldInterfaceMapping<Container,Generic,String> csvMapping;

	@Getter @Setter
	FormFieldRenderer<Container,Interface> renderer;

	// implementation

	@Override
	public
	Boolean fileUpload () {
		return renderer.fileUpload ();
	}

	@Override
	public
	boolean canView (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Container container,
			@NonNull Map <String, Object> hints) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"canView");

		// check feature

		if (

			isNotNull (
				featureCode)

			&& ! featureChecker.checkFeatureAccess (
				taskLogger,
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
				taskLogger,
				(Record<?>) container,
				privCode);

		} else if (
			collectionHasTwoElements (
				privParts)
		) {

			String delegatePath =
				privParts.get (0);

			String privCode =
				privParts.get (1);

			Record<?> delegate =
				(Record<?>)
				objectManager.dereferenceObsolete (
					container,
					delegatePath,
					hints);

			return privChecker.canRecursive (
				taskLogger,
				delegate,
				privCode);

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	void renderFormAlwaysHidden (
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
				"renderFormAlwaysHidden");

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

		htmlWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" id=\"%h-%h\"",
			formName,
			name (),
			" name=\"%h-%h\"",
			formName,
			name (),
			" value=\"%h\"",
			interfaceValue.isPresent ()
				? interfaceValue.get ().toString ()
				: "",
			">\n");

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
			@NonNull Map <String, Object> hints) {

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
	void renderFormRow (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormFieldSubmission submission,
			@NonNull FormatWriter htmlWriter,
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
			htmlWriter);

		htmlTableHeaderCellWrite (
			htmlWriter,
			label ());

		htmlTableCellOpen (
			htmlWriter);

		renderer.renderHtmlComplex (
			taskLogger,
			htmlWriter,
			container,
			hints,
			interfaceValue);

		htmlTableCellClose (
			htmlWriter);

		htmlTableRowClose (
			htmlWriter);

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

		Optional <String> csvValueOptional =
			resultValueRequired (
				csvMapping.genericToInterface (
					taskLogger,
					container,
					hints,
					genericValue));

		if (
			optionalIsNotPresent (
				csvValueOptional)
		) {

			throw new RuntimeException (
				stringFormat (
					"Missing CSV value for %s.%s",
					container.getClass ().getName (),
					name ()));

		}

		String csvValue =
			csvValueOptional.get ();

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
			@NonNull Map <String, Object> hints,
			@NonNull String formName) {

		return new UpdateResult <Generic, Native> ()

			.updated (
				false)

			.error (
				optionalAbsent ());

	}

	@Override
	public
	void runUpdateHook (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UpdateResult<Generic,Native> updateResult,
			@NonNull Container container,
			@NonNull PermanentRecord<?> linkObject,
			@NonNull Optional<Object> objectRef,
			@NonNull Optional<String> objectType) {

		doNothing ();

	}

}
