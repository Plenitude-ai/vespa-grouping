import my_package.DedupSearcher;

import org.junit.Test;
import org.junit.Assert;

import com.yahoo.search.searchchain.Execution;
import com.yahoo.search.result.*;
import com.yahoo.search.Searcher;
import com.yahoo.search.grouping.Continuation;
import com.yahoo.search.grouping.result.Group;
import com.yahoo.search.grouping.result.GroupList;
import com.yahoo.search.grouping.result.RootGroup;
import com.yahoo.search.grouping.result.StringId;
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

            RootGroup root_group = new RootGroup(0, new Continuation() {
                @Override
                public Continuation copy() {
                    return null;
                }

                @Override
                public String toString() {
                    return "AAAA";
                }
            });
            GroupList root_gl = new GroupList("category");
            root_group.add(root_gl);

            String categ = "news";
            Group news_group = new Group(new StringId(categ), new Relevance(1.0));
            root_gl.add(news_group);
            GroupList news_gl = new GroupList("subcategory");
            news_group.add(news_gl);

            String subcateg = "tech";
            Group news_tech_group = new Group(new StringId(subcateg), new Relevance(1.0));
            news_tech_group.add(defineHitExample("news_tech_1", categ, subcateg, 0.99));
            news_tech_group.add(defineHitExample("news_tech_2", categ, subcateg, 0.89));
            news_tech_group.add(defineHitExample("news_tech_3", categ, subcateg, 0.79));
            news_gl.add(news_tech_group);

            subcateg = "us";
            Group news_us_group = new Group(new StringId(subcateg), new Relevance(1.0));
            news_us_group.add(defineHitExample("news_us_1", categ, subcateg, 0.98));
            news_us_group.add(defineHitExample("news_us_2", categ, subcateg, 0.88));
            news_us_group.add(defineHitExample("news_us_3", categ, subcateg, 0.78));
            news_gl.add(news_us_group);

            subcateg = "business";
            Group news_business_group = new Group(new StringId(subcateg), new Relevance(1.0));
            news_business_group.add(defineHitExample("news_business_1", categ, subcateg, 0.97));
            news_business_group.add(defineHitExample("news_business_2", categ, subcateg, 0.87));
            news_business_group.add(defineHitExample("news_business_3", categ, subcateg, 0.77));
            news_gl.add(news_business_group);

            categ = "finance";
            Group finance_group = new Group(new StringId(categ), new Relevance(1.0));
            root_gl.add(finance_group);
            GroupList finance_gl = new GroupList("subcategory");
            finance_group.add(finance_gl);
            subcateg = "companies";
            Group finance_companies_group = new Group(new StringId(subcateg), new Relevance(1.0));
            finance_companies_group.add(defineHitExample("finance_companies_1", categ, subcateg, 0.96));
            finance_companies_group.add(defineHitExample("finance_companies_2", categ, subcateg, 0.86));
            finance_companies_group.add(defineHitExample("finance_companies_3", categ, subcateg, 0.76));
            finance_gl.add(finance_companies_group);

            result.hits().add(root_group);
            return result;
        }
    }
}