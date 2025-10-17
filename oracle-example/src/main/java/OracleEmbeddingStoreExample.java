import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.oracle.*;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.testcontainers.oracle.OracleContainer;

import java.sql.SQLException;

public class OracleEmbeddingStoreExample {

    public static void main(String[] args) throws SQLException {

        PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
        dataSource.setConnectionFactoryClassName(
                "oracle.jdbc.datasource.impl.OracleDataSource");
        String urlFromEnv = System.getenv("ORACLE_JDBC_URL");

        if (urlFromEnv == null) {
            OracleContainer oracleContainer = new OracleContainer(
                    "gvenzl/oracle-free:23.4-slim-faststart")
                    .withDatabaseName("pdb1")
                    .withUsername("testuser")
                    .withPassword("testpwd");
            oracleContainer.start();
            dataSource.setURL(oracleContainer.getJdbcUrl());
            dataSource.setUser(oracleContainer.getUsername());
            dataSource.setPassword(oracleContainer.getPassword());

        } else {
            dataSource.setURL(urlFromEnv);
            dataSource.setUser(System.getenv("ORACLE_JDBC_USER"));
            dataSource.setPassword(System.getenv("ORACLE_JDBC_PASSWORD"));
        }

        // 向量相似性搜索将使用暴力搜索
        // 查询时间复杂度为 O(n)，随数据量线性增长
        // 小数据集（<10万条）性能可接受
        EmbeddingStore<TextSegment> embeddingStore = OracleEmbeddingStore.builder()
                .dataSource(dataSource)
                .embeddingTable("test_content_retriever",
                        CreateOption.CREATE_OR_REPLACE)
                .build();

        // 使用更定制化的方式创建向量表
        EmbeddingStore<TextSegment> embeddingStoreByComplicate = OracleEmbeddingStore.builder()
                .dataSource(dataSource)
                .embeddingTable(EmbeddingTable.builder()
                        .createOption(CreateOption.CREATE_OR_REPLACE)
                        .name("my_embedding_table")
                        .idColumn("id_column_name")
                        .embeddingColumn("embedding_column_name")
                        .textColumn("text_column_name")
                        .metadataColumn("metadata_column_name")
                        .build())
                // 倒排文件索引，将向量空间划分为多个聚类，搜索只在最相关的几个聚类中查找
                // 查询时间复杂度为O(log n) 到 O(√n)
                .index(Index.ivfIndexBuilder().createOption(CreateOption.CREATE_IF_NOT_EXISTS).build())
                .build();

        EmbeddingStore<TextSegment> embeddingStoreByComplicate2 = OracleEmbeddingStore.builder()
                .dataSource(dataSource)
                .embeddingTable(EmbeddingTable.builder()
                        .createOption(CreateOption.CREATE_OR_REPLACE)
                        .name("my_embedding_table")
                        .idColumn("id_column_name")
                        .embeddingColumn("embedding_column_name")
                        .textColumn("text_column_name")
                        .metadataColumn("metadata_column_name")
                        .build())
                // 用于元数据字段的快速查询，在metadata列上，支持复合索引
                // 查询时间复杂度O(n)
                .index(Index.jsonIndexBuilder()
                        .createOption(CreateOption.CREATE_OR_REPLACE)
                        .key("name", String.class, JSONIndexBuilder.Order.ASC)
                        .key("year", Integer.class, JSONIndexBuilder.Order.DESC)
                        .build())
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();

        TextSegment segment1 = TextSegment.from("I like soccer.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("I love Stephen King.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        Content match = retriever
                .retrieve(Query.from("What is your favourite sport?"))
                .get(0);

        System.out.println(match.textSegment());
    }
}