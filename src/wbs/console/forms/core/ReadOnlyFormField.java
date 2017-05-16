package wbs.console.forms.core;

import static wbs.utils.collection.CollectionUtils.collectionHasOneItem;
import static wbs.utils.collection.CollectionUtils.collectionHasTwoItems;
import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.Misc.eitherGetLeft;
import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.ResultUtils.resultValueRequired;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringSplitColon;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.feature.FeatureChecker;
import wbs.console.forms.types.ConsoleFormNativeMapping;
import wbs.console.forms.types.FormField;
import wbs.console.forms.types.FormFieldAccessor;
import wbs.console.forms.types.FormFieldInterfaceMapping;
import wbs.console.forms.types.FormFieldRenderer;
import wbs.console.forms.types.FormUpdateResult;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

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
	ConsoleFormNativeMapping<Container,Generic,Native> nativeMapping;

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
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canView");

		) {

			// check feature

			if (

				isNotNull (
					featureCode)

				&& ! featureChecker.checkFeatureAccess (
					transaction,
					privChecker,
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
				collectionHasOneItem (
					privParts)
			) {

				String privCode =
					privParts.get (0);

				return privChecker.canRecursive (
					transaction,
					(Record<?>) container,
					privCode);

			} else if (
				collectionHasTwoItems (
					privParts)
			) {

				String delegatePath =
					privParts.get (0);

				String privCode =
					privParts.get (1);

				Record <?> delegate =
					genericCastUnchecked (
						objectManager.dereferenceRequired (
							transaction,
							container,
							delegatePath,
							context.hints ()));

				return privChecker.canRecursive (
					transaction,
					delegate,
					privCode);

			} else {

				throw new RuntimeException ();

			}

		}

	}

	@Override
	public
	void renderFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormAlwaysHidden");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			context.formatWriter ().writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" id=\"%h-%h\"",
				context.formName (),
				name (),
				" name=\"%h-%h\"",
				context.formName (),
				name (),
				" value=\"%h\"",
				interfaceValue.isPresent ()
					? interfaceValue.get ().toString ()
					: "",
				">\n");

		}

	}

	@Override
	public
	void renderTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container,
			@NonNull Boolean link,
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTableCellList");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			renderer.renderHtmlTableCellList (
				transaction,
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue,
				link,
				columnSpan);

		}

	}

	@Override
	public
	void renderTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container,
			@NonNull Long columnSpan) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderTableCellProperties");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			renderer.renderHtmlTableCellProperties (
				transaction,
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue,
				true,
				columnSpan);

		}

	}

	@Override
	public
	void renderFormRow (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container,
			@NonNull Optional <String> error) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderFormRow");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <Interface> interfaceValue =
				requiredValue (
					eitherGetLeft (
						interfaceMapping.genericToInterface (
							transaction,
							container,
							context.hints (),
							genericValue)));

			htmlTableRowOpen (
				context.formatWriter ());

			htmlTableHeaderCellWrite (
				context.formatWriter (),
				label ());

			htmlTableCellOpen (
				context.formatWriter ());

			renderer.renderHtmlComplex (
				transaction,
				context.formatWriter (),
				container,
				context.hints (),
				interfaceValue);

			htmlTableCellClose (
				context.formatWriter ());

			htmlTableRowClose (
				context.formatWriter ());

		}

	}

	@Override
	public
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container) {

		doNothing ();

	}

	@Override
	public
	void renderCsvRow (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderCsvRow");

		) {

			Optional <Native> nativeValue =
				requiredValue (
					accessor.read (
						transaction,
						container));

			Optional <Generic> genericValue =
				requiredValue (
					nativeMapping.nativeToGeneric (
						transaction,
						container,
						nativeValue));

			Optional <String> csvValueOptional =
				resultValueRequired (
					csvMapping.genericToInterface (
						transaction,
						container,
						context.hints (),
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

			context.formatWriter ().writeFormat (
				"\"%s\"",
				csvValue.replace (
					"\"",
					"\"\""));

		}

	}

	@Override
	public
	FormUpdateResult <Generic, Native> update (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update");

		) {

			return new FormUpdateResult <Generic, Native> ()

				.updated (
					false)

				.error (
					optionalAbsent ());

		}

	}

	@Override
	public
	void runUpdateHook (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container container,
			@NonNull FormUpdateResult <Generic, Native> updateResult,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		doNothing ();

	}

}
