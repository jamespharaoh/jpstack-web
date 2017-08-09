package wbs.console.forms.core;

import com.google.common.collect.ImmutableMap;

import wbs.console.helper.core.ConsoleHelper;

import wbs.framework.database.Transaction;

public
interface ConsoleFormHintsLogic {

	void prepareParentHints (
			Transaction parentTransaction,
			ImmutableMap.Builder <String, Object> formHintsBuilder,
			ConsoleHelper <?> objectHelper);

	void prepareGrandparentHints (
			Transaction parentTransaction,
			ImmutableMap.Builder <String, Object> formHintsBuilder,
			ConsoleHelper <?> objectHelper,
			ConsoleHelper <?> parentHelper);

	void prepareGreatGrandparentHints (
			Transaction parentTransaction,
			ImmutableMap.Builder <String, Object> formHintsBuilder,
			ConsoleHelper <?> objectHelper,
			ConsoleHelper <?> parentHelper,
			ConsoleHelper <?> grandparentHelper);

}
