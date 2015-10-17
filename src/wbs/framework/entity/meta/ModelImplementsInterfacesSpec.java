package wbs.framework.entity.meta;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("implements-interfaces")
@PrototypeComponent ("modelImplementsInterfacesSpec")
@ModelMetaData
public
class ModelImplementsInterfacesSpec {

	@DataChildren (
		direct = true)
	List<ModelImplementsInterfaceSpec> implementsInterfaces =
		new ArrayList<ModelImplementsInterfaceSpec> ();

}
