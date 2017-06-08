package wbs.console.forms.core;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("consoleMultiForm")
@Accessors (fluent = true)
public
class ConsoleMultiForm <Container> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Container value;

	@Getter @Setter
	Map <String, ConsoleForm <Container>> forms;

	// accessors

	public
	ConsoleForm <Container> context (
			@NonNull String name) {

		return mapItemForKeyRequired (
			forms,
			name);

	}

	public
	Boolean errors () {

		return forms.values ().stream ()

			.anyMatch (
				ConsoleForm::errors)

		;

	}

	public
	long errorCount () {

		return forms.values ().stream ()

			.mapToLong (
				form ->
					form.errorCount ())

			.sum ();

	}

	// public implementation

	public
	void outputFormAlwaysHidden (
			@NonNull Transaction parentTransaction,
			@NonNull String ... names) {

		for (
			String name
				: names
		) {

			context (name).outputFormAlwaysHidden (
				parentTransaction,
				value);

		}

	}

	public
	void outputFormRows (
			@NonNull Transaction parentTransaction,
			@NonNull String ... names) {

		for (
			String name
				: names
		) {

			context (name).outputFormRows (
				parentTransaction,
				value);

		}

	}

	public
	void outputFormTemporarilyHidden (
			@NonNull Transaction parentTransaction,
			@NonNull String ... names) {

		for (
			String name
				: names
		) {

			context (name).outputFormTemporarilyHidden (
				parentTransaction,
				value);

		}

	}

	public
	void reportErrors (
			@NonNull Transaction parentTransaction) {

		forms.values ().forEach (
			context ->
				context.reportErrors (
					parentTransaction));

	}

	public
	void update (
			@NonNull Transaction parentTransaction,
			@NonNull String ... names) {

		for (
			String name
				: names
		) {

			context (name).update (
				parentTransaction,
				value);

		}

	}

}
