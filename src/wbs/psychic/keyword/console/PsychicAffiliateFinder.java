package wbs.psychic.keyword.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.forms.EntityFinder;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.psychic.affiliate.console.PsychicAffiliateConsoleHelper;
import wbs.psychic.affiliate.model.PsychicAffiliateRec;
import wbs.psychic.core.console.PsychicConsoleHelper;
import wbs.psychic.core.model.PsychicRec;

@SingletonComponent ("psychicAffiliateFinder")
public
class PsychicAffiliateFinder
	implements EntityFinder<PsychicAffiliateRec> {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	PsychicAffiliateConsoleHelper psychicAffiliateHelper;

	@Inject
	PsychicConsoleHelper psychicHelper;

	@Override
	public
	PsychicAffiliateRec findEntity (
			int id) {

		PsychicRec psychic =
			psychicHelper.find (
				requestContext.stuffInt ("psychicId"));

		PsychicAffiliateRec affiliate =
			psychicAffiliateHelper.find (id);

		if (affiliate.getPsychic () != psychic) {

			throw new RuntimeException (
				stringFormat (
					"%s != %s",
					affiliate.getPsychic (),
					psychic));

		}

		return affiliate;

	}

	@Override
	public
	List<PsychicAffiliateRec> findEntities () {

		PsychicRec psychic =
			psychicHelper.find (
				requestContext.stuffInt ("psychicId"));

		return psychicAffiliateHelper.findByPsychic (
			psychic.getId ());

	}

}
