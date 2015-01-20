package wbs.platform.api.resource;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.platform.api.module.ApiModuleData;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = "name")
@ToString (of = "name")
@DataClass ("resource")
@PrototypeComponent ("apiResourceSpec")
@ApiModuleData
public
class ApiResourceSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataChildren (
		direct = true)
	List<Object> builders =
		new ArrayList<Object> ();

}
