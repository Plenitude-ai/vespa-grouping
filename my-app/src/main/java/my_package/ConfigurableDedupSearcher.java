package my_package;

import java.util.HashSet;

import com.yahoo.component.annotation.Inject;
import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.grouping.GroupingRequest;
import com.yahoo.search.grouping.request.AllOperation;
import com.yahoo.search.grouping.request.AttributeValue;
import com.yahoo.search.grouping.request.EachOperation;
import com.yahoo.search.grouping.request.SummaryValue;
import com.yahoo.search.grouping.result.Group;
import com.yahoo.search.result.Hit;
import com.yahoo.search.result.HitGroup;
import com.yahoo.search.searchchain.Execution;

public class ConfigurableDedupSearcher extends Searcher {

    private final boolean use_dedup;
    private final int max_categories;
    private final int max_sub_categories;

    @Inject
    public ConfigurableDedupSearcher(DedupSearcherConfig config) {
        this.use_dedup = config.useDedup();
        this.max_categories = config.maxCategories();
        this.max_sub_categories = config.maxSubCategories();
    }

    // public void addMetaConfigHit(Result result) {
    // Hit meta_config = new Hit("config");
    // meta_config.setField("dedup", this.use_dedup);
    // meta_config.setField("max_categories", this.max_categories);
    // meta_config.setField("max_sub_categories", this.max_sub_categories);
    // meta_config.setMeta(true);
    // meta_config.setRelevance(10);
    // result.hits().add(meta_config);
    // }

    @Override
    public Result search(Query query, Execution execution) {
        if (this.use_dedup == true) {
            GroupingRequest request = GroupingRequest.newInstance(query);

            request.setRootOperation(new AllOperation()
                    .setGroupBy(new AttributeValue("category"))
                    .addChild(new EachOperation()
                            .setMax(this.max_categories)
                            .setGroupBy(new AttributeValue("subcategory"))
                            .addChild(new EachOperation()
                                    .setMax(this.max_sub_categories)
                                    .addChild(new EachOperation()
                                            .addOutput(new SummaryValue())))));
            Result result = execution.search(query); // Pass on to the next searcher to get results

            // Extract result group
            Group result_group = request.getResultGroup(result);

            // Flatten and diversify the result set
            HitGroup flattenedHits = new HitGroup("flattened_hits");
            // flattenedHits.setRelevance(2);
            HashSet<String> seen_ids = new HashSet<>();
            buildFinalResult(result_group, flattenedHits, seen_ids);
            result.setHits(flattenedHits);
            // result.setHits()
            // // result.hits().freeze();
            // result.hits().add(flattenedHits);

            // if (this.return_grouped_results == false) {
            // result.hits().remove("group:root:0");
            // }

            // addMetaConfigHit(result);
            // result.hits().freeze();
            return result;
        } else {
            Result result = execution.search(query);
            // addMetaConfigHit(result);
            return result;
        }
    }

    private void buildFinalResult(HitGroup hit_group, HitGroup finalResult, HashSet<String> seen_ids) {
        for (Hit hit : hit_group) {
            if (hit instanceof HitGroup hitGroup) {
                // Recursively process nested groups
                buildFinalResult(hitGroup, finalResult, seen_ids);
            } else {
                String hitId = hit.getId().toString();
                // When a given doc has several subcategories,
                // grouping duplicates it in its different subcategories groups
                if (!seen_ids.contains(hitId)) {
                    seen_ids.add(hitId);
                    finalResult.add(hit);
                }
            }
        }
    }
}
