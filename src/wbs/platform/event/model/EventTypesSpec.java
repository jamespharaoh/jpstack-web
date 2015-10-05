package wbs.platform.event.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("event-types")
@PrototypeComponent ("eventTypesSpec")
@ModelMetaData
public
class EventTypesSpec {

	@DataChildren (
		direct = true)
	List<EventTypeSpec> eventTypes =
		new ArrayList<EventTypeSpec> ();

}
