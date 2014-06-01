package me.pagekite.glen3b.library.bukkit.command.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import me.pagekite.glen3b.library.bukkit.GBukkitCorePlugin;
import me.pagekite.glen3b.library.bukkit.Utilities;
import me.pagekite.glen3b.library.bukkit.Utilities.Effects.Particle;
import me.pagekite.glen3b.library.bukkit.command.CommandInvocationContext;
import me.pagekite.glen3b.library.bukkit.command.CommandSenderType;
import me.pagekite.glen3b.library.bukkit.command.PreprocessableCommand;
import me.pagekite.glen3b.library.bukkit.command.PreprocessedCommandHandler;
import me.pagekite.glen3b.library.bukkit.datastore.Message;
import me.pagekite.glen3b.library.bukkit.reflection.PrimitiveType;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents a parent command, which can encompass ase commands.
 * @author Glen Husman
 */
public abstract class ParentCommand implements TabExecutor, PreprocessedCommandHandler {

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
		private boolean _continualStringAtEnd = false;

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
				List<Predicate<? super CommandSender>> anded = Lists.newArrayListWithExpectedSize(3);
				anded.add(Predicates.<CommandSender>notNull());
				if(annotation.permission() != null && !annotation.permission().isEmpty()){
					anded.add(Utilities.Predicates.hasPermission(annotation.permission()));
				}

				List<Predicate<? super CommandSender>> or = Lists.newArrayListWithCapacity(annotation.allowedSenders().length);

				Class<? extends CommandSender> superclass = CommandSender.class;

				if(annotation.allowedSenders().length == 0){
					// Assume all senders allowed
					or.add(Utilities.Predicates.isOfSenderType(CommandSenderType.ALL));
				}else if(annotation.allowedSenders().length == 1){
					or.add(Utilities.Predicates.isOfSenderType(annotation.allowedSenders()[0]));
					superclass = annotation.allowedSenders()[0].getSenderType();
				}else{
					for(CommandSenderType t : annotation.allowedSenders()){
						if(t == CommandSenderType.ALL){
							throw new IllegalStateException("The allowed command sender types on " + method.toString() + " include ALL, but ALL is not the only element.");
						}
						or.add(Utilities.Predicates.isOfSenderType(t));
					}
				}

				anded.add(Predicates.or(or));


				if(_params[0] != superclass && _params[0] != CommandSender.class && !CommandInvocationContext.class.isAssignableFrom(_params[0])){
					// The appropriate instance cannot be passed in to this method
					throwIllegalFirstArg();
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
				String alias = "argument";
				boolean optional = false;
				if(!getSupportedParameterTypes().contains(_params[i]) && !_params[i].isEnum()){
					throw new IllegalStateException(_params[i].toString() + " is not a supported parameter type.");
				}

				boolean continualStrArg = false;

				for(Annotation a : paramAnnot){
					if(a instanceof Argument){
						Argument annot = (Argument)a;
						if(annot.name() != null){
							alias = annot.name();
							continualStrArg = annot.spaces(); // TODO: Instead of this, allow arrays of all supported types to be space delimited
						}
					}else if(a instanceof Optional){
						optional = ((Optional)a).optional();
					}
				}

				_optionals[i] = optional;

				if(i < _params.length - 1 && continualStrArg){
					throw new IllegalStateException(getMethod().toString() + " specified a continual string argument, but it is not the last parameter.");
				}else if(continualStrArg){
					// If it IS the last element
					_continualStringAtEnd = true;
				}

				if(continualStrArg && _params[i] != String.class){
					throw new IllegalStateException(getMethod().toString() + " specified a continual string argument, but the argument in question is not a string.");
				}

				if(optional){
					prevOptional = true;
					_optionalCt++;
				}else if(prevOptional){
					throw new IllegalStateException("Parameter of type " + _params[i].toString() + " is required, but follows an optional parameter. All parameters after an optional parameter must be optional.");
				}

				helpMessage.append(optional ? '[' : '<');
				helpMessage.append(alias);
				if(i == _params.length - 1 && continualStrArg){
					helpMessage.append("...");
				}
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

		private void execute(CommandSender sender, Object arg0, String[] args){
			// Assume predicate has been fulfilled
			Object[] methodArgs = new Object[_params.length];
			methodArgs[0] = arg0;
			if((args.length <= methodArgs.length || _continualStringAtEnd) && args.length >= methodArgs.length - _optionalCt){
				for(int i = 1; i < _params.length; i++){
					try{
						if(_continualStringAtEnd && i == _params.length - 1){
							StringBuilder arg = new StringBuilder();
							for(int j = i; j < args.length; j++){
								arg.append(args[j]);
								if(j != args.length - 1){
									arg.append(' ');
								}
							}
							methodArgs[i] = arg.toString();
						}else{
							methodArgs[i] = parseParameter(containsIndex(i, args) ? args[i] : null, _params[i]);
						}

						if(!containsIndex(i, args) && !_optionals[i]){
							sender.sendMessage(Message.get("cmdNotEnoughArgs"));
							break;
						}
					}catch(IllegalArgumentException except){
						// Error parsing argument
						Bukkit.getLogger().log(Level.FINE, "Couldn't parse an argument.", except);
						sender.sendMessage(Message.get("cmdInvalidArg"));
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
				} catch (Exception e) {
					// I've taken lots of safeguards, so this code SHOULD never be called
					// However, if it is called, I want Bukkit to handle it
					// Bukkit will log it properly and display the "An internal error occurred..." message
					// Which is the case: Unexpected internal error
					// Therefore, proper behavior is to rethrow the exception
					//
					// Also, an InvocationTargetException means that the clients method threw the exceptiom
					// Therefore it should be handed up to Bukkit (like we've previously been doing)
					throw new RuntimeException("An error occured while reflecting a command method within ParentCommand.", e); // Bukkit will display the appropriate error message to the sender and will log the error
				}
			}else{
				sender.sendMessage(Message.get("cmdNotEnoughArgs"));
			}
		}

		public void execute(CommandSender sender, Command cmd, String alias, String[] args){
			// Assume predicate has been fulfilled
			execute(sender, _params[0].isAssignableFrom(CommandInvocationContext.class) ? new CommandInvocationContext<CommandSender, Command>(sender, cmd, alias) : sender, args); // Only works due to generics not being safe in Java
		}

		public void execute(Player sender, PreprocessableCommand cmd, String alias, String[] args){
			// Assume predicate has been fulfilled
			execute(sender, _params[0].isAssignableFrom(CommandInvocationContext.class) ? new CommandInvocationContext<CommandSender, PreprocessableCommand>(sender, cmd, alias) : sender, args); // Only works due to generics not being safe in Java
		}
	}

	private boolean _subclassInitializedSets = false;
	private boolean _inSetInitializer = false;
	private Set<Class<?>> _supportedParamTypes;
	private Map<Class<?>, Object> _defaultParamValues;

	/**
	 * Called by the superclass during execution of the superclass constructor when, if applicable, the subclass is expected to add its own parameter types (that it supports) to the appropriate sets and maps.
	 * Initializing the supported and default parameter collections at any point other than in this method will result in an {@link IllegalStateException} during initialization because they were initialized too late.
	 * <p>
	 * If the subclass does not add support for additional parameter types beyond the default, this method does not have to be overriden.
	 * <p>
	 * The default implementation of this method is <i>not</i> where the default supported types are added to the collections. Therefore, if extending {@code ParentCommand} directly, it is unneccesary to call the superclass method.
	 * The default implementation of this method does nothing.
	 * <p>
	 * It is not necessary to add enum types to the sets directly in this method, as they are parsed automatically by the {@code ParentCommand} implementation of {@link #parseParameter(String, Class)}.
	 * Enum types do not need to be added to this set, however if parsing behavior for enum types is overriden in the subclass implementation of {@link #parseParameter(String, Class)}, it is expected that the type will be found in the set.
	 * <p>
	 * Any call to {@link ParentCommand#getSupportedParameterTypes()} or {@link ParentCommand#getDefaultParameterValues()} within this method would result in infinite recursion, however a safeguard is in place against this.
	 * Please use the parameters which represent these variables instead of using those methods.
	 * @param paramTypes A reference to the set of parameter types that are supported for parsing by this class.
	 * @param defaultParams A reference to the map that maps parameter type values to default instances to use (if the argument is not specified). {@code null} is the default if it is not in this map, so reference types need not be added to this set unless the implementation wishes to provide custom default values for reference types.
	 */
	protected void initializeParameterTypes(Set<Class<?>> paramTypes, Map<Class<?>, Object> defaultParams){
		// Default to doing nothing
	}

	/**
	 * Represents the default subcommand, which displays command help. This method is always implemented by the {@code ParentCommand} class for consistency. It is also required that {@code ParentCommand} implements this method because it requires raw access to the list of registered subcommands.
	 */
	@CommandMethod(aliases = { "help", "?" }, description = "Displays help for this command.")
	public final void helpCommand(CommandInvocationContext<CommandSender, ?> context, @Optional @Argument(name = "page") int page){

		if(page == 0){
			page = 1;
		}

		if(page < 0){
			page *= -1;
		}

		page--; // Convert to zero-based

		context.getSender().sendMessage(String.format(getHelpHeader(), page + 1));

		if(page * getConfig().getInt("commandsPerPage") > _commands.size()){
			return;
		}

		for(int i = page * getConfig().getInt("commandsPerPage"); (i < ((page + 1) * getConfig().getInt("commandsPerPage")) && i < _commands.size()); i++){
			context.getSender().sendMessage(Message.get("cmdHelpEntry").replace("%basecommand%", context.getInvocationAlias()).replace("%usage%", _commands.get(i).getHelpMessage()).replace("%desc%", _commands.get(i).getDescription()));
		}

		if(((page + 1) * getConfig().getInt("commandsPerPage")) < _commands.size()){
			context.getSender().sendMessage(Message.get("cmdHelpSeeMore").replace("%basecommand%", context.getInvocationAlias()).replace("%page%", Integer.valueOf(page + 2).toString()));
		}
	}

	/**
	 * Get a {@code String} which is used as the header of help pages for this command. This string is passed as a format string, with a formatting argument passed to {@link java.lang.String#format(String, Object...) String.format} of one integral value. Simpler implementations can assume that the first instance of "%d" within the string will be replaced with the page number. More complex visual displays may wish to use other formatting options with this format string.
	 * The code which uses this method does not provide coloring by default. The default implementation uses a color of {@link org.bukkit.ChatColor#AQUA aqua} as the prefix, but this will not appear without inclusion in the returned string. Subclasses are free to choose any color they wish for the help header.
	 * @return The help page header for this command.
	 * @see java.lang.String#format(String, Object...)
	 */
	protected String getHelpHeader(){
		return ChatColor.AQUA + "Help (page %d):";
	}

	/**
	 * Creates a parent command instance. For this class to successfully execute a command, {@link ParentCommand#register(PluginCommand)} must be invoked. This constructor will iterate through all methods on the runtime class of this instance to determine which ones represent a subcommand, as determined by the {@link CommandMethod} annotation.
	 */
	public ParentCommand() {
		_aliasesToCommands = new HashMap<String, AnnotatedCommandInfo>();
		_commands = new ArrayList<AnnotatedCommandInfo>();
		// Loop so if client class A extends ParentCommand and client class B extends A, then all command methods inherited from A are registered in B
		for (Class<?> clazz = getClass(); clazz != null; clazz = clazz.getSuperclass())
		{
			for(Method m: clazz.getDeclaredMethods()) {
				if(m.isAnnotationPresent(CommandMethod.class)) {
					AnnotatedCommandInfo info = new AnnotatedCommandInfo(m);
					CommandMethod annotation = m.getAnnotation(CommandMethod.class);
					if(annotation.aliases().length == 0){
						throw new IllegalStateException("There are no aliases for the command specified by " + m.toString());
					}
					for(String alias : annotation.aliases()){
						if(alias == null){ // TODO: Support one null alias, which represents the base command
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
		}
	}

	/**
	 * Register this parent command to handle execution of the specified plugin command.
	 * @param cmd A command which this instance represents.
	 */
	public final void register(PluginCommand cmd){
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

	private GBukkitCorePlugin _plugin;

	/**
	 * Get the GBukkitCore plugin.
	 */
	private GBukkitCorePlugin getPlugin(){
		if(_plugin == null || !_plugin.isEnabled()){
			_plugin = (GBukkitCorePlugin)Bukkit.getServer().getPluginManager().getPlugin("GBukkitCore");
		}

		return _plugin;
	}

	/**
	 * Get the GBukkitCore config file.
	 */
	private FileConfiguration getConfig(){
		return getPlugin().getConfig();
	}

	/**
	 * Returns a reference to the mutable map of supported method parameter types to default values for those types. If a {@code Class} is not contained in this set, {@code null} is assumed to be the default value.
	 * @return The default values, which are used when an argument is not specified, for specific types. If a {@code Class} is not a key in this set, {@code null} is the default value. 
	 */
	protected final Map<Class<?>, Object> getDefaultParameterValues() {
		checkInitSets();
		return _defaultParamValues;
	}

	/**
	 * Checks if the local parameter type and default parameter value collections have been initialized properly.
	 */
	private synchronized void checkInitSets(){
		if(_inSetInitializer){
			throw new IllegalStateException("The subclass initializer has not completed execution. Calls to this method are not supported during initialization of the internal collections.");
		}

		if(_supportedParamTypes == null){
			_subclassInitializedSets = false;
			_supportedParamTypes = Sets.<Class<?>>newHashSet(String.class,
					int.class, Integer.class,
					Double.class, double.class, boolean.class, Boolean.class,
					float.class, Float.class, char.class, Character.class,
					long.class, Long.class, short.class, Short.class,
					Player.class, OfflinePlayer.class, Material.class, Particle.class);
		}

		if(_defaultParamValues == null){
			// Even with the introduction of the PrimitiveType enum, subclasses still may wish to add in their own default values
			// or possibly override our defaults completely
			_subclassInitializedSets = false;
			_defaultParamValues = Maps.<Class<?>, Object>newHashMap();
			for(PrimitiveType primitive : PrimitiveType.values()){
				if(primitive == PrimitiveType.VOID){
					// We don't need to put too many nulls in the map
					continue;
				}

				// Java should automatically do the boxing/unboxing
				_defaultParamValues.put(primitive.getPrimitive(), primitive.getDefaultValue());
				_defaultParamValues.put(primitive.getWrapper(), primitive.getDefaultValue());
			}
		}

		if(!_subclassInitializedSets){
			_inSetInitializer = true;
			initializeParameterTypes(_supportedParamTypes, _defaultParamValues);
			_subclassInitializedSets = true;
			_inSetInitializer = false;
		}
	}

	/**
	 * Returns a reference to the mutable set of supported method parameter types. If a {@code Class} is contained in this set, it is expected that {@link ParentCommand#parseParameter(String,Class)} will be able to return an object of that type, assuming the string is in the proper format.
	 * @return The supported method parameter types. 
	 */
	protected final Set<Class<?>> getSupportedParameterTypes() {
		checkInitSets();

		return _supportedParamTypes;
	}

	/**
	 * Parses an argument, written in human-readable string form, to be of the specified type. Methods which derive from this method should call the superclass method as an attempted parse <em>after</em> attempting to parse the argument themselves, as the superclass call chain will ultimately throw the appropriate exception if no derived class can parse the argument.
	 * @param argument The argument in string form to parse. If this value is {@code null}, the default value in the Java compiler for fields of type {@code type} should be returned. This is {@code null} for reference types, {@code 0} for most numerical types, and {@code false} for booleans.
	 * @param type The {@code Class} of the argument that is being parsed. If this value is equivalent to {@code String.class}, it is expected that the argument itself is returned.
	 * @return {@code argument} represented as an instance of {@code type}.
	 * @throws IllegalArgumentException If {@code argument} is not deserializable to an instance of type. This exception should <i>not</i> be thrown if {@code argument} is {@code null}, in which case {@code null} (or the appropriate default) should be returned. However, it <i>must</i> be thrown if {@code type} is {@code null}.
	 * @throws UnsupportedOperationException If the specified {@code type} cannot be deserialized by this method.
	 */
	@SuppressWarnings({ "deprecation", "unchecked" }) // Needed to get players by name, for enumeration value parsing, and for material match by ID (matchMaterial is not deprecated, but using it for ID match is)
	protected Object parseParameter(String argument, Class<?> type) throws IllegalArgumentException, UnsupportedOperationException {
		if(type == null){
			throw new IllegalArgumentException("The specified type is null.");
		}

		if(argument == null){
			return getDefaultParameterValues().get(type);
		}

		if(type == String.class){
			return argument;
		}else if(type == Boolean.class || type == boolean.class){
			String vt = argument.trim().toLowerCase();

			if(vt.equals("true") || vt.equals("yes") || vt.equals("y") || vt.equals("on")){
				return type == Boolean.class ? Boolean.TRUE : true;
			}else if(vt.equals("false") || vt.equals("no") || vt.equals("n") || vt.equals("off")){
				return type == Boolean.class ? Boolean.FALSE : false;
			}else{
				throw new IllegalArgumentException(argument + " is not a boolean.");
			}
		}else if(type == Integer.class || type == int.class){
			int val = Integer.parseInt(argument.toLowerCase().trim());
			return type == Integer.class ? Integer.valueOf(val) : val;
		}else if(type == Double.class || type == double.class){
			double val = Double.parseDouble(argument.toLowerCase().trim());
			return type == Double.class ? Double.valueOf(val) : val;
		}else if(type == Float.class || type == float.class){
			float val = Float.parseFloat(argument.toLowerCase().trim());
			return type == Float.class ? Float.valueOf(val) : val;
		}else if(type == char.class || type == Character.class){
			if(argument.length() == 1){
				return type == char.class ? argument.charAt(0) : Character.valueOf(argument.charAt(0));
			}else if(argument.length() == 3 && ((argument.charAt(0) == '\'' && argument.charAt(2) == '\'') || (argument.charAt(0) == '"' && argument.charAt(2) == '"'))){
				return type == char.class ? argument.charAt(1) : Character.valueOf(argument.charAt(1));
			}else{
				throw new IllegalArgumentException("The specified argument does not represent a single character.");
			}
		}else if(type == Long.class || type == long.class){
			long val = Long.parseLong(argument.toLowerCase().trim());
			return type == Long.class ? Long.valueOf(val) : val;
		}else if(type == Short.class || type == short.class){
			short val = Short.parseShort(argument.toLowerCase().trim());
			return type == Short.class ? Short.valueOf(val) : val;
		}else if(type == Player.class){
			return Bukkit.getPlayer(argument);
		}else if(type == OfflinePlayer.class){
			return Bukkit.getOfflinePlayer(argument);
		}else if(type == Particle.class){
			return Particle.fromName(argument);
		}else if(type == Material.class){
			Material returnValue = Material.matchMaterial(argument.trim().replace('-', '_'));
			if(returnValue != null){
				return returnValue;
			}
			throw new IllegalArgumentException("The specified argument does not represent a material.");
		}else if(type.isEnum()){
			return Enum.valueOf(type.asSubclass(Enum.class), argument);
		}

		throw new UnsupportedOperationException("The type " + type.getName() + " could not be parsed as a parameter.");
	}

	@Override
	public final boolean onCommand(CommandSender sender,
			Command command, String alias,
			String[] args) {
		if(args.length >= 1){
			AnnotatedCommandInfo i = _aliasesToCommands.containsKey(args[0]) ? _aliasesToCommands.get(args[0]) : null;

			if(i != null){
				if(!i.getAccessRequirement().apply(sender)){
					sender.sendMessage(Message.get("cmdNoPermission"));
				}else{
					// Execute the command!
					i.execute(sender, command, alias, args);
				}
				return true;
			}else{
				if(args.length == 1 && Utilities.Arguments.parseInt(args[0], -1) > 0){
					// Assume help command
					helpCommand(new CommandInvocationContext<CommandSender, Command>(sender, command, alias), Integer.parseInt(args[0]));
				}else{
					sender.sendMessage(Message.get("cmdUnknown"));
				}
			}
		}else{
			helpCommand(new CommandInvocationContext<CommandSender, Command>(sender, command, alias), 1);
		}
		return true;
	}

	@Override
	public final boolean onCommand(Player sender, PreprocessableCommand command,
			String alias, String[] args) {
		if(args.length >= 1){
			AnnotatedCommandInfo i = _aliasesToCommands.containsKey(args[0]) ? _aliasesToCommands.get(args[0]) : null;

			if(i != null){
				if(!i.getAccessRequirement().apply(sender)){
					sender.sendMessage(Message.get("cmdNoPermission"));
				}else{
					// Execute the command!
					i.execute(sender, command, alias, args);
				}
				return true;
			}else{
				if(args.length == 1 && Utilities.Arguments.parseInt(args[0], -1) > 0){
					// Assume help command
					helpCommand(new CommandInvocationContext<CommandSender, PreprocessableCommand>(sender, command, alias), Integer.parseInt(args[0]));
				}else{
					sender.sendMessage(Message.get("cmdUnknown"));
				}
			}
		}else{
			helpCommand(new CommandInvocationContext<CommandSender, PreprocessableCommand>(sender, command, alias), 1);
		}
		return true;
	}

}
