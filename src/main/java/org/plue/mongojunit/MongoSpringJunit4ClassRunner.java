package org.plue.mongojunit;

import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * @author p.cortis@sinossi.it
 */
public class MongoSpringJunit4ClassRunner extends SpringJUnit4ClassRunner
{
	private MongodExecutable mongodExecutable;

	public MongoSpringJunit4ClassRunner(final Class<?> clazz) throws InitializationError
	{
		super(clazz);
	}

	@Override
	protected Statement withBeforeClasses(Statement statement)
	{
		Statement result = super.withBeforeClasses(statement);

		try {
			InMemoryMongo annotation = getTestClass().getAnnotation(InMemoryMongo.class);
			startMongoDB(annotation);
		} catch(IOException e) {
			throw new RuntimeException("Cannot start Mongo DB", e);
		}

		return result;
	}

	private void startMongoDB(InMemoryMongo annotation) throws IOException
	{
		MongodStarter starter = MongodStarter.getDefaultInstance();
		Net net = new Net(annotation.port(), Network.localhostIsIPv6());
		IMongodConfig mongodConfig = new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(net)
				.build();

		mongodExecutable = starter.prepare(mongodConfig);
		mongodExecutable.start();
	}

	@Override
	protected Statement withAfterClasses(Statement statement)
	{
		Statement result = super.withAfterClasses(statement);

		if(mongodExecutable != null) {
			mongodExecutable.stop();
		}

		return result;
	}

	@Override
	protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement)
	{
		Statement result = super.withBefores(frameworkMethod, testInstance, statement);

		try {
			MongoImport annotation = getTestClass().getAnnotation(MongoImport.class);
			importMongoDB(annotation);
		} catch(IOException e) {
			throw new RuntimeException("Cannot import Mongo DB", e);
		}

		return result;
	}

	private void importMongoDB(MongoImport annotation) throws IOException
	{
		Net net = new Net(annotation.port(), Network.localhostIsIPv6());
		IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(net)
				.db(annotation.databaseName())
				.collection(annotation.collection())
				.upsert(annotation.upsert())
				.dropCollection(annotation.dropCollection())
				.jsonArray(annotation.jsonArray())
				.importFile(annotation.importFile())
				.build();

		MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig).start();
	}
}
