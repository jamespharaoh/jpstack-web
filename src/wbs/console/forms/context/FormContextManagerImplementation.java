package wbs.console.forms.context;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.string.StringUtils.capitalise;
import static wbs.utils.string.StringUtils.hyphenToCamel;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.forms.types.FormType;
import wbs.console.module.ConsoleModule;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
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

	// prototype dependencies

	@PrototypeDependency
	Provider <FormContextBuilder <?>> formContextBuilderProvider;

	// public implementation

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

	@Override
	public <Type>
	FormContextBuilder <Type> createFormContextBuilder (
			@NonNull ConsoleModule consoleModule,
			@NonNull String formName,
			@NonNull Class <Type> containerClass,
			@NonNull FormType formType,
			@NonNull Optional <String> columnFieldsNameOptional,
			@NonNull Optional <String> rowFieldsNameOptional) {

		return genericCastUnchecked (
			formContextBuilderProvider.get ()

			.formName (
				formName)

			.objectClass (
				genericCastUnchecked (
					containerClass))

			.formType (
				formType)

			.columnFields (
				optionalOrNull (
					optionalMapRequired (
						columnFieldsNameOptional,
						columnFieldsName ->
							genericCastUnchecked (
								consoleModule.formFieldSetRequired (
									columnFieldsName)))))

			.rowFields (
				optionalOrNull (
					optionalMapRequired (
						rowFieldsNameOptional,
						rowFieldsName ->
							genericCastUnchecked (
								consoleModule.formFieldSetRequired (
									rowFieldsName)))))

		);

	}

}
