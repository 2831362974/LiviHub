package com.liviHub.tools;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class CommodityTools {

    private static final Logger logger = LoggerFactory.getLogger(CommodityTools.class);

    @Autowired
    private RestHighLevelClient esClient;

    // 查询请求参数
    public record SearchCommodityRequest(String description, String category, Double minPrice, Double maxPrice) {}

    // 商品详细信息
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CommodityInfo(
            Long id,
            String name,
            String category,
            Long price,
            Integer stock,
            String description,
            Integer status
    ) {}

    // 商品推荐结果
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecommendResult(
            List<CommodityInfo> commodities,
            String message
    ) {}

    @Bean
    @Description("基于ES查询推荐商品")
    public Function<SearchCommodityRequest, RecommendResult> recommendCommodity() {
        return request -> {
            try {
                // 构建查询条件
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                if (request.description() != null && !request.description().isBlank()) {
                    boolQuery.must(QueryBuilders.matchQuery("description", request.description()));
                } else {
                    return new RecommendResult(List.of(), "请提供商品描述信息以便推荐");
                }

                if (request.category() != null && !request.category().isBlank()) {
                    boolQuery.filter(QueryBuilders.termQuery("category.keyword", request.category()));
                }

                if (request.minPrice() != null || request.maxPrice() != null) {
                    RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
                    if (request.minPrice() != null) {
                        rangeQuery.gte(request.minPrice());
                    }
                    if (request.maxPrice() != null) {
                        rangeQuery.lte(request.maxPrice());
                    }
                    boolQuery.filter(rangeQuery);
                }

                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                        .query(boolQuery)
                        .size(10);

                SearchRequest searchRequest = new SearchRequest("commodities").source(sourceBuilder);
                SearchResponse response = esClient.search(searchRequest, RequestOptions.DEFAULT);

                List<CommodityInfo> resultList = new ArrayList<>();
                for (SearchHit hit : response.getHits()) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    CommodityInfo info = new CommodityInfo(
                            Long.parseLong(hit.getId()),
                            (String) source.get("name"),
                            (String) source.get("category"),
                            source.get("price") != null ? Long.valueOf(source.get("price").toString()) : null,
                            (Integer) source.get("stock"),
                            (String) source.get("description"),
                            (Integer) source.get("status")
                    );
                    resultList.add(info);
                }

                String message = resultList.isEmpty() ? "抱歉，没有找到符合条件的商品" : "以下是为您找到的商品";
                return new RecommendResult(resultList, message);

            } catch (Exception e) {
                logger.warn("ES 商品推荐失败: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
                return new RecommendResult(List.of(), "查询过程中发生错误，请稍后重试");
            }
        };
    }
}
