package wbs.console.forms;

import java.util.Map;

import com.google.common.base.Optional;

import fj.data.Either;

public
interface FormFieldInterfaceMapping <Container, Generic, Interface> {

	Either <Optional <Generic>, String> interfaceToGeneric (
			Container container,
			Map <String, Object> hints,
			Optional <Interface> interfaceValue);

	Either <Optional <Interface>, String> genericToInterface (
			Container container,
			Map <String, Object> hints,
			Optional <Generic> genericValue);

}
