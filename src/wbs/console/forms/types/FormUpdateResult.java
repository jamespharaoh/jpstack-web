package wbs.console.forms.types;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class FormUpdateResult <Generic, Native> {

	Boolean updated;

	Optional <Generic> oldGenericValue;
	Optional <Generic> newGenericValue;

	Optional <Native> oldNativeValue;
	Optional <Native> newNativeValue;

	Optional <String> updatedFieldName;

	Optional <String> error;

}
