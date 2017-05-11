package wbs.console.forms.object;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldNativeMapping;
import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

@Accessors (fluent = true)
@PrototypeComponent ("objectIdFormFieldNativeMapping")
public
class ObjectIdFormFieldNativeMapping
		<Container, RecordType extends Record <RecordType>>
	implements FormFieldNativeMapping <Container, RecordType, Long> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	ConsoleHelper <RecordType> consoleHelper;

	// implementation

	@Override
	public
	Optional <RecordType> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <Long> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nativeToGeneric");

		) {

			if (! nativeValue.isPresent ()) {
				return optionalAbsent ();
			}

			Long objectId =
				(Long)
				nativeValue.get ();

			return optionalOf (
				consoleHelper.findRequired (
					transaction,
					objectId));

		}

	}

	@Override
	public
	Optional <Long> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <RecordType> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToNative");

		) {

			if (! genericValue.isPresent ()) {
				return optionalAbsent ();
			}

			return optionalOf (
				genericValue.get ().getId ());

		}

	}

}
