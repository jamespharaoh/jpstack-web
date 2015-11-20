package wbs.platform.rpc.core;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public
class RpcList
	extends RpcElem {

	private final
	String memberName;

	private final
	RpcType memberType;

	private final
	List<RpcElem> members;

	public
	RpcList (
			String name,
			String newMemberName,
			RpcType newMemberType,
			RpcElem... newMembers) {

		super (
			name,
			RpcType.rList);

		memberName =
			newMemberName;

		memberType =
			newMemberType;

		members =
			new ArrayList<RpcElem> ();

		for (RpcElem member
				: newMembers) {

			add (
				member);

		}

	}

	public
	RpcList (
			String name,
			String newMemberName,
			RpcType newMemberType,
			Collection<RpcElem> newMembers) {

		super (
			name,
			RpcType.rList);

		memberName =
			newMemberName;

		memberType =
			newMemberType;

		members =
			new ArrayList<RpcElem> ();

		for (
			RpcElem member
				: newMembers
		) {

			add (
				member);

		}

	}

	public
	void add (
			RpcElem newMember) {

		if (! equal (
				newMember.getName (),
				memberName)) {

			throw new RuntimeException (
				stringFormat (
					"Member name should be: %s",
					memberName));

		}

		if (! equal (
				newMember.getType (),
				memberType)) {

			throw new RuntimeException (
				stringFormat (
					"Member type should be: %s",
					memberType));

		}

		members.add (
			newMember);

	}

	@Override
	public
	List<RpcElem> getValue () {

		return members;

	}

	@Override
	public
	List<Object> getNative () {

		List<Object> ret =
			new ArrayList<Object> ();

		for (
			RpcElem member
				: members
		) {

			ret.add (
				member.getNative ());

		}

		return ret;

	}

}
