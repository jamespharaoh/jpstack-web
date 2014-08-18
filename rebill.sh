#!/bin/bash

dir=`dirname $0`
cd $dir

test $# = 1 || {
	echo "Must supply single digit param"
	exit 1
}
code_prefix=$1

dow=`date +%w`
case $dow in
	5) time='6 months' ;;
	*) time='2 weeks' ;;
esac

psql txt2 <<-EOF

	BEGIN;

	UPDATE chat_user
	SET rebill_flag = false
	WHERE rebill_flag;

	UPDATE chat_user
	SET rebill_flag = true
	WHERE last_action >= now () - '$time'::interval
	  AND type = 'u'
	  AND number_id IS NOT NULL
	  AND credit_mode = 1
	  AND code LIKE '$code_prefix%';

	COMMIT;

EOF

./run-batch.sh < rebill.groovy
