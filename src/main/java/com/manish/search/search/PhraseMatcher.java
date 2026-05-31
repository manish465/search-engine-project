package com.manish.search.search;

import com.manish.search.indexing.Posting;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PhraseMatcher {
    public boolean matches(List<Posting> postings) {
        Posting first = postings.getFirst();

        for(int startPosition : first.getPositions()) {
            boolean matched = true;

            for(int i=1; i < postings.size(); i++) {
                int expectedPosition = startPosition + i;

                if(!postings.get(i).getPositions().contains(expectedPosition)) {
                    matched = false;
                    break;
                }
            }

            if(matched) return true;
        }

        return false;
    }
}
