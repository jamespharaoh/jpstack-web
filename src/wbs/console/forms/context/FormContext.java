package wbs.console.forms.context;

import static wbs.utils.collection.IterableUtils.iterableOnlyItemRequired;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.core.FormFieldSet;
import wbs.console.forms.types.FormFieldSubmission;
import wbs.console.forms.types.FormType;
import wbs.console.forms.types.FormUpdateResultSet;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;

import wbs.utils.string.FormatWriter;

public
interface FormContext <Container> {

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

	List <Container> objects ();

	default
	Container object () {

		return iterableOnlyItemRequired (
			objects ());

	}

	FormUpdateResultSet updateResultSet ();

	default
	Boolean errors () {
		return updateResultSet ().updates ();
	}

	default
	Boolean updates () {
		return updateResultSet ().updates ();
	}

	default
	Boolean fileUpload () {
		return allFields ().fileUpload ();
	}

	// property setters

	FormContext <Container> requestContext (
			ConsoleRequestContext requestContext);

	FormContext <Container> privChecker (
			UserPrivChecker privChecker);

	FormContext <Container> columnFields (
			FormFieldSet <Container> columnFields);

	FormContext <Container> rowFields (
			FormFieldSet <Container> rowFields);

	FormContext <Container> formName (
			String formName);

	FormContext <Container> formType (
			FormType formType);

	FormContext <Container> objects (
			List <Container> objects);

	FormContext <Container> hints (
			Map <String, Object> hints);

	FormContext <Container> submission (
			FormFieldSubmission submission);

	FormContext <Container> formatWriter (
			FormatWriter formatWriter);

	FormContext <Container> updateResultSet (
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
			object ());

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
			object ());

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
			object (),
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
			object ());

	}

	void outputFormTemporarilyHidden (
			Transaction parentTransaction,
			Container object);

	default
	void outputFormTemporarilyHidden (
			@NonNull Transaction parentTransaction) {

		outputFormTemporarilyHidden (
			parentTransaction,
			object ());

	}

	void outputFormRows (
			Transaction parentTransaction,
			Container object);

	default
	void outputFormRows (
			@NonNull Transaction parentTransaction) {

		outputFormRows (
			parentTransaction,
			object ());

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
			object (),
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
			object ());

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
			object (),
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
			object ());

	}

	void outputFormDebug (
			Transaction parentTransaction);

	Map <String, List <String>> exceptionDetails ();

}
