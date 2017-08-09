package wbs.console.forms.object;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
@ToString
public
class ObjectFormFieldSearchResponseItem
	implements Comparable <ObjectFormFieldSearchResponseItem> {

	@DataAttribute
	Long objectId;

	@DataAttribute
	String path;

	@DataAttribute
	String code;

	@DataAttribute
	String name;

	@DataAttribute
	String description;

	@Override
	public
	int compareTo (
			@NonNull ObjectFormFieldSearchResponseItem other) {

		return new CompareToBuilder ()

			.append (
				path (),
				other.path ())

			.append (
				objectId (),
				other.objectId ())

			.build ()

		;

	}

}
