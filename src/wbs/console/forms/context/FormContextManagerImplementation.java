package wbs.console.forms.context;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;

@SingletonComponent ("formContextManager")
public
class FormContextManagerImplementation
	implements FormContextManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, FormContextBuilder <?>> formContextBuilders;

	@ClassSingletonDependency
	LogContext logContext;

	// spublic implementation

	@Override
	public <Type>
	Optional <FormContextBuilder <Type>> formContextBuilder (
			@NonNull String consoleModuleName,
			@NonNull String name,
			@NonNull Class <Type> containerClass) {

		return genericCastUnchecked (
			mapItemForKey (
				formContextBuilders,
					stringFormat (
						"%s%sFormContextBuilder",
						consoleModuleName,
						capitalise (
							hyphenToCamel (
								name)))));

	}

}
