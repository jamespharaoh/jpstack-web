package wbs.console.forms.core;

import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;
import wbs.console.forms.types.FormUpdateResultSet;
import wbs.console.html.ScriptRef;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;

import wbs.utils.string.FormatWriter;

public
interface ConsoleForm <Container> {

	// property accessors

	ConsoleRequestContext requestContext ();

	UserPrivChecker privChecker ();

	FormFieldSet <Container> columnFields ();
	FormFieldSet <Container> rowFields ();
	FormFieldSet <Container> allFields ();

	String formName ();

	FormType formType ();

	Map <String, Object> hints ();

	FormFieldSubmission submission ();

	Class <Container> containerClass ();

	List <Container> values ();

	default
	Container value () {

		return iterableOnlyItemRequired (
			values ());

	}

	FormUpdateResultSet updateResultSet ();

	default
	Boolean errors () {
		return updateResultSet ().errors ();
	}

	default
	long errorCount () {
		return updateResultSet ().errorCount ();
	}

	default
	Boolean updates () {
		return updateResultSet ().updates ();
	}

	default
	Boolean fileUpload () {
		return allFields ().fileUpload ();
	}

	default
	Set <ScriptRef> scriptRefs () {
		return allFields ().scriptRefs ();
	}

	// property setters

	ConsoleForm <Container> requestContext (
			ConsoleRequestContext requestContext);

	ConsoleForm <Container> privChecker (
			UserPrivChecker privChecker);

	ConsoleForm <Container> columnFields (
			FormFieldSet <Container> columnFields);

	ConsoleForm <Container> rowFields (
			FormFieldSet <Container> rowFields);

	ConsoleForm <Container> formName (
			String formName);

	ConsoleForm <Container> formType (
			FormType formType);

	ConsoleForm <Container> values (
			List <Container> values);

	ConsoleForm <Container> hints (
			Map <String, Object> hints);

	ConsoleForm <Container> submission (
			FormFieldSubmission submission);

	ConsoleForm <Container> updateResultSet (
			FormUpdateResultSet updateResultSet);

	// operations

	void implicit (
			Transaction parentTransaction,
			Container object);

	default
	void implicit (
			@NonNull Transaction parentTransaction) {

		implicit (
			parentTransaction,
			value ());

	}

	void setDefaults (
			Transaction parentTransaction,
			Container object);

	default
	void setDefaults (
			Transaction parentTransaction) {

		setDefaults (
			parentTransaction,
			value ());

	}

	void update (
			Transaction parentTransaction,
			Container object);

	default
	void update (
			@NonNull Transaction parentTransaction) {

		update (
			parentTransaction,
			value ());

	}

	void reportErrors (
			Transaction parentTransaction);

	void runUpdateHooks (
			Transaction parentTransaction,
			Container object,
			PermanentRecord <?> linkObject,
			Optional <Object> objectRef,
			Optional <String> objectType);

	default
	void runUpdateHooks (
			@NonNull Transaction parentTransaction,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		runUpdateHooks (
			parentTransaction,
			value (),
			linkObject,
			objectRef,
			objectType);

	}

	void outputTableHeadings (
			Transaction parentTransaction,
			FormatWriter formatWriter);

	void outputCsvHeadings (
			Transaction parentTransaction,
			FormatWriter formatWriter);

	void outputFormAlwaysHidden (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	default
	void outputFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		outputFormAlwaysHidden (
			parentTransaction,
			formatWriter,
			value ());

	}

	void outputFormTemporarilyHidden (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	default
	void outputFormTemporarilyHidden (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		outputFormTemporarilyHidden (
			parentTransaction,
			formatWriter,
			value ());

	}

	void outputFormRows (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	default
	void outputFormRows (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		outputFormRows (
			parentTransaction,
			formatWriter,
			value ());

	}

	void outputFormReset (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	void outputFormTable (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object,
			String method,
			String actionUrl,
			String submitButtonLabel);

	default
	void outputFormTable (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull String method,
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel) {

		outputFormTable (
			parentTransaction,
			formatWriter,
			value (),
			method,
			actionUrl,
			submitButtonLabel);

	}

	void outputDetailsTable (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	default
	void outputDetailsTable (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		outputDetailsTable (
			parentTransaction,
			formatWriter,
			value ());

	}

	void outputListTable (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Boolean links);

	void outputCsvRow (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	void outputTableCellsList (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object,
			Boolean links);

	default
	void outputTableCellsList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean links) {

		outputTableCellsList (
			parentTransaction,
			formatWriter,
			value (),
			links);

	}

	void outputTableRowsList (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object,
			Boolean links,
			Long columnSpan);

	void outputTableRows (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			Container object);

	default
	void outputTableRows (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		outputTableRows (
			parentTransaction,
			formatWriter,
			value ());

	}

	void outputFormDebug (
			Transaction parentTransaction,
			FormatWriter frmatWriter);

	Map <String, List <String>> exceptionDetails ();

}
