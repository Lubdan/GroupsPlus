package lubdan.groupsplus.commands;

import lubdan.groupsplus.GroupsPlus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Properties;

public class GC extends Command {

    private GroupsPlus plugin;
    private Properties settings;

    public GC(GroupsPlus instance, Properties settings){
        super("groupchat","Groups.use","gc");
        this.plugin = instance;
        this.settings = settings;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer){
            ProxiedPlayer pp =  (ProxiedPlayer) sender;
            if(plugin.inGroup(pp.getUniqueId().toString())){
               boolean toggled = plugin.toggleChat(pp.getUniqueId().toString());
               if(toggled){
                   pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("GC-On"))));
               }
               else{
                   pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("GC-Off"))));

               }
            }
            else{
                pp.sendMessage(new TextComponent(ChatColor.RED+"You must be in a group to use group chat."));
            }
        }
    }
}
