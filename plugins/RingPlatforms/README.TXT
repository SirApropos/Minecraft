========================
RingPlatform Plugin for hMod (Created by Apropos <Apropos.of.nothing@gmx.com>)
========================

This plugin requires build 131+ of hMod.

[1.0] Installation Instructions
[2.0] Usage instructions

========================
[1.0]Installation Instructions
========================
Place RingPlatforms.jar into Plugins folder. Add "RingPlatforms" to plugins line of server.properties.

========================
[2.0]Usage instructions
========================
Create two or more platforms exactly vertically above each other in the following pattern:

01110
11211
12121
11211
01110

0 = any block
1 = Any given type. All 1s must be the same type.
2 = Any given type. All 1s must be the same type.

Platforms must use the same block 1 in order to teleport between each other. Platforms must be at least six blocks away
from each other. Just right click a platform to teleport to another platform. If there are more than two platforms, each
platform will teleport to the next platform (1->2->3->1, etc.).