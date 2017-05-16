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

	FormatWriter formatWriter ();

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

	ConsoleForm <Container> formatWriter (
			FormatWriter formatWriter);

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
			Transaction parentTransaction);

	void outputCsvHeadings (
			Transaction parentTransaction);

	void outputFormAlwaysHidden (
			Transaction parentTransaction,
			Container object);

	default
	void outputFormAlwaysHidden (
			@NonNull Transaction parentTransaction) {

		outputFormAlwaysHidden (
			parentTransaction,
			value ());

	}

	void outputFormTemporarilyHidden (
			Transaction parentTransaction,
			Container object);

	default
	void outputFormTemporarilyHidden (
			@NonNull Transaction parentTransaction) {

		outputFormTemporarilyHidden (
			parentTransaction,
			value ());

	}

	void outputFormRows (
			Transaction parentTransaction,
			Container object);

	default
	void outputFormRows (
			@NonNull Transaction parentTransaction) {

		outputFormRows (
			parentTransaction,
			value ());

	}

	void outputFormReset (
			Transaction parentTransaction,
			Container object);

	void outputFormTable (
			Transaction parentTransaction,
			Container object,
			String method,
			String actionUrl,
			String submitButtonLabel);

	default
	void outputFormTable (
			@NonNull Transaction parentTransaction,
			@NonNull String method,
			@NonNull String actionUrl,
			@NonNull String submitButtonLabel) {

		outputFormTable (
			parentTransaction,
			value (),
			method,
			actionUrl,
			submitButtonLabel);

	}

	void outputDetailsTable (
			Transaction parentTransaction,
			Container object);

	default
	void outputDetailsTable (
			@NonNull Transaction parentTransaction) {

		outputDetailsTable (
			parentTransaction,
			value ());

	}

	void outputListTable (
			Transaction parentTransaction,
			Boolean links);

	void outputCsvRow (
			Transaction parentTransaction,
			Container object);

	void outputTableCellsList (
			Transaction parentTransaction,
			Container object,
			Boolean links);

	default
	void outputTableCellsList (
			@NonNull Transaction parentTransaction,
			@NonNull Boolean links) {

		outputTableCellsList (
			parentTransaction,
			value (),
			links);

	}

	void outputTableRowsList (
			Transaction parentTransaction,
			Container object,
			Boolean links,
			Long columnSpan);

	void outputTableRows (
			Transaction parentTransaction,
			Container object);

	default
	void outputTableRows (
			@NonNull Transaction parentTransaction) {

		outputTableRows (
			parentTransaction,
			value ());

	}

	void outputFormDebug (
			Transaction parentTransaction);

	Map <String, List <String>> exceptionDetails ();

}
