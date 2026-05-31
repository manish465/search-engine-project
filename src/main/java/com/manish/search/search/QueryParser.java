package com.manish.search.search;

import com.manish.search.model.WeightedQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QueryParser {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("([+-]?)\"([^\"]+)\"|([+-]?\\S+)");

    public WeightedQuery parse(String query) {
        List<String> positiveTerms = new ArrayList<>();
        List<String> negativeTerms = new ArrayList<>();

        List<String> positivePhrases = new ArrayList<>();
        List<String> negativePhrases = new ArrayList<>();

        Matcher matcher = TOKEN_PATTERN.matcher(query);

        while (matcher.find()) {
            if(matcher.group(2) != null) {
                String sign = matcher.group(1);
                String phrase = matcher.group(2);

                if("-".equals(sign)) negativePhrases.add(phrase);
                else positivePhrases.add(phrase);
            } else {
                String token = matcher.group(3);

                if(token.startsWith("-")) negativeTerms.add(token.substring(1));
                else if(token.startsWith("+")) positiveTerms.add(token.substring(1));
                else positiveTerms.add(token);
            }
        }

        return new WeightedQuery(
                positiveTerms,
                negativeTerms,
                positivePhrases,
                negativePhrases
        );
    }
}
