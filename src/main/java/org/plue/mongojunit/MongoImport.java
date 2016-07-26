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
public @interface MongoImport
{
	int port() default 27017;

	Version.Main version() default Version.Main.PRODUCTION;

	String databaseName();

	String collection();

	boolean upsert() default false;

	boolean dropCollection() default true;

	boolean jsonArray() default true;

	String importFile();
}
