package wbs.console.forms.types;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

public
interface ConsoleFormNativeMapping <Container, Generic, Native> {

	Optional <Generic> nativeToGeneric (
			Transaction parentTransaction,
			Container container,
			Optional <Native> nativeValue);

	Optional <Native> genericToNative (
			Transaction parentTransaction,
			Container container,
			Optional <Generic> genericValue);

}
