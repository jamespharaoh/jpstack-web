package wbs.platform.event.model;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("event-type")
@PrototypeComponent ("eventTypeSpec")
@ModelMetaData
public
class EventTypeSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String text;

}
