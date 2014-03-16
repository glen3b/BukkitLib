GBukkitLibrary
=========

This library allows usage of certain functions not provided by the bukkit API that I find I commonly use. It also allows for registration of certain properties in a single configuration file. For example, to teleport a user with delay, all you have to do is this:

    TeleportationManager teleportManager = getServer().getServicesManager().getRegistration(TeleportationManager.class).getProvider();
    teleportManager.teleportPlayer(player, targetLocation);
    
That's it! All of the messaging to the player, actual teleportation, and teleport delay is handled by my plugin. It is all configurable in a single file, so all plugins using this API will have a consistent feel **and** consistent teleport delay.
