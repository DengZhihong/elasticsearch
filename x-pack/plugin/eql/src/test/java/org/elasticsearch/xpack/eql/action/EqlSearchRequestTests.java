/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */
package org.elasticsearch.xpack.eql.action;

import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.fetch.subphase.FieldAndFormat;
import org.elasticsearch.xpack.eql.AbstractBWCSerializationTestCase;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.elasticsearch.index.query.AbstractQueryBuilder.parseInnerQueryBuilder;

public class EqlSearchRequestTests extends AbstractBWCSerializationTestCase<EqlSearchRequest> {

    // TODO: possibly add mutations
    static String defaultTestFilter = "{\n" +
        "   \"match\" : {\n" +
        "       \"foo\": \"bar\"\n" +
        "   }" +
        "}";

    static String defaultTestIndex = "endgame-*";

    @Before
    public void setup() {
    }

    @Override
    protected NamedWriteableRegistry getNamedWriteableRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, Collections.emptyList());
        return new NamedWriteableRegistry(searchModule.getNamedWriteables());
    }

    @Override
    protected NamedXContentRegistry xContentRegistry() {
        SearchModule searchModule = new SearchModule(Settings.EMPTY, Collections.emptyList());
        return new NamedXContentRegistry(searchModule.getNamedXContents());
    }

    @Override
    protected EqlSearchRequest createTestInstance() {
        try {
            List<FieldAndFormat> randomFetchFields = new ArrayList<>();
            int fetchFieldsCount = randomIntBetween(0, 5);
            for (int j = 0; j < fetchFieldsCount; j++) {
                randomFetchFields.add(new FieldAndFormat(randomAlphaOfLength(10), randomAlphaOfLength(10)));
            }
            if (randomFetchFields.isEmpty()) {
                randomFetchFields = null;
            }
            QueryBuilder filter = parseFilter(defaultTestFilter);
            EqlSearchRequest request = new EqlSearchRequest()
                .indices(new String[]{defaultTestIndex})
                .filter(filter)
                .timestampField(randomAlphaOfLength(10))
                .eventCategoryField(randomAlphaOfLength(10))
                .fetchSize(randomIntBetween(1, 50))
                .size(randomInt(50))
                .query(randomAlphaOfLength(10))
                .fetchFields(randomFetchFields);

            return request;
        } catch (IOException ex) {
            assertNotNull("unexpected IOException " + ex.getCause().getMessage(), ex);
        }
        return null;
    }

    protected QueryBuilder parseFilter(String filter) throws IOException {
        XContentParser parser = createParser(JsonXContent.jsonXContent, filter);
        return parseFilter(parser);
    }

    protected QueryBuilder parseFilter(XContentParser parser) throws IOException {
        QueryBuilder parseInnerQueryBuilder = parseInnerQueryBuilder(parser);
        assertNull(parser.nextToken());
        return parseInnerQueryBuilder;
    }

    @Override
    protected Writeable.Reader<EqlSearchRequest> instanceReader() {
        return EqlSearchRequest::new;
    }

    @Override
    protected EqlSearchRequest doParseInstance(XContentParser parser) {
        return EqlSearchRequest.fromXContent(parser).indices(new String[]{defaultTestIndex});
    }
}
