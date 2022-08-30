package org.gephi.plugins.neo4j.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.testcontainers.containers.Neo4jContainer;

import java.util.Scanner;

public class Neo4jIntegrationTest {

    @ClassRule
    public static Neo4jContainer<?> neo4j = new Neo4jContainer("neo4j:4.4");

    @BeforeClass
    public static void before() {
        Neo4jIntegrationTest.neo4j.start();
    }

    @AfterClass
    public static void after() {
        Neo4jIntegrationTest.neo4j.stop();
    }

    /**
     * Retrieve the Neo4j driver instance for the running container.
     */
    public static Driver getNeo4jDriver() {
        return GraphDatabase.driver(Neo4jIntegrationTest.neo4j.getBoltUrl(), AuthTokens.basic("neo4j", Neo4jIntegrationTest.neo4j.getAdminPassword()));
    }

    /**
     * Load the given cypher script file in the database.
     */
    public static void loadFile(String file) {
        // init db if needed
        if (file != null) {
            String query = "";
            try (Scanner s = new Scanner(Neo4jIntegrationTest.class.getResourceAsStream(file)).useDelimiter("\\n")) {
                while (s.hasNext()) {
                    query += s.next() + "\n";
                }
            }
            Driver driver = Neo4jIntegrationTest.getNeo4jDriver();
            try (Session session = driver.session()) {
                session.run(query);
            }
        }
    }
}
