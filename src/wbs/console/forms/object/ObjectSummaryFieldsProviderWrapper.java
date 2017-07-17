package wbs.console.forms.object;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FieldsProvider;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@Accessors (fluent = true)
public
class ObjectSummaryFieldsProviderWrapper <
	Container extends Record <Container>,
	Parent extends Record <Parent>
>
	implements FieldsProvider <Container, Parent> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	ComponentProvider <ObjectFieldsProvider <Container, Parent>>
		objectFieldsProviderProvider;

	// state

	ObjectFieldsProvider <Container, Parent> wrappedProvider;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			wrappedProvider =
				objectFieldsProviderProvider.provide (
					taskLogger);

		}

	}

	// details

	@Override
	public
	Class <Container> containerClass () {
		return wrappedProvider.containerClass ();
	}

	@Override
	public
	Class <Parent> parentClass () {
		return wrappedProvider.parentClass ();
	}

	// public implementation

	@Override
	public
	FormFieldSetPair <Container> getStaticFields (
			@NonNull Transaction parentTransaction) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSetPair <Container> getFieldsForParent (
			@NonNull Transaction parentTransaction,
			@NonNull Parent parent) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	FormFieldSetPair <Container> getFieldsForObject (
			@NonNull Transaction parentTransaction,
			@NonNull Container container) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getFieldsForObject");

		) {

			return wrappedProvider.getSummaryFields (
				transaction,
				container);

		}

	}

}
