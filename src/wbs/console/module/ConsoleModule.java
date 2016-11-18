package wbs.console.module;

import static wbs.utils.collection.MapUtils.mapItemForKeyOrThrow;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.context.ConsoleContextType;
import wbs.console.forms.FormFieldSet;
import wbs.console.supervisor.SupervisorConfig;
import wbs.console.tab.ConsoleContextTab;
import wbs.console.tab.ContextTabPlacement;

import wbs.web.file.WebFile;
import wbs.web.responder.Responder;
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

	Map <String, Provider <Responder>> responders ();

	Map <String, FormFieldSet <?>> formFieldSets ();

	Map <String, SupervisorConfig> supervisorConfigs ();

	// implementation

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
	FormFieldSet <Type> formFieldSet (
			@NonNull String name,
			@NonNull Class <?> containerClass) {

		FormFieldSet <?> fieldsUncast =
			formFieldSetRequired (
				name);

		return fieldsUncast.cast (
			containerClass);

	}

}
