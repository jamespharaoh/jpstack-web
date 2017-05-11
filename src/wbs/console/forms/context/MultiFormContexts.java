package wbs.console.forms.context;

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

@PrototypeComponent ("multiFormContexts")
@Accessors (fluent = true)
public
class MultiFormContexts <Container> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	Container object;

	@Getter @Setter
	Map <String, FormContext <Container>> formContexts;

	// accessors

	public
	FormContext <Container> context (
			@NonNull String name) {

		return mapItemForKeyRequired (
			formContexts,
			name);

	}

	public
	Boolean errors () {

		return formContexts.values ().stream ()

			.anyMatch (
				FormContext::errors)

		;

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
				object);

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
				object);

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
				object);

		}

	}

	public
	void reportErrors (
			@NonNull Transaction parentTransaction) {

		formContexts.values ().forEach (
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
				object);

		}

	}

}
