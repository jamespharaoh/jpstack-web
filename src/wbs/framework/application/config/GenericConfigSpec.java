package wbs.framework.application.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("config")
public
class GenericConfigSpec {

	@DataChildren (
		direct = true)
	List <GenericConfigItemSpec> items =
		new ArrayList <GenericConfigItemSpec> ();

}
