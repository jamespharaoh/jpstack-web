package wbs.console.module;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.core.FormFieldSet;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;

import wbs.web.file.WebFile;
import wbs.web.responder.WebModule;

public
interface ConsoleModule
	extends WebModule {

	String name ();

	List <ConsoleContextType> contextTypes ();

	List <ConsoleContext> contexts ();

	List <ConsoleContextTab> tabs ();

	Map <String, List <ContextTabPlacement>> tabPlacementsByContextType ();

	Map <String, WebFile> contextFiles ();

	Map <String, List <String>> contextFilesByContextType ();

	Map <String, FormFieldSet <?>> formFieldSets ();

	Map <String, SupervisorConfig> supervisorConfigs ();

	// implementation

	default
	Optional <FormFieldSet <?>> formFieldSet (
			@NonNull String name) {

		return mapItemForKey (
			formFieldSets (),
			name);

	}

	default
	FormFieldSet <?> formFieldSetRequired (
			@NonNull String name) {

		return mapItemForKeyOrThrow (
			formFieldSets (),
			name,
			() -> new NoSuchElementException (
				stringFormat (
					"Console module '%s' ",
					name (),
					"has no field set: %s",
					name)));

	}

	default <Type>
	Optional <FormFieldSet <Type>> formFieldSet (
			@NonNull String name,
			@NonNull Class <?> containerClass) {

		Optional <FormFieldSet <?>> fieldsUncastOptional =
			formFieldSet (
				name);

		return optionalMapRequired (
			fieldsUncastOptional,
			fieldsUncast ->
				fieldsUncast.cast (
					containerClass));

	}

	default <Type>
	FormFieldSet <Type> formFieldSetRequired (
			@NonNull String name,
			@NonNull Class <Type> containerClass) {

		FormFieldSet <?> fieldsUncast =
			mapItemForKeyOrThrow (
				formFieldSets (),
				name,
				() -> new NoSuchElementException (
					stringFormat (
						"Console module '%s' ",
						name (),
						"has no field set: %s",
						name)));

		return fieldsUncast.cast (
			containerClass);

	}

}
