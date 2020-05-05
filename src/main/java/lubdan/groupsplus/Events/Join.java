package lubdan.groupsplus.Events;

import lubdan.groupsplus.GroupsPlus;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Join implements Listener {

    private GroupsPlus plugin;

    public Join(GroupsPlus instance){
        this.plugin = instance;
    }

    @EventHandler
    public void onJoin(PostLoginEvent event){
    plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
        @Override
        public void run() {
            plugin.loadGroup(event.getPlayer().getUniqueId().toString());
        }
    });
    }


}
