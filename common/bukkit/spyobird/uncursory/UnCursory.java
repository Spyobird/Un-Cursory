package bukkit.spyobird.uncursory;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
    public static int food;
    public static float foodRate;
    private ArrayList<Player> ban = new ArrayList<Player>();
    
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
                    if (getConfig().getBoolean("changeGm") == true)
                    {
                        event.getPlayer().setGameMode(GameMode.SURVIVAL);
                    }
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
                    if (getConfig().getBoolean("takeFood") == true && !event.getPlayer().hasPermission("uc.noFoodLost"))
                    {
                        food = getConfig().getInt("foodAmount");
                        event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() - food);
                        event.getPlayer().sendMessage(ChatColor.RED + getConfig().getString("msgFood"));
                    }
                    if (getConfig().getBoolean("addFoodRate") == true && !event.getPlayer().hasPermission("uc.noFoodLost"))
                    {
                        foodRate = (float) getConfig().getFloatList("foodRateAmount").get(0);
                        event.getPlayer().setExhaustion(event.getPlayer().getExhaustion() + foodRate);
                    }
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("uc warn"))
        {
            if (args.length == 0)
            {
                sender.sendMessage(ChatColor.RED + "Please specify a player.");
                return false;
            }
            final Player player = Bukkit.getServer().getPlayer(args[1]);
            if (player == null)
            {
                sender.sendMessage(ChatColor.RED + "Player " + args[1] + " could not be found.");
                return false;
            }
            String msg = "";
            if (args.length > 1)
            {
                for (int i = 1; i < args.length; i++)
                {
                    msg += args[i];
                }
            }
            if (args.length == 1)
            {
                msg = getConfig().getString("msgWarn");
            }
            Object warnLvl = this.getConfig().getInt(player.getName());
            if (warnLvl == null)
            {
                player.sendMessage(ChatColor.RED + msg);
                this.getConfig().set(player.getName(), 1);
                this.saveConfig();
                return true;
            }
            int Lvl = Integer.parseInt(warnLvl.toString());
            if (Lvl == 1)
            {
                player.sendMessage(ChatColor.RED + msg);
                player.damage(0);
                this.getConfig().set(player.getName(), 2);
                this.saveConfig();
                return true;
            }
            if (Lvl == 2)
            {
                player.kickPlayer(ChatColor.RED + msg);
                this.getConfig().set(player.getName(), 3);
                this.saveConfig();
                return true;
            }
            if (Lvl == 3)
            {
                ban.add(player);
                player.kickPlayer(ChatColor.RED + msg);
                player.setBanned(true);
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
                {
                    public void run()
                    {
                        ban.remove(player);
                    }
                }, 600);
                return true;
            }
        }
        return true;
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