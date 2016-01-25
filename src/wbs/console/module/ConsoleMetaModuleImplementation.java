package wbs.console.module;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleContextExtensionPoint;
import wbs.console.context.ConsoleContextHint;
import wbs.console.context.ConsoleContextLink;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@DataClass ("console-meta-module")
@PrototypeComponent ("consoleMetaModule")
public
class ConsoleMetaModuleImplementation
	implements ConsoleMetaModule {

	// properties

	@DataChildren
	@Getter @Setter
	List<ConsoleContextExtensionPoint> extensionPoints =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List<ConsoleContextLink> contextLinks =
		new ArrayList<> ();

	@DataChildren
	@Getter @Setter
	List<ConsoleContextHint> contextHints =
		new ArrayList<> ();

	// property utils

	public
	ConsoleMetaModuleImplementation addExtensionPoint (
			@NonNull ConsoleContextExtensionPoint extensionPoint) {

		extensionPoints.add (
			extensionPoint);

		return this;

	}

	public
	ConsoleMetaModuleImplementation addContextLink (
			@NonNull ConsoleContextLink contextLink) {

		contextLinks.add (
			contextLink);

		return this;

	}

	public
	ConsoleMetaModuleImplementation addContextHint (
			@NonNull ConsoleContextHint contextHint) {

		contextHints.add (
			contextHint);

		return this;

	}

}
