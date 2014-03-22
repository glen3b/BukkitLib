package me.pagekite.glen3b.library.bukkit.command;

import java.util.List;
import java.util.ArrayList;

import me.pagekite.glen3b.library.bukkit.GBukkitLibraryPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

/**
 * Represents a base command that can execute subcommands
 * @author Glen Husman
 */
public final class BaseCommand implements CommandExecutor {

	private String _helpPageHeader = "Help (page %d):";
	private ArrayList<SubCommand> _subCommands;
	
	/**
	 * Create a base command. For this class to execute a command, {@code getCommand("commandNameInPluginYaml").setExecutor(new BaseCommand(...))} must be called.
	 * @param helpHeader The header to display on help pages, where {@code %d} is substituted with the page number. Color codes must be translated by the caller.
	 * @param commands The array of commands which are executed via this base command.
	 */
	public BaseCommand(String helpHeader, SubCommand... commands){
		if(commands == null || commands.length == 0){
			throw new IllegalArgumentException("Subcommands are required.");
		}
		
		if(helpHeader != null){
			_helpPageHeader = helpHeader;
		}
		
		_subCommands = Lists.newArrayList(commands);
	}
	
	/**
	 * Get a list of subcommands executed by this BaseCommand instance.
	 * @return An {@code ArrayList<SubCommand>} instance that can be manipulated via reference to change the subcommands executed by this base command.
	 */
	public List<SubCommand> getSubCommands(){
		return _subCommands;
	}
	
	private GBukkitLibraryPlugin _plugin;
	
	private FileConfiguration getConfig(){
		if(_plugin == null || !_plugin.isEnabled()){
			_plugin = (GBukkitLibraryPlugin)Bukkit.getServer().getPluginManager().getPlugin("GBukkitLib");
		}
		
		return _plugin.getConfig();
		
	}
	
	/**
	 * Determine if str is an integer.
	 */
	private boolean isInt(String str){
		try{
			Integer.parseInt(str);
		}catch(Throwable thr){
			return false;
		}
    	return true;
    }
	
	/**
	 * Executes the base command, attempting to parse arguments.
	 * @see CommandExecutor
	 * @see Command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if(args.length == 0 || (args.length == 2 && isInt(args[1]) && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))) || (args.length == 1 && (isInt(args[0]) || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")))){
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
    		
    		sender.sendMessage(String.format(_helpPageHeader, ChatColor.AQUA.toString(), page + 1));
    		
    		if(page * getConfig().getInt("cmdPerPage") > _subCommands.size()){
    			return true;
    		}
    		
    		for(int i = page * getConfig().getInt("commandsPerPage"); (i < ((page + 1) * getConfig().getInt("commandsPerPage")) && i < _subCommands.size()); i++){

    			//TODO: Configurable message format
    			sender.sendMessage(ChatColor.GOLD + "/" + label + " " + _subCommands.get(i).getUsage() + ChatColor.GRAY + " - " + ChatColor.YELLOW + _subCommands.get(i).getDescription());
    		}
    		
    		if(((page + 1) * getConfig().getInt("commandsPerPage")) < _subCommands.size()){
    			sender.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GOLD + "/"+ label + " help " + (page + 2) + ChatColor.YELLOW + " to see more commands.");
    		}
    		
    		return true;
    	}else if(args.length >= 1){
    		if(!(sender instanceof Player)){
    			sender.sendMessage(ChatColor.DARK_RED + "Currently only players can run teams commands.");
    			return true;
    		}
    		
    		for(SubCommand cmd : _subCommands){
    			for(String alias : cmd.getAliases()){
    				if(alias != null && args[0].equalsIgnoreCase(alias)){
    					//Execute this command!
    					cmd.execute(sender, args);
    				}
    			}
    		}
    		
    		sender.sendMessage(ChatColor.DARK_RED + "Unknown command.");			
    	}
		return false;
	}

}
