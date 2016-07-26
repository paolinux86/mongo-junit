package org.plue.mongojunit;

import de.flapdoodle.embed.mongo.distribution.Version;

import java.lang.annotation.*;

/**
 * @author p.cortis@sinossi.it
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface InMemoryMongo
{
	int port() default 27017;

	Version.Main version() default Version.Main.PRODUCTION;
}
