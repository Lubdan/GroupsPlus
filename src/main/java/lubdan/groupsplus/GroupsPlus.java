package lubdan.groupsplus;

import lubdan.groupsplus.Events.Chat;
import lubdan.groupsplus.Events.Join;
import lubdan.groupsplus.commands.GC;
import lubdan.groupsplus.commands.Group;
import lubdan.groupsplus.util.PData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.sqlite.JDBC;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public final class GroupsPlus extends Plugin {

    private Connection DBconnection;
    private Properties properties;
    private HashMap<String, PData> PToG;
    private HashMap<String, List<String>> groups;
    private HashMap<String,List<String>> invites;
    private List<String> chattoggled;

    @Override
    public void onEnable() {
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.registerDriver(new JDBC());
            File file = new File("");
            Files.createDirectories(Paths.get(file.getAbsolutePath()+"/plugins/GroupsPlus/"));
            initializeDatabase(file.getAbsolutePath() + "/plugins/GroupsPlus/Data.DB");
            setProperties();
            PToG = new HashMap<>();
            groups = new HashMap<>();
            invites = new HashMap<>();
            chattoggled = new ArrayList<>();
            getProxy().getPluginManager().registerCommand(this, new Group(this,properties));
            getProxy().getPluginManager().registerCommand(this, new GC(this,properties));
            getProxy().getPluginManager().registerListener(this, new Join(this));
            getProxy().getPluginManager().registerListener(this,new Chat(this));
            }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveGroup(String UUID){
        List<String> group = groups.get(PToG.get(UUID).getGroupLeaderUUID());
        group.remove(UUID);
        groups.remove(PToG.get(UUID).getGroupLeaderUUID());
        groups.put(PToG.get(UUID).getGroupLeaderUUID(),group);
        leaveBC(UUID,PToG.get(UUID).getGroupLeaderUUID());
        PData old = PToG.get(UUID);
        old.setGroupLeaderUUID(null);
        PToG.remove(UUID);
        PToG.put(UUID,old);
        try{
            String sql = "UPDATE Players SET GroupLeader = ? WHERE UUID = ?";
            PreparedStatement stn = DBconnection.prepareStatement(sql);
            stn.setString(1,null);
            stn.setString(2,UUID);
            stn.execute();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean isSpecificLeader(String leader, String follower){
        return PToG.get(follower).getGroupLeaderUUID().equals(leader);
    }

    public void disbandGroup(String UUID){
        PData oldpdata = PToG.get(UUID);
        oldpdata.setLeader(false);
        oldpdata.setGroupLeaderUUID(null);
        PToG.remove(UUID);
        PToG.put(UUID,oldpdata);
        for(String player : groups.get(UUID)){
            ProxiedPlayer pp = getProxy().getPlayer(java.util.UUID.fromString(player));
            String uuid = pp.getUniqueId().toString();
            PData temp = PToG.get(uuid);
            temp.setGroupLeaderUUID(null);
            PToG.remove(uuid);
            PToG.put(uuid,temp);
            if(chattoggled.contains(uuid)){
                chattoggled.remove(uuid);
            }

            pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Group-Disbanded"))));
        }
        groups.remove(UUID);

        try{
            String sql = "UPDATE Players SET GroupLeader = ? WHERE GroupLeader = ?";
            PreparedStatement stn = DBconnection.prepareStatement(sql);
            stn.setString(1,null);
            stn.setString(2,UUID);
            stn.execute();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }




    }

    public void createGroup(String UUID){
        PData olddata = PToG.get(UUID);
        olddata.setGroupLeaderUUID(UUID);
        olddata.setLeader(true);
        this.setGroup(UUID,olddata);
        //update DB
        try{
            String sql = "DELETE FROM Players WHERE UUID = ?";
            PreparedStatement stm = DBconnection.prepareStatement(sql);
            stm.setString(1, UUID);
            stm.execute();
            String recinv;
            if(olddata.isRecieveinvites()){
                recinv = "True";
            }
            else{
                recinv = "False";
            }
            String sql2 = "INSERT INTO Players (UUID,GroupLeader,ReceiveInvites) VALUES (?,?,?)";
            PreparedStatement stmn2 = DBconnection.prepareStatement(sql2);
            stmn2.setString(1,UUID);
            stmn2.setString(2,UUID);
            stmn2.setString(3,recinv);
            stmn2.execute();
            TextComponent created = new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Group-Created")) + " ");
            ProxiedPlayer pp = getProxy().getPlayer(java.util.UUID.fromString(UUID));
            pp.sendMessage(created);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }


    public void loadGroup(String UUID){
        try{
            String sql = "SELECT * FROM Players WHERE UUID = ?";
            PreparedStatement stm = DBconnection.prepareStatement(sql);
            stm.setString(1, UUID);
            ResultSet rs = stm.executeQuery();
            String GL = null;
            String rtoggled = null;
            while(rs.next()){
                GL = rs.getString("GroupLeader");
                rtoggled = rs.getString("ReceiveInvites");
            }
            if(rtoggled == null){
                rtoggled = "True";
            }
            setGroup(UUID,new PData(UUID,GL,rtoggled.equals("True"),UUID.equals(GL)));
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void BCTOG(String SUUID, String message){
        String name = getProxy().getPlayer(UUID.fromString(SUUID)).getName();
        for(String player : groups.get(PToG.get(SUUID).getGroupLeaderUUID())){
            ProxiedPlayer pp = this.getProxy().getPlayer(UUID.fromString(player));
            try{
                pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Group-Chat-Format").replace("%name%",name).replace("%msg%",message))));

            }
            catch (Exception ex){
                //deciding rather to remove people from groups when leaving or not.
            }
        }
    }

    public String listMembers(String UUID){
        String LUUID = PToG.get(UUID).getGroupLeaderUUID();
        StringBuilder sb = new StringBuilder();
        for(String player : groups.get(LUUID)){
            ProxiedPlayer pp = getProxy().getPlayer(java.util.UUID.fromString(player));
            sb.append((properties.getProperty("List-Format").replace("%name%",pp.getName()) + "  "));
        }
        return sb.toString();
    }

    public boolean isChatToggled(String UUID){
      return chattoggled.contains(UUID);
    }

    public boolean toggleChat(String UUID){
        if(chattoggled.contains(UUID)){
            chattoggled.remove(UUID);
            return false;
        }
        else{
            chattoggled.add(UUID);
            return true;
        }
    }
    public void toggleInvites(String UUID){
        PData olddata = PToG.get(UUID);
        olddata.setRecieveinvites(!olddata.isRecieveinvites());
        this.setGroup(UUID,olddata);
        //update DB
        try{
            String sql = "DELETE FROM Players WHERE UUID = ?";
            PreparedStatement stm = DBconnection.prepareStatement(sql);
            stm.setString(1, UUID);
            stm.execute();
            String recinv = olddata.isRecieveinvites() ? "True" : "False";
            String sql2 = "INSERT INTO Players (UUID,GroupLeader,ReceiveInvites) VALUES (?,?,?)";
            PreparedStatement stmn2 = DBconnection.prepareStatement(sql2);
            stmn2.setString(1,UUID);
            stmn2.setString(2,olddata.getGroupLeaderUUID());
            stmn2.setString(3,recinv);
            stmn2.execute();
            TextComponent TGI;
            if(olddata.isRecieveinvites()){
                TGI = new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Toggled-Invites-On")) + " ");
            }
            else{
                TGI = new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Toggled-Invites-Off")) + " ");
            }

            ProxiedPlayer pp = getProxy().getPlayer(java.util.UUID.fromString(UUID));
            pp.sendMessage(TGI);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public boolean invitesOn(String UUID){
        return PToG.get(UUID).isRecieveinvites();
    }

    public void invitePlayer(String LUUID, String IUUID){
        if(invites.containsKey(LUUID)){
            List<String> list = invites.get(LUUID);
            list.add(IUUID);
            invites.remove(LUUID);
            invites.put(LUUID,list);
            ProxiedPlayer leader = this.getProxy().getPlayer(UUID.fromString(LUUID));
            ProxiedPlayer invitee = this.getProxy().getPlayer(UUID.fromString(IUUID));
            TextComponent invmsg = new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Invite-Msg").replace("%leader%",leader.getName())));
            invmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/group join " + leader.getUniqueId().toString()));
            invitee.sendMessage(invmsg);
        }
        else{
            List<String> list = new ArrayList<>();
            list.add(IUUID);
            invites.put(LUUID,list);
            ProxiedPlayer leader = this.getProxy().getPlayer(UUID.fromString(LUUID));
            ProxiedPlayer invitee = this.getProxy().getPlayer(UUID.fromString(IUUID));
            TextComponent invmsg = new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Invite-Msg").replace("%leader%",leader.getName())));
            invmsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/group join " + leader.getUniqueId().toString()));
            invitee.sendMessage(invmsg);
        }
    }

    public void joinGroup(String JUUID, String LUUID){
        if(invites.containsKey(LUUID)){
            List<String> invs = invites.get(LUUID);
            if(!invs.contains(JUUID)){
                ProxiedPlayer ps = getProxy().getPlayer(UUID.fromString(JUUID));
                ps.sendMessage(new TextComponent(ChatColor.RED+"You are not invited"));
                return;
            }
        }
        else{
            ProxiedPlayer ps = getProxy().getPlayer(UUID.fromString(JUUID));
            ps.sendMessage(new TextComponent(ChatColor.RED+"You are not invited;"));
            return;
        }
        List<String> invs = invites.get(LUUID);
        invs.remove(JUUID);
        invites.remove(LUUID);
        invites.put(LUUID,invs);
        PData olddata = PToG.get(JUUID);
        olddata.setGroupLeaderUUID(LUUID);
        olddata.setLeader(false);
        this.setGroup(JUUID,olddata);
        //update DB
        try{
            String sql = "DELETE FROM Players WHERE UUID = ?";
            PreparedStatement stm = DBconnection.prepareStatement(sql);
            stm.setString(1, JUUID);
            stm.execute();
            String recinv;
            if(olddata.isRecieveinvites()){
                recinv = "True";
            }
            else{
                recinv = "False";
            }
            String sql2 = "INSERT INTO Players (UUID,GroupLeader,ReceiveInvites) VALUES (?,?,?)";
            PreparedStatement stmn2 = DBconnection.prepareStatement(sql2);
            stmn2.setString(1,JUUID);
            stmn2.setString(2,LUUID);
            stmn2.setString(3,recinv);
            stmn2.execute();
            ProxiedPlayer jp = getProxy().getPlayer(UUID.fromString(JUUID));
            ProxiedPlayer lp = getProxy().getPlayer(UUID.fromString(LUUID));
            TextComponent joined = new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Joined-Group").replace("%leader%",lp.getName())) + " ");
            jp.sendMessage(joined);
            joinBC(JUUID,LUUID);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void leaveBC(String leUUID, String LUUID){
        String leaver = getProxy().getPlayer(UUID.fromString(leUUID)).getName();
        for(String player : groups.get(LUUID)){
            ProxiedPlayer pp = this.getProxy().getPlayer(UUID.fromString(player));
            pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Party-Leave-BC").replace("%leaver%", leaver))));
        }
    }


    public void joinBC(String JUUID, String LUUID){
        String joiner = getProxy().getPlayer(UUID.fromString(JUUID)).getName();
        for(String player : groups.get(LUUID)){
            ProxiedPlayer pp = this.getProxy().getPlayer(UUID.fromString(player));
            pp.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',properties.getProperty("Party-Join-BC").replace("%joiner%",joiner))));
        }
    }
    public boolean isLeader(String UUID){
        return PToG.get(UUID).isLeader();
    }

    public void setGroup(String UUID,PData data){
        if(PToG.containsKey(UUID)){
            PToG.remove(UUID);
            PToG.put(UUID,data);
            if(data.getGroupLeaderUUID() != null) {
                if (groups.containsKey(data.getGroupLeaderUUID())) {
                    List<String> holder = groups.get(data.getGroupLeaderUUID());
                    if(holder.contains(UUID)){
                        return;
                    }
                    holder.add(UUID);
                    groups.remove(data.getGroupLeaderUUID());
                    groups.put(data.getGroupLeaderUUID(), holder);
                } else {
                    List<String> holder = new ArrayList<>();
                    holder.add(UUID);
                    groups.put(data.getGroupLeaderUUID(), holder);
                }
            }
            return;
        }
        PToG.put(UUID,data);
        if(data.getGroupLeaderUUID() != null) {
            if (groups.containsKey(data.getGroupLeaderUUID())) {
                List<String> holder = groups.get(data.getGroupLeaderUUID());
                if(holder.contains(UUID)){
                    return;
                }
                holder.add(UUID);
                groups.remove(data.getGroupLeaderUUID());
                groups.put(data.getGroupLeaderUUID(), holder);
            } else {
                List<String> holder = new ArrayList<>();
                holder.add(UUID);
                groups.put(data.getGroupLeaderUUID(), holder);
            }
        }
    }

    public boolean inGroup(String UUID){return PToG.get(UUID).getGroupLeaderUUID() != null;}
    private void setProperties() throws Exception {
        File file = new File("");
        if(!Files.exists(Paths.get(file.getAbsolutePath()+"/plugins/GroupsPlus/GroupsPlus.properties"))){
            properties = new Properties();
            properties.setProperty("Line.Color","&a");
            properties.setProperty("Create","&f[&6Create&f]");
            properties.setProperty("Receive-Invites","&f[&6Receive-Invites&f]");
            properties.setProperty("Version","&f[&6Version&f]");
            properties.setProperty("Invite","&f[&aInvite&f]");
            properties.setProperty("ToggleChat","&f[&6Toggle-Chat&f]");
            properties.setProperty("Disband","&f[&cDisband&f]");
            properties.setProperty("Already-in-Group","&c You are already in a group.");
            properties.setProperty("Group-Created","&aYour group has been created!");
            properties.setProperty("Toggled-Invites-On","&aYour invites have been toggled on");
            properties.setProperty("Toggled-Invites-Off","&cYour invites have been toggled off");
            properties.setProperty("Invite-Msg","&aYou have been invited to join %leader%'s group. Click this message to accept!");
            properties.setProperty("Joined-Group","&a You have successfully joined %leader%'s group!");
            properties.setProperty("Party-Join-BC","&a %joiner% has joined the group!");
            properties.setProperty("GC-On","&aGroup Chat toggled on.");
            properties.setProperty("GC-Off","&cGroup Chat toggled off.");
            properties.setProperty("Group-Chat-Format","&f[&aGroupChat&f] &a%name%: &a%msg%");
            properties.setProperty("Leave","&f[&cLeave&f]");
            properties.setProperty("ListMembers","&f[&6List&f-&6Members&f]");
            properties.setProperty("Group-Disbanded","&cYour group was disbanded!");
            properties.setProperty("Left-Group","&cYou have left your group!");
            properties.setProperty("Party-Leave-BC","&c %leaver% has left the group!");
            properties.setProperty("List-Format","&f[&c%name%&f]");
            properties.setProperty("Kick","&f[&cKick&f]");
            OutputStream output = new FileOutputStream(file.getAbsolutePath()+"/plugins/GroupsPlus/GroupsPlus.properties");
            properties.store(output,null);
        }
        else{
            InputStream input = new FileInputStream(file.getAbsolutePath()+"/plugins/GroupsPlus/GroupsPlus.properties");
            properties = new Properties();
            properties.load(input);
        }
    }
    private void initializeDatabase(String path){
        try{
            String url = "jdbc:sqlite:"+path;
            DBconnection = DriverManager.getConnection(url);
                String sql = "CREATE TABLE IF NOT EXISTS Players(\n"
                        + "UUID text PRIMARYKEY, \n"
                        + "GroupLeader text, \n"
                        + "ReceiveInvites text \n"
                        + ");";
                Statement statement = DBconnection.createStatement();
                statement.execute(sql);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Please contact Lubdan#0420 or 'Lubdan' on spigot with the above stacktrace.");
        }

    }

}
