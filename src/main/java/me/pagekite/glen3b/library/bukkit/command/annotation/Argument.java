package me.pagekite.glen3b.library.bukkit.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation represents that an argument of a command method should have an alias, other than the type name, displayed within the help menu.
 * @author Glen Husman
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Argument {

	/**
	 * Gets the help page alias for this argument.
	 */
	String name();
	
	/**
	 * Determines whether to allow a space-delimited string as this argument.
	 */
	boolean spaces() default false;
	
}
