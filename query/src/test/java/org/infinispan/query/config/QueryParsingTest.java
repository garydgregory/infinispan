package org.infinispan.query.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.test.AbstractInfinispanTest;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "query.config.QueryParsingTest")
public class QueryParsingTest extends AbstractInfinispanTest {

   public void testConfigurationFileParsing() throws IOException {
      ParserRegistry parserRegistry = new ParserRegistry(Thread.currentThread().getContextClassLoader());
      ConfigurationBuilderHolder holder = parserRegistry.parseFile("configuration-parsing-test.xml");
      Map<String, ConfigurationBuilder> namedConfigurations = holder.getNamedConfigurationBuilders();
      Configuration defaultConfiguration = namedConfigurations.get("default").build();

      assertEquals(defaultConfiguration.indexing().properties().size(), 0);
      assertFalse(defaultConfiguration.indexing().enabled());

      Configuration simpleCfg = namedConfigurations.get("simple").build();
      assertFalse(simpleCfg.indexing().enabled());
      assertEquals(simpleCfg.indexing().properties().size(), 0);

      Configuration memoryCfg = namedConfigurations.get("memory-searchable").build();
      assertTrue(memoryCfg.indexing().enabled());
      assertEquals(2, memoryCfg.indexing().properties().size());
      assertEquals(memoryCfg.indexing().properties().getProperty("default.directory_provider"), "local-heap");

      Configuration diskCfg = namedConfigurations.get("disk-searchable").build();
      assertTrue(diskCfg.indexing().enabled());
      assertEquals(diskCfg.indexing().properties().size(), 4);
      assertEquals(diskCfg.indexing().properties().getProperty("hibernate.search.default.directory_provider"), "filesystem");
      assertEquals(diskCfg.indexing().properties().getProperty("hibernate.search.cats.exclusive_index_use"), "true");

      Configuration replDefaults = namedConfigurations.get("repl-with-default").build();
      assertTrue(replDefaults.indexing().enabled());
      assertFalse(replDefaults.indexing().properties().isEmpty());
   }

   public void testConfigurationFileParsingWithDefaultEnabled() throws IOException {
      ParserRegistry parserRegistry = new ParserRegistry(Thread.currentThread().getContextClassLoader());
      ConfigurationBuilderHolder holder = parserRegistry.parseFile("configuration-parsing-test-enbledInDefault.xml");
      Map<String, ConfigurationBuilder> namedConfigurations = holder.getNamedConfigurationBuilders();
      Configuration defaultConfiguration = namedConfigurations.get("default").build();

      assertEquals(defaultConfiguration.indexing().properties().size(), 2);
      assertTrue(defaultConfiguration.indexing().enabled());
      assertEquals(defaultConfiguration.indexing().properties().getProperty("hibernate.search.default.directory_provider"), "local-heap");

      Configuration nonSearchableCfg = namedConfigurations.get("not-searchable").build();
      assertFalse(nonSearchableCfg.indexing().enabled());

      Configuration simpleCfg = namedConfigurations.get("simple").build();
      assertTrue(simpleCfg.indexing().enabled());
      assertEquals(simpleCfg.indexing().properties().size(), 2);

      Configuration memoryCfg = namedConfigurations.get("memory-searchable").build();
      assertTrue(memoryCfg.indexing().enabled());
      assertEquals(memoryCfg.indexing().properties().size(), 2);
      assertEquals(memoryCfg.indexing().properties().getProperty("hibernate.search.default.directory_provider"), "local-heap");

      Configuration diskCfg = namedConfigurations.get("disk-searchable").build();
      assertTrue(diskCfg.indexing().enabled());
      assertEquals(diskCfg.indexing().properties().size(), 4);
      assertEquals(diskCfg.indexing().properties().getProperty("hibernate.search.default.directory_provider"), "filesystem");
      assertEquals(diskCfg.indexing().properties().getProperty("hibernate.search.cats.exclusive_index_use"), "true");
   }
}
