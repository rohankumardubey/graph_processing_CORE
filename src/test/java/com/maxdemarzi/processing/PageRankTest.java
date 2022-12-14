package com.maxdemarzi.processing;

import org.junit.*;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PageRankTest {
    public static final double EXPECTED_20 = 4.778829041015646;
    public static final double EXPECTED_5 = 2.5952823162078857;
    public static final String LABEL = "Person";
    public static final String REL_TYPE = "KNOWS";

    private static GraphDatabaseService db;
    private static Service service;

    @BeforeClass
    public static void setUp() {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        service = new Service();
        populateDb(db);
    }

    private static void populateDb(GraphDatabaseService db) {
        try ( Transaction tx = db.beginTx()) {
            db.execute(TestObjects.MOVIES_QUERY);
            db.execute(TestObjects.KNOWS_QUERY);
            tx.success();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        db.shutdown();
    }

    @Test
    public void shouldGetRecommendation() throws IOException {
        String response = service.pageRank(LABEL, REL_TYPE, db);
        assertEquals("PageRank for Person and KNOWS Completed!", response);

        dumpResults(EXPECTED_5);

    }
    @Test
    public void shouldGetRecommendationArrayStorageSPI() throws IOException {
        PageRank pageRank = new PageRankArrayStorageSPI(db);
        pageRank.computePageRank(LABEL, REL_TYPE, 20);
        long id = (long) getEntry("Tom Hanks").get("id");
        assertEquals(EXPECTED_20, pageRank.getRankOfNode(id),0.1D);
//        dump(pageRank);
    }
    @Test
    public void shouldGetRecommendationArrayStorageParallelSPI() throws IOException {
        PageRank pageRank = new PageRankArrayStorageParallelSPI(db,Utils.createPool(4,100));
        pageRank.computePageRank(LABEL, REL_TYPE, 20);
        long id = (long) getEntry("Tom Hanks").get("id");
        assertEquals(EXPECTED_20, pageRank.getRankOfNode(id),0.1D);
//        dump(pageRank);
    }
    @Test
    public void shouldGetRecommendationArrayStorage() throws IOException {
        PageRank pageRank = new PageRankArrayStorage(db);
        pageRank.computePageRank(LABEL, REL_TYPE, 20);
        long id = (long) getEntry("Tom Hanks").get("id");
        assertEquals(EXPECTED_20, pageRank.getRankOfNode(id),0.1D);
//        dump(pageRank);
    }
    @Test
    public void shouldGetRecommendationMapStorage() throws IOException {
        PageRank pageRank = new PageRankMapStorage(db);
        pageRank.computePageRank(LABEL, REL_TYPE, 20);
        long id = (long) getEntry("Tom Hanks").get("id");
        assertEquals(EXPECTED_20, pageRank.getRankOfNode(id),0.1D);
//        dump(pageRank);
    }

    private void dump(PageRank pageRank) {
        for (int node = 0;node < pageRank.numberOfNodes();node++) {
            System.out.printf("%d -> %.5f %n", node, pageRank.getRankOfNode(node));
        }
    }

    private void dumpResults(double expected) {
        Map<String, Object> row = getEntry("Tom Hanks");
//        assertEquals( 4.642800717539658, pageranks.next() );
        double rank = (double) row.get("pagerank");
        assertEquals(expected, rank,0.1D);
        System.out.println(row);
    }

    private Map<String, Object> getEntry(String name) {
        try (Result result = db.execute(TestObjects.PERSON_PG_QUERY, Collections.singletonMap("name", name))) {
            return result.next();
        }
    }

}
