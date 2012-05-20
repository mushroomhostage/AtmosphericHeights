/*
Copyright (c) 2012, Mushroom Hostage
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package me.exphc.AtmosphericHeights;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Formatter;
import java.util.Random;
import java.lang.Byte;
import java.io.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.Material.*;
import org.bukkit.material.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.inventory.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.scheduler.*;
import org.bukkit.enchantments.*;
import org.bukkit.*;

import net.minecraft.server.CraftingManager;

import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

class AtmosphericHeightsListener implements Listener {
	AtmosphericHeights plugin;
    Random random;

    ConcurrentHashMap<UUID, Boolean> inOuterspace;

	public AtmosphericHeightsListener(AtmosphericHeights plugin) {
		this.plugin = plugin;

        random = new Random();

        inOuterspace = new ConcurrentHashMap<UUID, Boolean>();

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

    private int getTropopause(Player player) {
        return plugin.getConfig().getInt(player.getWorld().getName() + ".tropopause", 128);
    }

    private int getMesopause(Player player) {
        return plugin.getConfig().getInt(player.getWorld().getName() + ".mesopause", 256);
    }

    private int getMagnetopause(Player player) {
        return plugin.getConfig().getInt(player.getWorld().getName() + ".magnetopause", 512);
    }

    private int getKalmanLine(Player player) {
        return plugin.getConfig().getInt(player.getWorld().getName() + ".kalmanLine", 1024);
    }


    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true) 
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player)entity;

        int oldLevel = player.getFoodLevel();
        int newLevel = event.getFoodLevel();

        // hunger change in half hearts
        int delta = oldLevel - newLevel;

        plugin.log("Hunger change: " + oldLevel + " -> " + newLevel + " (delta = "+delta+")");

        if (delta < 0) {
            // increased = ate, do nothing
            return;
        }

        int height = player.getLocation().getBlockY();

        if (height > getTropopause(player)) {
            applyTropo(player, event, delta, height, oldLevel);
        }

    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerMove(PlayerMoveEvent event) {
        int height = event.getTo().getBlockY();
        Player player = event.getPlayer();

        if (height > getMesopause(player)) {
            applyMeso(player, height);
        }

        if (height > getMagnetopause(player)) {
            applyMagneto(player, height);
        }

        if (height > getKalmanLine(player)) {
            applyOuterspace(player, height);
        }
    }

    // Thinner air, hungrier
    private void applyTropo(Player player, FoodLevelChangeEvent event, int delta, int height, int oldLevel) {
        double hungerPerMeter = plugin.getConfig().getDouble(player.getWorld().getName() + ".hungerPerMeter", 10.0);

        double moreHunger = Math.ceil((height - getTropopause(player)) / hungerPerMeter);

        delta += moreHunger;
        plugin.log("Above tropopause, new hunger delta = " + delta);

        int newLevel = oldLevel - delta;
        plugin.log("set level: " + newLevel);
        event.setFoodLevel(newLevel);
    }

    // Meteors or no air, suffocating
    private void applyMeso(Player player, int height) {
        if (random.nextInt(plugin.getConfig().getInt(player.getWorld().getName() + ".damageChance", 10)) != 0) {
            // lucked out
            return;
        }
        
        int damage = (int)Math.ceil((height - getMesopause(player)) / plugin.getConfig().getDouble(player.getWorld().getName() + ".damagePerMeter", 10.0));
        damage = Math.max(damage, plugin.getConfig().getInt(player.getWorld().getName() + ".damageMax", 10));

        if (player.getHealth() - damage < plugin.getConfig().getInt(player.getWorld().getName() + "damageHealthMin", 2)) {
            return; // you've been spared - can't go further
        }

        if (hasOxygenMask(player)) {
            //plugin.log("Player "+player+" wearing oxygen mask, avoided suffocation damage "+damage);
            return;
        }

        player.damage(damage);
        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.SUFFOCATION, damage));
        plugin.log("Above mesopause, suffocation damage = " + damage);
    }

    // Cosmic rays, unprotected from earth's magnetic field, set aflame
    private void applyMagneto(Player player, int height) {
        if (random.nextInt(plugin.getConfig().getInt(player.getWorld().getName() + ".fireChance", 5)) != 0) {
            return;
        }

        if (hasSpacesuit(player)) {
            // absorb damage
            damageSpacesuit(player);
        } else {
            // exposed to the elements!
            player.setFireTicks(plugin.getConfig().getInt(player.getWorld().getName() + ".fireTicks", 20*2));
        } 
    }

    private void applyOuterspace(Player player, int height) {
        if (inOuterspace.containsKey(player.getUniqueId())) {
            return;  // already told (reset on quit)
        }

        player.sendMessage(plugin.getConfig().getString(player.getWorld().getName() + ".kalmanLineMessage", "You are now in outer space"));

        // TODO: above very high elevations, reduce gravity?? no gravity? allow flying around freely without falling?

        inOuterspace.put(player.getUniqueId(), true);
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true) 
    public void onPlayerQuit(PlayerQuitEvent event) {
        // tell in outerspace next time
        inOuterspace.remove(event.getPlayer().getUniqueId());
    }

    final Enchantment RESPIRATION = Enchantment.OXYGEN;

    private boolean hasOxygenMask(Player player) {
        if (!plugin.getConfig().getBoolean(player.getWorld().getName() + ".oxygenMaskEnabled", true)) {
            return false;
        }
        ItemStack helmet = player.getInventory().getHelmet();

        return helmet != null 
            && helmet.containsEnchantment(RESPIRATION) 
            && helmet.getEnchantmentLevel(RESPIRATION) >= plugin.getConfig().getInt(player.getWorld().getName() + ".oxygenMaskMinLevel", 1);
    }

    private boolean hasSpacesuit(Player player) {
        if (!plugin.getConfig().getBoolean(player.getWorld().getName() + ".spacesuitEnabled", true)) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();

        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        if (helmet == null || chestplate == null || leggings == null || boots == null) {
            // incomplete suit
            plugin.log("incomplete suit on "+player.getName());
            return false;
        }

        /* TODO: tell if suit is all made of same material? all different type ids even though all diamond..
        int type = helmet.getTypeId();
        if (chestplate.getTypeId() != type || leggings.getTypeId() != type || boots.getTypeId() != type) {
            // mixed suit, not acceptable - must be all same material
            plugin.log("mixed suit");
            return null;
        }
        */
        // TODO: craftable or with enchantments? protection? but could get complex, four-part suit..

        return true;
    }

    private void damageSpacesuit(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack spacesuit;
        int n = random.nextInt(4);

        // Return random part of suit to damage
        // TODO: weights for different pieces? since provide different protection..
        switch (n) {
        case 0: spacesuit = inventory.getHelmet(); break;
        case 1: spacesuit = inventory.getChestplate(); break;
        case 2: spacesuit = inventory.getLeggings(); break;
        default:
        case 3: spacesuit = inventory.getBoots(); break;
        }

        short damage = spacesuit.getDurability();

        damage += plugin.getConfig().getInt(player.getWorld().getName() + ".spacesuitDamagePerHit", 100);
        // TODO: don't completely wearout?
        //damage = (short)Math.max(damage, plugin.getConfig().getInt(player.getWorld().getName() + ".spacesuitDamageMin", 10));

        plugin.log("Damage = "+  damage);
            
        spacesuit.setDurability(damage);
        // TODO: destroy if beyond max damage?

        switch (n) {
        case 0: inventory.setHelmet(spacesuit); break;
        case 1: inventory.setChestplate(spacesuit); break;
        case 2: inventory.setLeggings(spacesuit); break;
        default:
        case 3: inventory.setBoots(spacesuit); break;
        }
    }
}

public class AtmosphericHeights extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");

    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        new AtmosphericHeightsListener(this);
    }

    public void onDisable() {
    }

    public void log(String message) {
        if (getConfig().getBoolean("verbose", true)) {
            log.info(message);
        }
    }

}
