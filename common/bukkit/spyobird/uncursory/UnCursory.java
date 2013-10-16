package bukkit.spyobird.uncursory;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        for (String word : event.getMessage().split(" "))
        {
            if (getConfig().getStringList("curseWords").contains(word.toLowerCase()))
            {
                if (!event.getPlayer().hasPermission("uc.allowCurse"))
                {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.GREEN + getConfig().getString("msgCurse"));
                    if (getConfig().getBoolean("moneyUsed") == true && !event.getPlayer().hasPermission("uc.noMoneyLost"))
                    {
                        money = (float) getConfig().getFloatList("moneyCost").get(0);
                        EconomyResponse r = econ.withdrawPlayer(event.getPlayer().getName(), money);
                        if (r.transactionSuccess())
                        {
                            event.getPlayer().sendMessage(ChatColor.BLUE + getConfig().getString("msgMoney"));
                        }
                        else
                        {
                            event.getPlayer().sendMessage(ChatColor.BLUE + getConfig().getString("msgMoneyNone"));
                        }
                    }
                    if (getConfig().getBoolean("takeDmg") == true && !event.getPlayer().hasPermission("uc.noHealthLost"))
                    {
                        dmg = getConfig().getInt("dmgAmount");
                        event.getPlayer().damage(dmg);
                        event.getPlayer().sendMessage(ChatColor.RED + getConfig().getString("msgDmg"));
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