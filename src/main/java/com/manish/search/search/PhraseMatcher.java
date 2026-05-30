package com.manish.search.search;

import com.manish.search.indexing.Posting;

public class PhraseMatcher {
    public boolean matches(Posting first, Posting second) {
        for(int p1 : first.getPositions()) {
            for (int p2 : second.getPositions()) {
                if(p2 - p1 == 1) return true;
            }
        }

        return false;
    }
}
