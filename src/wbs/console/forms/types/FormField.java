package wbs.console.forms.types;

import static wbs.utils.etc.Misc.doNothing;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.core.ConsoleForm;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.PermanentRecord;

import wbs.utils.string.FormatWriter;

public
interface FormField <Container, Generic, Native, Interface>
	extends FormItem <Container> {

	final static
	int defaultSize = 48;

	Boolean fileUpload ();
	Boolean large ();

	@Override
	default
	boolean canView (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> formContext,
			@NonNull Container object) {

		return true;

	}

	default
	void renderTableCellList (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container object,
			@NonNull Boolean link,
			@NonNull Long columnSpan) {

		doNothing ();

	}

	default
	void renderTableCellProperties (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container object,
			@NonNull Long columnSpan) {

		doNothing ();

	}

	default
	void renderFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container object) {

		doNothing ();

	}

	default
	void renderFormTemporarilyHidden (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container object) {

		doNothing ();

	}

	default
	void renderFormRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container container,
			@NonNull Optional <String> error) {

		doNothing ();

	}

	default
	void renderFormReset (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container container) {

		doNothing ();

	}

	default
	void renderCsvRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container object) {

		doNothing ();

	}

	default
	void implicit (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> form,
			@NonNull Container object) {

		doNothing ();

	}

	default
	void setDefault (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container object) {

		doNothing ();

	}

	default
	FormUpdateResult <Generic, Native> update (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container object) {

		throw new UnsupportedOperationException ();

	}

	default
	void runUpdateHook (
			@NonNull Transaction parentTransaction,
			@NonNull ConsoleForm <Container> context,
			@NonNull Container object,
			@NonNull FormUpdateResult <Generic, Native> result,
			@NonNull PermanentRecord <?> linkObject,
			@NonNull Optional <Object> objectRef,
			@NonNull Optional <String> objectType) {

		doNothing ();

	}

	// data

	public static
	enum Align {
		left,
		center,
		right;
	}

}
