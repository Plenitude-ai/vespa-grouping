import org.junit.Assert;
import org.junit.Test;

// import com.yahoo.vespa.config.search.AttributesConfig;
// import com.yahoo.search.config.ClusterConfig;
import com.yahoo.search.grouping.GroupingQueryParser;
import com.yahoo.component.ComponentId;
import com.yahoo.component.chain.Chain;
import com.yahoo.search.Query;
import com.yahoo.search.result.Hit;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.grouping.vespa.GroupingExecutor;
import com.yahoo.search.searchchain.Execution;

import my_package.DedupSearcher;

public class DedupSearcherTestCase {

    @Test
    public void testDedup() {
        Chain<Searcher> chain = new Chain<Searcher>(new DedupSearcher(),
                // new QueryValidator(),
                // new GroupingValidator(new ClusterConfig(), new AttributesConfig()),
                new GroupingQueryParser(),
                new GroupingExecutor(new ComponentId("grouping")),
                new MockBackend());
        Execution execution = new Execution(chain, Execution.Context.createContextStub());
        Result result = new Execution(chain, execution.context()).search(new Query("?query=test"));

        Assert.assertEquals(6, result.hits().size());
        Assert.assertEquals("news_tech_1", result.hits().get(0).getId().toString());
        Assert.assertEquals("news_us_1", result.hits().get(1).getId().toString());
        Assert.assertEquals("finance_companies_1", result.hits().get(2).getId().toString());
        Assert.assertEquals("news_tech_2", result.hits().get(3).getId().toString());
        Assert.assertEquals("news_us_2", result.hits().get(4).getId().toString());
        Assert.assertEquals("finance_companies_2", result.hits().get(5).getId().toString());
    }

    private static class MockBackend extends Searcher {

        private Hit defineHitExample(String id, String category, String subcategory,
                double relevance) {
            Hit example = new Hit(id);
            example.setField("category", category);
            example.setField("subcategory", subcategory);
            example.setRelevance(relevance);
            return example;
        }

        @Override
        public Result search(Query query, Execution execution) {
            Result result = new Result(query);

            String categ = "news";
            String subcateg = "tech";
            result.hits().add(defineHitExample("news_tech_1", categ, subcateg, 0.99));
            result.hits().add(defineHitExample("news_tech_2", categ, subcateg, 0.89));
            result.hits().add(defineHitExample("news_tech_3", categ, subcateg, 0.79));
            subcateg = "us";
            result.hits().add(defineHitExample("news_us_1", categ, subcateg, 0.98));
            result.hits().add(defineHitExample("news_us_2", categ, subcateg, 0.88));
            result.hits().add(defineHitExample("news_us_3", categ, subcateg, 0.78));
            subcateg = "business";
            result.hits().add(defineHitExample("news_business_1", categ, subcateg, 0.97));
            result.hits().add(defineHitExample("news_business_2", categ, subcateg, 0.87));
            result.hits().add(defineHitExample("news_business_3", categ, subcateg, 0.77));

            categ = "finance";
            subcateg = "companies";
            result.hits().add(defineHitExample("finance_companies_1", categ, subcateg, 0.96));
            result.hits().add(defineHitExample("finance_companies_2", categ, subcateg, 0.86));
            result.hits().add(defineHitExample("finance_companies_3", categ, subcateg, 0.76));

            return result;
        }
    }
}
