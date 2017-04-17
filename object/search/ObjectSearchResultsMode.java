package wbs.platform.object.search;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldSet;

@Accessors (fluent = true)
@Data
public
class ObjectSearchResultsMode <Container> {

	String name;

	Optional <FormFieldSet <Container>> columns;
	Optional <FormFieldSet <Container>> rows;

}
