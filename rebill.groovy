finished = false
loop_count = 0
while (! finished) {
	tx = db.rw ()
	try {
		sess = db.currentSession
		cus = sess.createQuery ("FROM ChatUserRec cu WHERE cu.rebillFlag = true").setMaxResults (100).list ()
		cus.each () { |cu|
			try {
				chatLogic.userBill (cu, true);
			} catch (Exception e) {
				throw new RuntimeException ("Error doing bill for " + cu.getId (), e);
			}
			cu.rebillFlag = false;
		}
		tx.commit ()
		if (cus.size () < 100) finished = true
	} catch (Exception e) {
		exceptionUtils.logException ("daemon", "rebill", e, null, false)
		e.printStackTrace ();
	} finally {
		tx.close ()
	}
	loop_count = loop_count + 1
	if (loop_count >= 1000) finished = true
}
