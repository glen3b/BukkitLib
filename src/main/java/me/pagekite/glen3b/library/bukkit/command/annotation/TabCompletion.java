package me.pagekite.glen3b.library.bukkit.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation represents that an argument of a command method should be tab completed in a user-defined manner.
 * @author Glen Husman
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TabCompletion {

	/**
	 * Gets the mode of tab completion to use.
	 */
	TabCompleteMode mode() default TabCompleteMode.PLAYER_LIST;
	
	/**
	 * Gets the array of values to complete with if {@link #mode()} is {@link TabCompleteMode#CONSTANT_LIST}.
	 */
	String[] values() default {};
}
