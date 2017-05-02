package wbs.console.forms;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("objectCsvFormFieldInterfaceMapping")
public
class ObjectCsvFormFieldInterfaceMapping <
	Container,
	Generic extends Record <Generic>
>
	implements FormFieldInterfaceMapping <Container, Generic, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String rootFieldName;

	// implementation

	@Override
	public
	Either <Optional <String>, String> genericToInterface (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Map <String, Object> hints,
			@NonNull Optional <Generic> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToInterface");

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {

				return successResult (
					optionalOf (
						""));

			} else {

				Optional <Record <?>> root;

				if (
					isNotNull (
						rootFieldName)
				) {

					root =
						optionalOf (
							genericCastUnchecked (
								objectManager.dereferenceObsolete (
									transaction,
									container,
									rootFieldName)));

				} else {

					root =
						optionalAbsent ();

				}

				return successResult (
					optionalOf (
						objectManager.objectPathMini (
							transaction,
							genericValue.get (),
							root)));

			}

		}

	}

}
