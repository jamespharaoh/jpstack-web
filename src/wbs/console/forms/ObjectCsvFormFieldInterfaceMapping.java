package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.successResult;
import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

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

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String rootFieldName;

	// implementation

	@Override
	public
	Either<Optional<Generic>,String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<Generic> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {

			return successResult (
				Optional.<String>of (
					""));

		} else {

			Optional<Record<?>> root;

			if (
				isNotNull (
					rootFieldName)
			) {

				root =
					Optional.<Record<?>>of (
						(Record<?>)
						objectManager.dereference (
							container,
							rootFieldName));

			} else {

				root =
					Optional.<Record<?>>absent ();

			}

			return successResult (
				Optional.of (
					objectManager.objectPathMini (
						genericValue.get (),
						root)));

		}

	}

}
