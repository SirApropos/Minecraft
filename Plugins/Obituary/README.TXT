========================
Obituary Plugin for hMod (Created by Apropos <sir.apropos.of.nothign@gmail.com>)
========================

This plugin requires build 131+ of hMod.

[1.0] Installation Instructions
	[1.1] obituary_messages.txt

========================
[1.0]Installation Instructions
========================
Place Obituary.jar into Plugins folder. Add "Obituary" to plugins line of server.properties. Open
Obituary.jar in WinRar or the like. Copy Obituary.properties from into the main server folder.

========================
[1.1]obituary_messages.txt
========================
In order to add new obituary messages, add additional lines to obituary_messages.txt. Lines are added in the format of conditional:message.

Ex:
fire:<cc><player> burned to death.
fall:<cc><player> fell to their death.
fall:<cc><player> died from a terrible fall.

If there are multiple lines for a given death conditional, a random line will be selected to be displayed upon player death. If no lines exist
for a given conditional, the message will default to "<cc><player> was killed."

The various death conditionals are as follows:	lava
                                                fire
                                                fire_tick*
                                                fall
                                                water
                                                entity**
                                                player
                                                cactus
                                                explosion
                                                suicide
                                                unknown
                                                <MobName>***

* If fire_tick is not used, fire_tick deaths will use the fire death message.
** If a message is not set up for a specific mob, this is the message that will be used.
***The mob name in lower case (skeleton, creeper, pigzombie, etc.). Ex:
skeleton:<cc>Skeleton killed <player> at a range of <distance>m.
creeper:<cc><player> was blown up by a creeper.

There are also a variety of variables which can be used in most messages:
                        <player>		|The name of the player who died.
                        <killer>		|The name of the entity that killed the player.
                        <distance>		|The distance between the player and the attacker.
                        <item>			|The item the attacker is holding.
                        <damage>		|The amount of damage the killing blow dealt.
                        <cX>			|Color. Replace "X" with 0-f to change the color.
                        a<n>			|If the letter of the next word begins with a, e, u, i or o, the 'n'
																|is displayed. Otherwise, it is not.

Ex:
ranged:<cc><killer> killed <player> with a<n> <item> at a range of <distance>m.