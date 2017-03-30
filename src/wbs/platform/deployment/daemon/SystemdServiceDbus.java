package wbs.platform.deployment.daemon;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusInterfaceName;
import org.freedesktop.dbus.DBusMemberName;

@DBusInterfaceName ("org.freedesktop.systemd1.Service")  
public
interface SystemdServiceDbus
	extends DBusInterface {

}
