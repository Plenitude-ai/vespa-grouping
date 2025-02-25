import my_package.DedupSearcher;

import org.junit.Test;
import org.junit.Assert;

import com.yahoo.search.searchchain.Execution;
import com.yahoo.search.result.*;
import com.yahoo.search.Searcher;
import com.yahoo.component.chain.Chain;

import com.yahoo.search.Query;
import com.yahoo.search.Result;

public class DedupSearcherTestCase {

    @Test
    public void testDedup() {
        Chain<Searcher> chain = new Chain<Searcher>(new DedupSearcher(), new MockBackend());
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

            HitGroup root = new HitGroup("toplevel");

            String categ = "news";
            String subcateg = "tech";
            root.add(defineHitExample("news_tech_1", categ, subcateg, 0.99));
            root.add(defineHitExample("news_tech_2", categ, subcateg, 0.89));
            root.add(defineHitExample("news_tech_3", categ, subcateg, 0.79));
            subcateg = "us";
            root.add(defineHitExample("news_us_1", categ, subcateg, 0.98));
            root.add(defineHitExample("news_us_2", categ, subcateg, 0.88));
            root.add(defineHitExample("news_us_3", categ, subcateg, 0.78));
            subcateg = "business";
            root.add(defineHitExample("news_business_1", categ, subcateg, 0.97));
            root.add(defineHitExample("news_business_2", categ, subcateg, 0.87));
            root.add(defineHitExample("news_business_3", categ, subcateg, 0.77));

            categ = "finance";
            subcateg = "companies";
            root.add(defineHitExample("finance_companies_1", categ, subcateg, 0.96));
            root.add(defineHitExample("finance_companies_2", categ, subcateg, 0.86));
            root.add(defineHitExample("finance_companies_3", categ, subcateg, 0.76));

            result.setHits(root);
            return result;
        }
    }
}