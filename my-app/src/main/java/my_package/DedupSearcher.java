package my_package;

import java.util.HashSet;

import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.grouping.GroupingRequest;
import com.yahoo.search.grouping.request.AllOperation;
import com.yahoo.search.grouping.request.AttributeValue;
import com.yahoo.search.grouping.request.EachOperation;
import com.yahoo.search.grouping.request.SummaryValue;
import com.yahoo.search.result.Hit;
import com.yahoo.search.result.HitGroup;
import com.yahoo.search.grouping.result.Group;
import com.yahoo.search.searchchain.Execution;


public class DedupSearcher extends Searcher {

    @Override
    public Result search(Query query, Execution execution) {

        GroupingRequest request = GroupingRequest.newInstance(query);
        // BlendingSearcher blender = new BlendingSearcher("blender");

        request.setRootOperation(new AllOperation()
                .setGroupBy(new AttributeValue("category"))
                .addChild(new EachOperation()
                .setMax(2)
                .setGroupBy(new AttributeValue("subcategory"))
                .addChild(new EachOperation()
                .setMax(2)
                .addChild(new EachOperation()
                .addOutput(new SummaryValue())))));
        Result result = execution.search(query); // Pass on to the next searcher to get results
        // Result result = blender.search(query, execution);
        
        // Flatten and diversify the result set
        // HitGroup root = result.hits(); // <- this is wrong
        Group group = request.getResultGroup(result);
        HitGroup flattenedHits = new HitGroup("flattened_hits");
        HashSet<String> seen_ids = new HashSet<>();
        buildFinalResult(group, flattenedHits, seen_ids);

        // Replace original hits with the flattened, diversified list
        result.setHits(flattenedHits);

        return result;
    }

    private void buildFinalResult(HitGroup hits, HitGroup finalResult, HashSet<String> seen_ids) {
        for (Hit hit : hits) {
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
