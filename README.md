GBukkitLibrary
=========

This library allows usage of certain functions not provided by the bukkit API that I find I commonly use. It also allows for registration of certain properties in a single configuration file. For example, to teleport a user with delay, all you have to do is this:

    TeleportationManager teleportManager = getServer().getServicesManager().getRegistration(TeleportationManager.class).getProvider();
    teleportManager.teleportPlayer(player, targetLocation);
    
That's it! All of the messaging to the player, actual teleportation, and teleport delay is handled by my plugin. It is all configurable in a single file, so all plugins using this API will have a consistent feel **and** consistent teleport delay.



	GBukkitLib - A library for common functions used in Bukkit
    Copyright (C) 2014 Glen Husman

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.