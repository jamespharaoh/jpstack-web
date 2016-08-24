package wbs.console.forms;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.successResult;

import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import fj.data.Either;

import wbs.console.helper.ConsoleObjectManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.entity.record.Record;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;

@Accessors (fluent = true)
@PrototypeComponent ("objectCsvFormFieldInterfaceMapping")
public
class ObjectCsvFormFieldInterfaceMapping<Container,Generic extends Record<Generic>>
	implements FormFieldInterfaceMapping<Container,Generic,String> {

	// dependencies

	@Inject
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
			isNotPresent (
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
