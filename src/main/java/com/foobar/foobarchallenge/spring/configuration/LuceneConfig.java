package com.foobar.foobarchallenge.spring.configuration;

import io.vavr.control.Try;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class LuceneConfig {
  @SneakyThrows
  @Bean
  Directory memoryIndex() {
    return new ByteBuffersDirectory(new SingleInstanceLockFactory());
  }

  @Bean
  Analyzer analyzer() {
    return new StandardAnalyzer();
  }

  @Bean
  Supplier<IndexWriterConfig> indexWriterSupplier(Analyzer analyzer) {
    return () -> new IndexWriterConfig(analyzer);
  }

  @Bean
  Try.WithResources1<IndexWriter> indexWriter(
      Directory memoryIndex, Supplier<IndexWriterConfig> indexWriterSupplier) {
    return Try.withResources(() -> new IndexWriter(memoryIndex, indexWriterSupplier.get()));
  }

  @Bean
  BiFunction<String[], Analyzer, QueryParser> queryParser() {
    return MultiFieldQueryParser::new;
  }
}
