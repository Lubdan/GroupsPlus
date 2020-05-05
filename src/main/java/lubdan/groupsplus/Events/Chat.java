package lubdan.groupsplus.Events;

import lubdan.groupsplus.GroupsPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Chat implements Listener {

    private GroupsPlus plugin;

    public Chat(GroupsPlus instance){
        this.plugin = instance;
    }

    @EventHandler
    public void onChat(ChatEvent event){
        ProxiedPlayer pp = plugin.getProxy().getPlayer(event.getSender().toString());
        if(!event.isCommand() && !event.isCancelled() && !event.isProxyCommand() && (plugin.isChatToggled(pp.getUniqueId().toString()) || event.getMessage().startsWith("@g")) && plugin.inGroup(pp.getUniqueId().toString())){
            event.setCancelled(true);
            plugin.BCTOG(pp.getUniqueId().toString(),event.getMessage());
        }
    }


}
