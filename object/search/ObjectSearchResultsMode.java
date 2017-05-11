package wbs.platform.object.search;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.console.forms.context.FormContextBuilder;

@Accessors (fluent = true)
@Data
public
class ObjectSearchResultsMode <Container> {

	String name;

	FormContextBuilder <Container> formContextBuilder;

}
