package wbs.platform.event.console;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.module.ConsoleModuleData;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("event-details-field")
@PrototypeComponent ("eventDetailsFormFieldSpec")
@ConsoleModuleData
public
class EventDetailsFormFieldSpec {

}
