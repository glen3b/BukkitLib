package me.pagekite.glen3b.library.bukkit.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.pagekite.glen3b.library.bukkit.GBukkitCorePlugin;
import me.pagekite.glen3b.library.bukkit.Utilities;
import me.pagekite.glen3b.library.bukkit.datastore.Message;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

/**
 * Represents a base command that can execute subcommands.
 * @author Glen Husman
 */
public final class BaseCommand implements TabExecutor, PreprocessedCommandHandler {

	private String _helpPageHeader = "Help (page %d):";
	private ArrayList<SubCommand> _subCommands;
	
	private GBukkitCorePlugin _plugin;
	
	/**
	 * Create a base command. For this class to execute a command, {@code register(getCommand("commandNameInPluginYaml"))} must be called.
	 * @param helpHeader The header to display on help pages, where {@code %d} is substituted with the page number. If used, color codes must be translated by the caller.
	 * @param commands The array of commands which are executed via this base command.
	 */
	public BaseCommand(String helpHeader, SubCommand... commands){
		Validate.notEmpty(commands, "At least one subcommand is required.");
		Validate.noNullElements(commands, "Null subcommands are not allowed.");
		
		if(helpHeader != null){
			_helpPageHeader = helpHeader;
		}
		
		_subCommands = Lists.newArrayList(commands);
	}
	
	private List<SubCommand> getCommands(String label, boolean getExact){
		ArrayList<SubCommand> retVal = new ArrayList<SubCommand>();
		
		for(SubCommand cmd : _subCommands){
			for(String alias : cmd.getAliases()){
				if(alias != null && (label.equalsIgnoreCase(alias) || (!getExact && alias.toLowerCase().startsWith(label.toLowerCase())))){
					retVal.add(cmd);
					//Break out of the alias loop
					break;
				}
			}
		}
		
		return retVal;
	}
	
	private FileConfiguration getConfig(){
		if(_plugin == null || !_plugin.isEnabled()){
			// TODO: BaseCommand should not directly depend upon GBukkitCore, maybe global variable (not just message) service?
			_plugin = (GBukkitCorePlugin)Bukkit.getServer().getPluginManager().getPlugin("GBukkitCore");
		}
		
		return _plugin.getConfig();
	}
	
	/**
	 * Get a list of subcommands executed by this BaseCommand instance.
	 * @return A read only {@code Collection<SubCommand>} instance that can <b>not</b> be manipulated via reference to change the subcommands executed by this base command.
	 */
	public List<SubCommand> getSubCommands(){
		return Collections.unmodifiableList(_subCommands);
	}
	
	/**
	 * Executes the base command, attempting to parse arguments.
	 * @see CommandExecutor
	 * @see Command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if(args.length == 0 || (args.length == 2 && Utilities.Arguments.parseInt(args[1], -1) > 0 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))) || (args.length == 1 && (Utilities.Arguments.parseInt(args[0], -1) > 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")))){
    		//Show help manual and quit
    		int page = 0;
    		if(args.length == 2){
    			try{
    				page = Integer.parseInt(args[1]) - 1;
    			}catch(Throwable thr){
    				//Ignore
    				page = 0;
    			}
    		}else if(args.length == 1 && !args[0].equalsIgnoreCase("help")){
    			try{
    				page = Integer.parseInt(args[0]) - 1;
    			}catch(Throwable thr){
    				//Ignore
    				page = 0;
    			}
    		}
    		
    		if(page < 0){
    			page *= -1;
    		}
    		
    		sender.sendMessage(String.format(ChatColor.AQUA + _helpPageHeader, page + 1));
    		
    		if(page * getConfig().getInt("commandsPerPage") > _subCommands.size()){
    			return true;
    		}
    		
    		for(int i = page * getConfig().getInt("commandsPerPage"); (i < ((page + 1) * getConfig().getInt("commandsPerPage")) && i < _subCommands.size()); i++){
    			sender.sendMessage(Message.get("cmdHelpEntry").replace("%basecommand%", label).replace("%usage%", _subCommands.get(i).getUsage()).replace("%desc%", _subCommands.get(i).getDescription()));
    		}
    		
    		if(((page + 1) * getConfig().getInt("commandsPerPage")) < _subCommands.size()){
    			sender.sendMessage(Message.get("cmdHelpSeeMore").replace("%basecommand%", label).replace("%page%", Integer.valueOf(page + 2).toString()));
    		}
    		
    		return true;
    	}else if(args.length >= 1){
    		List<SubCommand> cmd = getCommands(args[0], true);
    		
    		if(cmd.size() == 1){
    			if(!cmd.get(0).hasAccess(sender)){
    				sender.sendMessage(Message.get("cmdNoPermission"));
    			}else{
    				cmd.get(0).execute(sender, args);
    			}
    			return true;
    		}
    		
    		sender.sendMessage(Message.get("cmdUnknown"));			
    	}
		return true;
	}

	
	/**
	 * Tab completes the base command.
	 * @see TabCompleter
	 * @see Command
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		List<String> completions = new ArrayList<String>();
		SubCommand argOne = args.length < 1 ? null : (getCommands(args[0], true).size() == 1 ? getCommands(args[0], true).get(0) : null);
		if(args.length == 0 || args.length == 1){
			List<SubCommand> cmd = args.length == 0 ? _subCommands : getCommands(args[0], false);
			
			for(SubCommand cmds : cmd){
				completions.add(cmds.getName());
			}
		}
		else if(args.length >= 1 && argOne != null){
			//Pass tab completion on to subclass
			completions = argOne.tabComplete(sender, args);
		}
		
		if(completions == null){
			completions = new ArrayList<String>();
		}
		
		return completions;
	}
	
	/**
	 * Register a base command as a command executor
	 * @param cmd The command which this instance represents.
	 */
	public void register(PluginCommand cmd){
		Validate.notNull(cmd, "The plugin command is null.");
		
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
	}

	@Override
	public boolean onCommand(Player sender, PreprocessableCommand command,
			String label, String[] args) {
		if(args.length == 0 || (args.length == 2 && Utilities.Arguments.parseInt(args[1], -1) > 0 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))) || (args.length == 1 && (Utilities.Arguments.parseInt(args[0], -1) > 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")))){
    		//Show help manual and quit
    		int page = 0;
    		if(args.length == 2){
    			try{
    				page = Integer.parseInt(args[1]) - 1;
    			}catch(Throwable thr){
    				//Ignore
    				page = 0;
    			}
    		}else if(args.length == 1 && !args[0].equalsIgnoreCase("help")){
    			try{
    				page = Integer.parseInt(args[0]) - 1;
    			}catch(Throwable thr){
    				//Ignore
    				page = 0;
    			}
    		}
    		
    		if(page < 0){
    			page *= -1;
    		}
    		
    		sender.sendMessage(String.format(ChatColor.AQUA + _helpPageHeader, page + 1));
    		
    		if(page * getConfig().getInt("commandsPerPage") > _subCommands.size()){
    			return true;
    		}
    		
    		for(int i = page * getConfig().getInt("commandsPerPage"); (i < ((page + 1) * getConfig().getInt("commandsPerPage")) && i < _subCommands.size()); i++){
    			sender.sendMessage(Message.get("cmdHelpEntry").replace("%basecommand%", label).replace("%usage%", _subCommands.get(i).getUsage()).replace("%desc%", _subCommands.get(i).getDescription()));
    		}
    		
    		if(((page + 1) * getConfig().getInt("commandsPerPage")) < _subCommands.size()){
    			sender.sendMessage(Message.get("cmdHelpSeeMore").replace("%basecommand%", label).replace("%page%", Integer.valueOf(page + 2).toString()));
    		}
    		
    		return true;
    	}else if(args.length >= 1){
    		List<SubCommand> cmd = getCommands(args[0], true);
    		
    		if(cmd.size() == 1){
    			if(!cmd.get(0).hasAccess(sender)){
    				sender.sendMessage(Message.get("cmdNoPermission"));
    			}else{
    				cmd.get(0).execute(sender, args);
    			}
    			return true;
    		}
    		
    		sender.sendMessage(Message.get("cmdUnknown"));			
    	}
		return true;
	}

}
