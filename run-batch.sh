#!/bin/sh

export BASEDIR=..

# setup classpath
CLASSPATH="bin"
for PROJ in \
		autoresponder \
		broadcast \
		chat \
		core \
		dbpool \
		dialogue \
		forwarder \
		g8wave \
		gsm \
		hybyte \
		jigsaw \
		kernel \
		locator \
		magicnumber \
		manualresponder \
		media \
		mediaburst \
		mig \
		modempoll \
		orderer \
		photograbber \
		php \
		rpc \
		smpp \
		sms \
		subscription \
		ticketer \
		utils \
		wappush; do
	CLASSPATH="$CLASSPATH:$BASEDIR/txt2-$PROJ/bin"
done
for filename in lib/*.jar; do
	CLASSPATH="$CLASSPATH:$filename";
done
export CLASSPATH

# run the interactive session
java \
	-classpath "$CLASSPATH" \
	-Dprogram.name="$PROGNAME" \
	-Dclassworlds.conf="$CLASSWORLDS_CONF" \
	-Djava.library.path=$HOME/.groovy/lib \
	-Xverify:none \
	-Djava.lang.SecurityManager \
	txt2.stuff.Batch \
	classpath:txt2/hades/hades-misc-beans.xml
