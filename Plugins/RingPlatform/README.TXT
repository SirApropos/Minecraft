========================
RingPlatform Plugin for hMod (Created by Apropos <Apropos.of.nothing@gmx.com>)
========================

This plugin requires build 131+ of hMod.

[1.0] Installation Instructions
[2.0] Usage instructions
[3.0] Configuration
    [3.1] Basic configuration
    [3.2] Advanced configuration

========================
[1.0]Installation Instructions
========================
Place RingPlatform.jar into Plugins folder. Add "RingPlatform" to plugins line of server.properties.


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
2 = Any given type. All 2s must be the same type.

Platforms must use the same block 1 in order to teleport between each other. Platforms must be at least six blocks away
from each other. Just right click a platform to teleport to another platform. If there are more than two platforms, each
platform will teleport to the next platform (1->2->3->1, etc.).


========================
[3.0] Configuration
========================
You have access to virtually every template used in the plugin. Templates are defined in RingPlatform.properties. This file
will be generated the first time the plugin is loaded with default options.

========================
[3.1] Basic Configuration
========================

There are a couple basic options:

check_permissions=false     //If set to true, will check if the user has access to /RingPlatform command. If not, they cannot use the platform.
max_platforms_in_stack=-1   //If set to a positive integer, this is the maximum number of platforms that will be activated in a single set.
                            //For example, f you only want to be able to transport between two platforms, set this to '2'.


========================
[3.2] Basic Configuration
========================

There are two kinds of objects that need to be defined: shapes and animations.

ex:
shape:{
    height=5             //The height of the platform. This is used in determining how tall the teleportation "field" is.
    name=defaultShape    //The name of the shape. Doesn't have much use now, but may in later versions.
    teleportShape:{      //The shape of the teleportation 'field'. 1s are blocks that will be teleported, 0s not.
        {0,0,0,0,0},
        {0,1,1,1,0},
        {0,1,1,1,0},
        {0,1,1,1,0},
        {0,0,0,0,0}
    }
    platformShape:{     //The shape of the platform. 0s are any block, 1s and 2s are blocks of any kind, but must
        {0,1,1,1,0},    //remain consistent within a given platform. The center blocks of two platforms in a set
        {1,1,2,1,1},    //must be the same.
        {1,2,1,2,1},
        {1,1,2,1,1},
        {0,1,1,1,0}
    }
    attach:defaultAnim  //The name of the animation you wish to attach to this shape. You can have multiple attach
}                       //lines. One animation per line.


ex:
animation:{
    height=5            //The height of the animation. This is needed for overlapping purposes and explosion testing.
    name=defaultAnim    //The name of the animation. This is required to attach an animation to a given shape.
    shape:{             //The shape of the animation. This is the shape of the "ring".
        {0,1,1,1,0},    //1s are blocks that will be animated, 0s are blocks that will not be.
        {1,0,0,0,1},
        {1,0,0,0,1},
        {1,0,0,0,1},
        {0,1,1,1,0}
    }
    delay=100           //This is the delay in miliseconds between animation frames.

    frame:{{1,0},{2,0},{3,0},{4,0},{5,0}} //The first frame in an animation is the cleanup frame. This will normally
                                          //never be seen and will only be used to remove animation blocks if an
                                          //error occurs.

    frame:{{1,49}}                        //Frames are displayed in the order they are listed in the definition.
                                          //This will set a ring of blocks of type 49 (obsidian) in the shape defined
                                          //above at a height of 1 block above the platform.

    frame:{{1,0},{2,49}}                  //This frame will create a ring of blocks of type 0 (air) at a height of 1 above the
                                          //platform. It will then create a ring of type 49 (obsidian) at a height of 2 above
                                          //the platform.

    frame:{{2,0},3,49}},prune:{3}         //This frame will create a ring of type 0 at height 2 and a ring of 49 at height 3.
                                          //However, because this is defined as a pruning frame, all blocks at height 3 that would
                                          //be in the way of the animation will be dropped before the blocks are displayed.
                                          //Normally, the blocks would just be overwritten.


    frame:{{3,0},{5,49}},prune:{4,5}      //This would prune the blocks at heights of 4 and 5 before creating a ring of air at
                                          //height 3 and a ring of obsidian at height 5.

    frame:protect:{{4,57}}                //This will create a ring of 57 (diamond) at a height of 4 and protect the frame.
                                          //A protected frame will not allow nearby explosions (within 5 blocks of the animation)
                                          //to detonate. This is mostly to prevent farming rare blocks.

    frame:{}                              //This will create a frame with no animation. Use these for pauses in the animation.

    frame:teleport:protect:{}             //This will create a protected teleport frame. When the animation reaches this frame,
                                          //anything within the teleportation field of the shape will be teleported.
}