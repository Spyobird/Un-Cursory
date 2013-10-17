package bukkit.spyobird.uncursory;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class UnCursory extends JavaPlugin implements Listener
{
    public static float money;
    public static int dmg;
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = (Player) event.getPlayer();
        FileConfiguration config = (FileConfiguration) getConfig();
        for (String word : event.getMessage().split(" "))
        {
            if (config.getStringList("curseWords").contains(word.toLowerCase()))
            {
                if (!player.hasPermission("uc.allowCurse"))
                {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.GREEN + config.getString("msgCurse"));
                    if (config.getBoolean("changeGm") == true)
                    {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                    if (config.getBoolean("moneyUsed") == true && !player.hasPermission("uc.noMoneyLost"))
                    {
                        money = (float) config.getFloatList("moneyCost").get(0);
                        EconomyResponse r = econ.withdrawPlayer(player.getName(), money);
                        if (r.transactionSuccess())
                        {
                            player.sendMessage(ChatColor.BLUE + config.getString("msgMoney"));
                        }
                        else
                        {
                            player.sendMessage(ChatColor.BLUE + config.getString("msgMoneyNone"));
                        }
                    }
                    if (config.getBoolean("takeDmg") == true && !player.hasPermission("uc.noHealthLost"))
                    {
                        dmg = config.getInt("dmgAmount");
                        player.damage(dmg);
                        player.sendMessage(ChatColor.RED + config.getString("msgDmg"));
                    }
                    if (config.getBoolean("takeFood") == true && !player.hasPermission("uc.noFoodLost"))
                    {
                        food = config.getInt("foodAmount");
                        int getFoodLevel = player.getFoodLevel();
                        player.setFoodLevel(getFoodLevel - food);
                        player.sendMessage(ChatColor.RED + config.getString("msgFood"));
                    }
                    if (config.getBoolean("addFoodRate") == true && !player.hasPermission("uc.noFoodLost"))
                    {
                        foodRate = (float) config.getFloatList("foodRateAmount").get(0);
                        float getExhaustion = player.getExhaustion();
                        player.setExhaustion(getExhaustion + foodRate);
                    }
                }
            }
        }
    }

    //Vault Plugin Configuration
    private static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;

    @Override
    public void onDisable()
    {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable()
    {
        getConfig().options().copyDefaults(true);
        saveConfig();
        log.info(String.format("[%s] Plugin Enabled", getDescription().getName()));
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        if (!setupEconomy() )
        {
            log.severe(String.format("[%s] - No Vault dependency found!", getDescription().getName()));
            return;
        }
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}