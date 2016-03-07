package wbs.applications.imchat.model;

public
enum ImChatPurchaseState {

	unknown,

	creating,
	created,
	createFailed,

	retrieving,
	retrieved,
	retrieveFailed,

	confirming,
	comfirmed,
	confirmFailed,

	cancelled;

}
