package wbs.console.part;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.database.Transaction;

@Accessors (fluent = true)
public
class ProviderPagePartFactory
	implements PagePartFactory {

	// properties

	@Getter @Setter
	Provider <PagePart> pagePartProvider;

	// implementation

	@Override
	public
	PagePart buildPagePart (
			@NonNull Transaction parentTransaction) {

		return pagePartProvider.get ();

	}

}
