package wbs.ticket.model;

public 
enum TicketStateState {

	submitted, // the user just submitted the ticket
	accepted, // an operator saw accepted the ticket
	pending, // an operator read the ticket and starts solving the issue
	solved, // the operator solved the issue
	closed; // the user agrees with the solution and the ticket is closed
	
}
