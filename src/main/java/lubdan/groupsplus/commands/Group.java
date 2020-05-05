package lubdan.groupsplus.commands;


import lubdan.groupsplus.Events.Chat;
import lubdan.groupsplus.GroupsPlus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


import java.util.Properties;
import java.util.UUID;

public class Group extends Command {

    private GroupsPlus plugin;
    private Properties settings;

   public Group(GroupsPlus instance, Properties properties){
        super("Group","Groups.use","g");
        this.plugin = instance;
        this.settings = properties;
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
       if(sender instanceof ProxiedPlayer){
           ProxiedPlayer pp = (ProxiedPlayer) sender;
           if(args.length == 0){
               if(plugin.inGroup(pp.getUniqueId().toString())){
                   //begin in a group options
                   TextComponent dashes = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Line.Color"))+ChatColor.BOLD+"══════════════════════════");
                   TextComponent invite = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Invite"))+ " ");
                   invite.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/Group invite "));
                   TextComponent toggleChat = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("ToggleChat"))+ " ");
                   toggleChat.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/gc"));
                   TextComponent disband = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Disband"))+ " ");
                   disband.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group disband"));
                   TextComponent version = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Version")) + " ");
                   version.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group version"));
                   TextComponent leave = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Leave")) + " ");
                   leave.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group leave"));
                   TextComponent listmembers = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("ListMembers")) + " ");
                   listmembers.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group listmembers"));
                   TextComponent kick = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Kick")) + " ");
                   kick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/Group kick "));
                   if(plugin.isLeader(pp.getUniqueId().toString())){
                       sender.sendMessage(dashes);
                       sender.sendMessage(invite,kick,toggleChat,listmembers,disband,version);
                       sender.sendMessage(dashes);
                   }
                   else{
                       sender.sendMessage(dashes);
                       sender.sendMessage(toggleChat,listmembers,leave,version);
                       sender.sendMessage(dashes);
                   }

               }
               else{
                   //not in a group
                   TextComponent dashes = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Line.Color"))+ChatColor.BOLD+"══════════════════════════");
                   TextComponent create = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Create"))+ " ");
                   create.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group create"));
                   TextComponent ReceiveInv = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Receive-Invites")) + " ");
                   ReceiveInv.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group toggleinvites"));
                   TextComponent version = new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Version")) + " ");
                   version.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/Group version"));
                   sender.sendMessage(dashes);
                   sender.sendMessage(create,ReceiveInv,version);
                   sender.sendMessage(dashes);
               }

           }
           else{
               //begin arg commands
               if(args[0].equalsIgnoreCase("create")){
                   if(plugin.inGroup(pp.getUniqueId().toString())){
                       sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Already-in-Group")) + " "));
                       return;
                   }
                   plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                       @Override
                       public void run() {
                           plugin.createGroup(pp.getUniqueId().toString());
                       }
                   });
               }

               if(args[0].equalsIgnoreCase("toggleinvites")){
                   plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                       @Override
                       public void run() {
                        plugin.toggleInvites(pp.getUniqueId().toString());
                       }
                   });
               }

               if(args[0].equalsIgnoreCase("version")){
                   pp.sendMessage(new TextComponent(ChatColor.GREEN+"This server is running GroupsPlus 1.0 by " + ChatColor.GREEN + ChatColor.BOLD + "Lubdan"+ChatColor.GREEN+"."));
               }

               if(args[0].equalsIgnoreCase("invite")){
                   if(args.length != 2){
                       pp.sendMessage(new TextComponent(ChatColor.RED+"Please enter a player to invite. Example: /group invite Lubdan"));
                       return;
                   }
                   if(plugin.isLeader(pp.getUniqueId().toString())){

                    try{
                        ProxiedPlayer ppz = plugin.getProxy().getPlayer(args[1]);
                        if(plugin.invitesOn(ppz.getUniqueId().toString())){
                            if(!plugin.inGroup(ppz.getUniqueId().toString())){
                                plugin.invitePlayer(pp.getUniqueId().toString(),ppz.getUniqueId().toString());
                                pp.sendMessage(new TextComponent(ChatColor.GREEN+args[1] + " was invited."));
                            }
                            else{
                                pp.sendMessage(new TextComponent(ChatColor.RED+"That player is already in a group."));
                            }
                        }
                        else{
                            pp.sendMessage(new TextComponent(ChatColor.RED+"This person is not accepting group invites currently."));
                        }

                    }
                    catch (Exception ex){
                        pp.sendMessage(new TextComponent(ChatColor.RED+"That player is not online"));
                    }
                   }
                   else{
                       pp.sendMessage(new TextComponent(ChatColor.RED+"You cannot invite new group members."));
                   }

               }

               if(args[0].equalsIgnoreCase("join")){
                   if(args.length != 2){
                       pp.sendMessage(new TextComponent(ChatColor.RED+"Please click an invite message"));
                       return;
                   }
                   if(plugin.inGroup(pp.getUniqueId().toString())){
                       sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Already-in-Group")) + " "));
                       return;
                   }
                   try {
                       ProxiedPlayer lp = plugin.getProxy().getPlayer(UUID.fromString(args[1]));

                       plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                           @Override
                           public void run() {
                               plugin.joinGroup(pp.getUniqueId().toString(), lp.getUniqueId().toString());
                           }
                       });
                   }
                   catch (Exception ex){
                       pp.sendMessage(new TextComponent(ChatColor.RED+"Please click an invite message"));
                   }


               }

               if(args[0].equalsIgnoreCase("disband")){
                   String uuid = pp.getUniqueId().toString();
                   if(plugin.inGroup(uuid) && plugin.isLeader(uuid)){
                       plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                           @Override
                           public void run() {
                               plugin.disbandGroup(uuid);
                           }
                       });

                   }
                   else{
                       pp.sendMessage(new TextComponent(ChatColor.RED+"You cannot disband."));
                   }
               }

               if(args[0].equalsIgnoreCase("leave")){
                   if(plugin.isLeader(pp.getUniqueId().toString())){
                       pp.sendMessage(new TextComponent(ChatColor.RED+"You cannot leave your group, you must disband it."));
                       return;
                   }
                   if(!plugin.inGroup(pp.getUniqueId().toString())){
                       pp.sendMessage(new TextComponent(ChatColor.RED+"You are not in a group!"));
                       return;
                   }
                   plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                       @Override
                       public void run() {
                        plugin.leaveGroup(pp.getUniqueId().toString());
                        pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',settings.getProperty("Left-Group"))));
                       }
                   });
               }

               if(args[0].equalsIgnoreCase("listmembers")){
                   String UUID = pp.getUniqueId().toString();
                   if(plugin.inGroup(UUID)){
                       pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',"&aOnline Members: \n" + plugin.listMembers(UUID))));
                   }
                   else{
                       pp.sendMessage(new TextComponent(ChatColor.RED+"You are not in a group!"));
                   }
               }

               if(args[0].equalsIgnoreCase("kick")){
                   if(args.length != 2){
                       pp.sendMessage(new TextComponent(ChatColor.RED+"Please include a valid group member to kick"));
                       return;
                   }
                   String UUID = pp.getUniqueId().toString();
                   if(plugin.isLeader(UUID)){
                       try{
                           ProxiedPlayer target = plugin.getProxy().getPlayer(args[1]);
                           if(plugin.isSpecificLeader(UUID,target.getUniqueId().toString())){
                               plugin.leaveGroup(target.getUniqueId().toString());
                           }

                       }
                       catch (Exception ex){
                           pp.sendMessage(new TextComponent(ChatColor.RED+"Player is not online or in your group"));
                       }


                   }
               }


           }
       }
       else{
           sender.sendMessage(new TextComponent("Only players can use this command"));
       }

    }
}
