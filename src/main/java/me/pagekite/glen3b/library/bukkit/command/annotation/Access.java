package me.pagekite.glen3b.library.bukkit.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import me.pagekite.glen3b.library.bukkit.command.CommandSenderType;

/**
 * This annotation represents access restrictions of a command at runtime.
 * @author Glen Husman
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Access {
	/**
	 * @return A string that represents the permission node required by {@code CommandSender} instances specifying that they are allowed to execute this command.
	 */
	String permission() default "";
	
	/**
	 * @return An array of allowed types of command senders.
	 */
	CommandSenderType[] allowedSenders() default {CommandSenderType.ALL};
}
