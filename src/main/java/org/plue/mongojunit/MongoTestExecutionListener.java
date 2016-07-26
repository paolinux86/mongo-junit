package org.plue.mongojunit;

import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import java.io.IOException;
import java.net.URL;

/**
 * @author p.cortis@sinossi.it
 */
public class MongoTestExecutionListener implements TestExecutionListener
{
	private MongodExecutable mongodExecutable;

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception
	{
		try {
			Class<?> testClass = testContext.getTestClass();
			InMemoryMongo annotation = testClass.getAnnotation(InMemoryMongo.class);
			startMongoDB(annotation);
		} catch(IOException e) {
			throw new RuntimeException("Cannot start Mongo DB", e);
		}
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception
	{
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
	public void afterTestClass(TestContext testContext) throws Exception
	{
		if(mongodExecutable != null) {
			mongodExecutable.stop();
		}
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception
	{
		try {
			Class<?> testClass = testContext.getTestClass();
			MongoImport annotation = testClass.getAnnotation(MongoImport.class);
			importMongoDB(annotation);
		} catch(IOException e) {
			throw new RuntimeException("Cannot import Mongo DB", e);
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception
	{
	}

	private void importMongoDB(MongoImport annotation) throws IOException
	{
		URL resource = getClass().getClassLoader().getResource(annotation.importFile());

		Net net = new Net(annotation.port(), Network.localhostIsIPv6());
		IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(net)
				.db(annotation.databaseName())
				.collection(annotation.collection())
				.upsert(annotation.upsert())
				.dropCollection(annotation.dropCollection())
				.jsonArray(annotation.jsonArray())
				.importFile(resource.getPath())
				.build();

		MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig).start();
	}
}
