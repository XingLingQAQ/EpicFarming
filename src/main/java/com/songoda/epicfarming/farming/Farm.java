package com.songoda.epicfarming.farming;


import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfarming.EpicFarming;
import com.songoda.epicfarming.Lang;
import com.songoda.epicfarming.player.PlayerData;
import com.songoda.epicfarming.utils.Debugger;
import com.songoda.epicfarming.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Farm implements InventoryHolder {

    private Location location;
    private Level level;
    private Inventory inventory;

    public Farm(Location location, Level level) {
        this.location = location;
        this.level = level;
        this.inventory = Bukkit.createInventory(null, 54, Methods.formatName(level.getLevel(),false));
    }

    public void view(Player player) {
        try {
            if (!player.hasPermission("epicfarming.view"))
                return;

                setupOverview(player);

            player.openInventory(inventory);
            PlayerData playerData = EpicFarming.getInstance().getPlayerActionManager().getPlayerAction(player);

            playerData.setFarm(this);

            getInventory();

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public void setupOverview(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, Methods.formatName(level.getLevel(),false));
        inventory.setContents(this.inventory.getContents());
        this.inventory = inventory;

        EpicFarming instance = EpicFarming.getInstance();

        Level nextLevel = instance.getLevelManager().getHighestLevel().getLevel() > level.getLevel() ? instance.getLevelManager().getLevel(level.getLevel()+1) : null;

        int level = this.level.getLevel();

        ItemStack item = new ItemStack(Material.END_ROD, 1);
        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(Arconix.pl().getApi().format().formatText(Lang.LEVEL.getConfigValue(level)));
        ArrayList<String> lore = this.level.getDescription();
        lore.add("");
        if (nextLevel == null) lore.add(Lang.MAXED.getConfigValue());
        else {
            lore.add(Lang.NEXT_LEVEL.getConfigValue(nextLevel.getLevel()));
            lore.addAll(nextLevel.getDescription());
        }

        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        ItemStack itemXP = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.XP Icon")), 1);
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(Lang.XPTITLE.getConfigValue());
        ArrayList<String> loreXP = new ArrayList<>();
        if (nextLevel != null)
            loreXP.add(Lang.XPLORE.getConfigValue(Integer.toString(nextLevel.getCostExperiance())));
        else
            loreXP.add(Lang.MAXED.getConfigValue());
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.Economy Icon")), 1);
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(Lang.ECOTITLE.getConfigValue());
        ArrayList<String> loreECO = new ArrayList<>();
        if (nextLevel != null)
            loreECO.add(Lang.ECOLORE.getConfigValue(Arconix.pl().getApi().format().formatEconomy(nextLevel.getCostExperiance())));
        else
            loreECO.add(Lang.MAXED.getConfigValue());
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        int nu = 0;
        while (nu != 27) {
            inventory.setItem(nu, Methods.getGlass());
            nu++;
        }
        if (instance.getConfig().getBoolean("Main.Upgrade With XP") && player != null && player.hasPermission("EpicDispensers.Upgrade.XP")) {
            inventory.setItem(11, itemXP);
        }

        inventory.setItem(13, item);

        if (instance.getConfig().getBoolean("Main.Upgrade With Economy") && player != null && player.hasPermission("EpicDispensers.Upgrade.ECO")) {
            inventory.setItem(15, itemECO);
        }

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(10, Methods.getBackgroundGlass(false));
        inventory.setItem(16, Methods.getBackgroundGlass(false));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));
        inventory.setItem(25, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void loadInventory(List<ItemStack> items) {
        setupOverview(null);
        int i = 27;
        for (ItemStack item : items) {
            inventory.setItem(i++, item);
        }
    }

    public List<ItemStack> dumpInventory() {
        List<ItemStack> items = new ArrayList<>();

        for(int i=27; i < inventory.getSize(); i++) {
            items.add(inventory.getItem(i));
        }

        return items;
    }

    public void upgrade(String type, Player player) {
        try {
            EpicFarming instance = EpicFarming.getInstance();
            if (instance.getLevelManager().getLevels().containsKey(this.level.getLevel()+1)) {

                Level level = instance.getLevelManager().getLevel(this.level.getLevel()+1);
                int cost;
                if (type == "XP") {
                    cost = level.getCostExperiance();
                } else {
                    cost = level.getCostEconomy();
                }

                if (type == "ECO") {
                    if (instance.getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(player, cost)) {
                            econ.withdrawPlayer(player, cost);
                            upgradeFinal(level, player);
                        } else {
                            player.sendMessage(instance.references.getPrefix() + Lang.CANT_AFFORD.getConfigValue(null));
                        }
                    } else {
                        player.sendMessage("Vault is not installed.");
                    }
                } else if (type == "XP") {
                    if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.setLevel(player.getLevel() - cost);
                        }
                        upgradeFinal(level, player);
                    } else {
                        player.sendMessage(instance.references.getPrefix() + Lang.CANT_AFFORD.getConfigValue(null));
                    }
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }


    public void upgradeFinal(Level level, Player player) {
        try {
            EpicFarming instance = EpicFarming.getInstance();
            this.level = level;
            if (instance.getLevelManager().getHighestLevel() != level) {
                player.sendMessage(Lang.UPGRADE_MESSAGE.getConfigValue(level.getLevel()));
            } else {
                player.sendMessage(Lang.YOU_MAXED.getConfigValue(Integer.toString(level.getLevel())));
            }
            if (instance.getConfig().getBoolean("settings.On-upgrade-particles")) {
                Location loc = location.clone().add(.5,.5,.5);
                if (!instance.v1_8 && !instance.v1_7) {
                    player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(instance.getConfig().getString("Main.Upgrade Particle Type")), loc, 200, .5, .5, .5);
                } else {
                    player.getWorld().playEffect(loc, org.bukkit.Effect.valueOf(instance.getConfig().getString("Main.Upgrade Particle Type")), 1, 0);
                    //Can't get that to resolve.
                    //player.getWorld().spigot().playEffect(loc, org.bukkit.Effect.valueOf(instance.getConfig().getString("Main.Upgrade Particle Type")), 1, 0, (float) 1, (float) 1, (float) 1, 1, 200, 10);
                }
            }
            if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
                if (instance.getLevelManager().getHighestLevel() == level) {
                    if (!instance.v1_8 && !instance.v1_7) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
                    } else {
                        player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 2F, 15.0F);
                    }
                } else {
                    if (!instance.v1_10 && !instance.v1_9 && !instance.v1_8 && !instance.v1_7) {
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_CHIME, 2F, 25.0F);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_CHIME, 1.2F, 35.0F), 5L);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_CHIME, 1.8F, 35.0F), 10L);
                    } else {
                        player.playSound(player.getLocation(), org.bukkit.Sound.valueOf("LEVEL_UP"), 2F, 25.0F);
                    }
                }
            }
            tillLand(location);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private static final Random random = new Random();

    public boolean tillLand(Location location) {
        EpicFarming instance = EpicFarming.getInstance();
        Block block = location.getBlock();
        int radius = level.getRadius();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -2; fy <= 1; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    // ToDo: enum for all flowers.
                    if (b2.getType() == Material.LONG_GRASS || b2.getType() == Material.RED_ROSE || b2.getType() == Material.YELLOW_FLOWER) {
                        Bukkit.getScheduler().runTaskLater(EpicFarming.getInstance(), () -> {
                            b2.getRelative(BlockFace.DOWN).setType(Material.SOIL);
                            b2.breakNaturally();
                            if (instance.getConfig().getBoolean("Main.Sounds Enabled"))
                                b2.getWorld().playSound(b2.getLocation(), org.bukkit.Sound.BLOCK_GRASS_BREAK, 10, 15);
                            }, random.nextInt(30) + 1);
                    }
                    if ((b2.getType() == Material.GRASS || b2.getType() == Material.DIRT) && b2.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        Bukkit.getScheduler().runTaskLater(EpicFarming.getInstance(), () -> {
                            b2.setType(Material.SOIL);
                            if (instance.getConfig().getBoolean("Main.Sounds Enabled"))
                                b2.getWorld().playSound(b2.getLocation(), org.bukkit.Sound.BLOCK_GRASS_BREAK, 10, 15);
                        }, random.nextInt(30) + 1);
                    }

                }
            }
        }
        return false;
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Level getLevel() {
        return level;
    }
}