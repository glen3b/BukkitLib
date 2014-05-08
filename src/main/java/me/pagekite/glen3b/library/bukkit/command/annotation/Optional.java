package me.pagekite.glen3b.library.bukkit.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies whether a parameter to a command is optional. If so, and a parameter is not specified, a default value, such as {@code null}, {@code 0}, or {@code false} will be provided to the method, and it is the responsibility of the command executer to determine default behavior.
 * @author Glen Husman
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Optional {
	/**
	 * Determines whether the parameter is optional. This value defaults to true because if the annotation is present, it should be assumed by default that the programmer means that the parameter is optional.
	 */
	boolean optional() default true;
}
