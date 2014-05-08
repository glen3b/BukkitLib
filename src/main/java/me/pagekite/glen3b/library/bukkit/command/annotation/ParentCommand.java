/**
 * 
 */
package me.pagekite.glen3b.library.bukkit.command.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.pagekite.glen3b.library.bukkit.GBukkitLibraryPlugin;
import me.pagekite.glen3b.library.bukkit.Utilities;
import me.pagekite.glen3b.library.bukkit.datastore.Message;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

/**
 * Represents a parent command, which can encompass ase commands.
 * @author Glen Husman
 */
public abstract class ParentCommand implements TabExecutor {

	private Map<String, AnnotatedCommandInfo> _aliasesToCommands;
	private List<AnnotatedCommandInfo> _commands;

	private final class AnnotatedCommandInfo{
		private Method _method;
		private Predicate<CommandSender> _accessPredicate;
		private String _helpMessage;
		public Method getMethod(){
			return _method;
		}

		public Predicate<CommandSender> getAccessRequirement(){
			return _accessPredicate;
		}

		public String getHelpMessage(){
			return _helpMessage;
		}

		private void throwIllegalFirstArg(){
			throw new IllegalStateException(_method.getName() + " does not accept a legal first argument.");
		}

		public String getDescription(){
			return _description;
		}

		private Class<?>[] _params;

		private String _description;

		private int _optionalCt = 0;
		private boolean[] _optionals; // Map _params index values to boolean indicating if optional

		public AnnotatedCommandInfo(Method method){
			_method = method;

			_params = method.getParameterTypes();
			if(_params.length <= 0){
				// Does not accept a command sender
				throwIllegalFirstArg();
			}

			CommandMethod cmdAnnotation = method.getAnnotation(CommandMethod.class);
			_description = cmdAnnotation.description();

			if(_description == null){
				throw new IllegalStateException(method.getName() + " has a null description.");
			}

			if(method.isAnnotationPresent(Access.class)){
				Access annotation = method.getAnnotation(Access.class);
				List<Predicate<CommandSender>> anded = Lists.newArrayListWithExpectedSize(3);
				anded.add(Predicates.<CommandSender>notNull());
				if(annotation.permission() != null){
					anded.add(Utilities.hasPermissionPredicate(annotation.permission()));
				}
				if(annotation.playersOnly()){
					anded.add(Utilities.playerPredicate());
					if(!_params[0].isAssignableFrom(Player.class)){
						// A Player instance cannot be passed in to this method
						throwIllegalFirstArg();
					}
				}else{
					if(!_params[0].isAssignableFrom(CommandSender.class)){
						// A CommandSender instance cannot be passed in to this method
						throwIllegalFirstArg();
					}
				}
				_accessPredicate = Predicates.and(anded);
			}else{
				_accessPredicate = Predicates.notNull();
			}

			// Build help message here so we don't have to compute it on each execution
			StringBuilder helpMessage = new StringBuilder(cmdAnnotation.aliases()[0]);
			helpMessage.append(' ');
			Annotation[][] paramsAnnotations = method.getParameterAnnotations();
			boolean prevOptional = false;
			_optionals = new boolean[_params.length];
			_optionals[0] = false;
			for(int i = 1 /* Exclude CommandSender param */; i < _params.length; i++){
				Annotation[] paramAnnot = paramsAnnotations[i];
				String alias = null;
				boolean optional = false;
				if(_params[i] == String.class){
					// String parameters are easy
					alias = "string";
				}else if(_params[i] == int.class || _params[i] == Integer.class){
					// Integer parameters are supported
					alias = "integer";
				}else if(_params[i] == double.class || _params[i] == Double.class){
					// Double parameters are supported
					alias = "number";
				}else if(_params[i] == boolean.class || _params[i] == Boolean.class){
					// Boolean parameters are supported
					alias = "yes/no";
				}else{
					// TODO: Let user specify own parameter types
					throw new IllegalStateException(_params[i].toString() + " is not a supported parameter type.");
				}

				for(Annotation a : paramAnnot){
					if(a instanceof ArgumentAlias){
						ArgumentAlias annot = (ArgumentAlias)a;
						if(annot.alias() != null){
							alias = annot.alias();
						}
					}else if(a instanceof Optional){
						optional = ((Optional)a).optional();
					}
				}

				_optionals[i] = optional;

				if(optional){
					prevOptional = true;
					_optionalCt++;
				}else if(prevOptional){
					throw new IllegalStateException("Parameter of type " + _params[i].toString() + " is required, but follows an optional parameter. All parameters after an optional parameter must be optional.");
				}

				helpMessage.append(optional ? '[' : '<');
				helpMessage.append(alias);
				helpMessage.append(optional ? ']' : '>');

				if(i != _params.length - 1){
					helpMessage.append(' ');
				}
			}

			_helpMessage = helpMessage.toString();
		}

		private <T> boolean containsIndex(int index, T[] arr){
			return arr != null && index >= 0 && index < arr.length;
		}

		private boolean parseBool(String val) throws IllegalArgumentException{
			String vt = val.trim().toLowerCase();
			if(vt.equals("true") || vt.equals("yes") || vt.equals("y") || vt.equals("on")){
				return true;
			}else if(vt.equals("false") || vt.equals("no") || vt.equals("n") || vt.equals("off")){
				return false;
			}else{
				throw new IllegalArgumentException(val + " is not a boolean.");
			}
		}

		public void execute(CommandSender sender, String... args){
			// Assume predicate has been fulfilled
			Object[] methodArgs = new Object[_params.length];
			methodArgs[0] = sender;

			if(args.length <= methodArgs.length - 1 && args.length >= (methodArgs.length - _optionalCt) - 1){
				for(int i = 1; i < _params.length; i++){
					try{
						if(_params[i] == String.class){
							// String parameters are easy
							methodArgs[i] = containsIndex(i - 1, args) ? args[i - 1] : null;
						}else if(_params[i] == int.class || _params[i] == Integer.class){
							// Integer parameters are supported
							methodArgs[i] = containsIndex(i - 1, args) ? Integer.parseInt(args[i - 1]) : 0;
						}else if(_params[i] == double.class || _params[i] == Double.class){
							// Double parameters are supported
							methodArgs[i] = containsIndex(i - 1, args) ? Double.parseDouble(args[i - 1]) : 0.0;
						}else if(_params[i] == boolean.class || _params[i] == Boolean.class){
							// Boolean parameters are supported
							methodArgs[i] = containsIndex(i - 1, args) ? parseBool(args[i - 1]) : false;
						}
						
						if(!containsIndex(i - 1, args) && !_optionals[i]){
							// TODO: Show proper "not enough args" message
							sender.sendMessage(Message.get("cmdUnknown"));
							break;
						}
					}catch(IllegalArgumentException except){
						// Error parsing argument
						// TODO: Show proper "invalid blah" error message
						sender.sendMessage(Message.get("cmdUnknown"));
						break;
					}
				}
				
				// Method arguments have been computed, we are ready to execute!
				try {
					Object returnVal = getMethod().invoke(ParentCommand.this, methodArgs);
					if(returnVal != null && returnVal instanceof CharSequence){
						// Interpret as a message
						sender.sendMessage(returnVal.toString());
					}
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO: Better handling of reflective exceptions
					// I've taken lots of safeguards, so this code SHOULD never be called
					// However, if it is called, I want Bukkit to handle it
					// Bukkit will log it properly and display the "An internal error occurred..." message
					// Which is the case: Unexpected internal error
					// Therefore, proper behavior is to rethrow the exception
					throw new RuntimeException("An error occured while reflecting the appropriate command method within ParentCommand.", e);
				}
			}else{
				// Wrong number? TODO: Show proper "not enough args" message
				sender.sendMessage(Message.get("cmdUnknown"));
			}
		}
	}

	/**
	 * Creates a parent command instance. For this class to successfully execute a command, {@link ParentCommand#register(PluginCommand)} must be invoked. This constructor will iterate through all methods on the runtime class of this instance to determine which ones represent a subcommand, as determined by the {@link CommandMethod} annotation.
	 */
	public ParentCommand() {
		_aliasesToCommands = new HashMap<String, AnnotatedCommandInfo>();
		_commands = new ArrayList<AnnotatedCommandInfo>();
		Class<?> clazz = getClass();
		// Loop so if client class A extends ParentCommand and client class B extends A, then all command methods inherited from A are registered in B
		while(clazz != null && clazz != ParentCommand.class && ClassUtils.isAssignable(clazz, ParentCommand.class)){
			for(Method m: clazz.getDeclaredMethods()) {
				if(m.isAnnotationPresent(CommandMethod.class)) {
					AnnotatedCommandInfo info = new AnnotatedCommandInfo(m);
					CommandMethod annotation = m.getAnnotation(CommandMethod.class);
					if(annotation.aliases().length == 0){
						throw new IllegalStateException("There are no aliases for the command specified by " + m.toString());
					}
					for(String alias : annotation.aliases()){
						if(alias == null){
							throw new IllegalStateException("An alias for the command specified by " + m.toString() + " is null.");
						}

						AnnotatedCommandInfo prevVal = _aliasesToCommands.put(alias.toLowerCase(), info);

						if(prevVal != null){
							throw new IllegalStateException("The alias '" + alias + "' for the command specified by " + m.toString() + " conflicts with the alias of the same name speciifed by " + prevVal.getMethod().toString());
						}
					}
					_commands.add(info);
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * Register this parent command to handle execution of the specified plugin command.
	 * @param cmd A command which this instance represents.
	 */
	public void register(PluginCommand cmd){
		Validate.notNull(cmd, "The plugin command is null.");

		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
	}

	/**
	 * Attempts to tab complete this command. The general contract of this method is that if the superclass implementation of this method returns {@code null}, it is the subclasses responsibility to tab complete the command.
	 * @param sender The sender of the command.
	 * @param command The command being tab completed.
	 * @param alias The currently used alias of the command being tab completed.
	 * @param args The arguments as typed so far.
	 * @return All possibilities for the tab completion.
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender,
			Command command, String alias,
			String[] args) {
		if(args.length == 1){
			return StringUtil.copyPartialMatches(args[0], _aliasesToCommands.keySet(), new ArrayList<String>(_aliasesToCommands.size()));
		}
		return null;
	}

	private GBukkitLibraryPlugin _plugin;

	/**
	 * Get the GBukkitLib config file.
	 */
	private FileConfiguration getConfig(){
		if(_plugin == null || !_plugin.isEnabled()){
			// TODO: BaseCommand should not directly depend upon GBukkitLib, maybe global variable (not just message) service?
			_plugin = (GBukkitLibraryPlugin)Bukkit.getServer().getPluginManager().getPlugin("GBukkitLib");
		}

		return _plugin.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender,
			Command command, String alias,
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

			sender.sendMessage(String.format(ChatColor.AQUA + /* TODO Configurable header */ "Help (page %d):", page + 1));

			if(page * getConfig().getInt("commandsPerPage") > _commands.size()){
				return true;
			}

			for(int i = page * getConfig().getInt("commandsPerPage"); (i < ((page + 1) * getConfig().getInt("commandsPerPage")) && i < _commands.size()); i++){
				sender.sendMessage(Message.get("cmdHelpEntry").replace("%basecommand%", alias).replace("%usage%", _commands.get(i).getHelpMessage()).replace("%desc%", _commands.get(i).getDescription()));
			}

			if(((page + 1) * getConfig().getInt("commandsPerPage")) < _commands.size()){
				sender.sendMessage(Message.get("cmdHelpSeeMore").replace("%basecommand%", alias).replace("%page%", Integer.valueOf(page + 2).toString()));
			}

			return true;
		}else if(args.length >= 1){
			AnnotatedCommandInfo i = _aliasesToCommands.containsKey(args[0]) ? _aliasesToCommands.get(args[0]) : null;

			if(i != null){
				if(!i.getAccessRequirement().apply(sender)){
					sender.sendMessage(Message.get("cmdNoPermission"));
				}else{
					// Execute the command!
					i.execute(sender, args);
				}
				return true;
			}

			sender.sendMessage(Message.get("cmdUnknown"));
		}
		return true;
	}

}
