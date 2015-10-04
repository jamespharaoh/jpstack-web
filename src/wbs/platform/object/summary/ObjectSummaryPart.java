package wbs.platform.object.summary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;

@Accessors (fluent = true)
@PrototypeComponent ("objectSummaryPart")
public
class ObjectSummaryPart
	extends AbstractPagePart {

	@Getter @Setter
	List<Provider<PagePart>> partFactories;

	List<PagePart> parts;

	@Override
	public
	void prepare () {

		parts =
			new ArrayList<PagePart> ();

		for (Provider<PagePart> partFactory
				: partFactories) {

			PagePart pagePart =
				partFactory.get ();

			pagePart.setup (
				Collections.<String,Object>emptyMap ());

			pagePart.prepare ();

			parts.add (pagePart);

		}

	}

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		Set<ScriptRef> scriptRefs =
			new LinkedHashSet<ScriptRef> ();

		for (PagePart pagePart : parts) {

			scriptRefs.addAll (
				pagePart.scriptRefs ());

		}

		return scriptRefs;

	}

	@Override
	public
	void renderHtmlHeadContent () {

		for (PagePart part : parts)
			part.renderHtmlHeadContent ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		for (PagePart part : parts)
			part.renderHtmlBodyContent ();

	}

}
