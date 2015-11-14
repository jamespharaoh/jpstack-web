package wbs.platform.service.metamodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("service-types")
@PrototypeComponent ("serviceTypesSpec")
@ModelMetaData
public
class ServiceTypesSpec {

	@DataChildren (
		direct = true)
	List<ServiceTypeSpec> serviceTypes =
		new ArrayList<ServiceTypeSpec> ();

}
