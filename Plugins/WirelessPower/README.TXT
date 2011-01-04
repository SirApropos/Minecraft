========================
WirelessPower Plugin for hMod (Created by Apropos <Apropos.of.nothing@gmx.com>)
========================

This plugin requires build 126+ of hMod.

[1.0] Installation Instructions
	[1.1] Setting up the MySQL table
	[1.2] WirelessPower.properties
[2.0] Usage

========================
[1.0]Installation Instructions
========================
Place WirelessPower.jar into Plugins folder. Add "WirelessPower" to plugins line of server.properties.
You must also set up a SQL table in your Minecraft database. See section 1.1 for details.
WirelessPower.properties will be created on the first time the plugin is enabled. See 1.2 for configuration details.

========================
[1.2]Setting up the MySQL table
========================
A .SQL file is included in WirelessPower.jar. Run the query in whatever administration you use for your SQL database.

========================
[1.2]WirelessPower.properties
========================
There are three types of entries that can be added. Vars, permissions or boosts.

Vars are in the format of "[var]=[value]".
ex:protect_transmitters=false

There are two vars that can currently be changed.
    -protect_transmitters (boolean, default false)       | Whether liquids can flow to the transmitter or
                                                         | players can destroy other's transmitters.
    -base_power (double, default 15.0)                   | The base range in meters of a transmitter.

Permissions are in the format of "perm:[user|group]:[name]:[type]:[value]".
ex:perm:group:admins:maxtransmitters:-1

[user|group] is whether the permissions applies to a user or group of the given name.
[name] is the name of the user or group.
[type] is the type of permission. There are currently two permissions: maxtransmitters and createchannel.
[value] is an integer. In the case of createchannel, 1 is true and 0 is false. In the case of maxtransmitters,
the value is the number of transmitters a player is allowed to have. Use -1 to allow for infinite transmitters.

Boosts are in the format of "boost:[blocktype]:[boost]".
ex: boost:89:2.0                                         |This will give transmitters placed on lightstone 2x
                                                         |the range.

[blocktype] is the integer type of the block you wish to apply a boost to.
[boost] is the number the base_power is multiplied by.

========================
[2.0]Usage
========================
To set up a transmitter, place a redstone torch on a block. On the block, place a sign in the following format:
Line 1: <Can be anything>
Line 2: [<type>]    |Where <type> is transmit, receive or repeat.
Line 3: <channel>   |Can be any string. Case insensitive.
line 4: [Private]   |Optional. If set, only the owner will be able to see the type and channel.

Ex:
Line 1: <blank>
Line 2: [Repeat]
Line 3: ChannelA
line 4: <blank>

This would create a repeater on channel "ChannelA".

Note that in order for a transmitter or repeater to transmit, the block it is on must be powered. This means
that the torch must be in the 'off' state. Receivers will be in the 'off' state unless powered by a transmitter
or repeater.