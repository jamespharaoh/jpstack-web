package wbs.console.forms;

import com.google.common.base.Optional;

import fj.data.Either;

public
interface FormFieldInterfaceMapping<Container,Generic,Interface> {

	Either<Optional<Generic>,String> interfaceToGeneric (
			Container container,
			Optional<Interface> interfaceValue);

	Either<Optional<Interface>,String> genericToInterface (
			Container container,
			Optional<Generic> genericValue);

}
