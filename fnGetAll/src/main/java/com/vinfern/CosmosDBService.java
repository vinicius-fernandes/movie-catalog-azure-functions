package com.vinfern;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CosmosDBService {

    private static CosmosClient cosmosClient;
    private static CosmosContainer cosmosContainer;

    static {
        String connectionKey = System.getenv("CosmosDbKey");
        String endPoint = System.getenv("CosmosDbEndPoint");
        String databaseName = System.getenv("DatabaseName");
        String containerName = System.getenv("ContainerName");

        cosmosClient = new CosmosClientBuilder()
                .endpoint(endPoint)
                .key(connectionKey)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        cosmosClient.createDatabaseIfNotExists(databaseName);
        CosmosDatabase database = cosmosClient.getDatabase(databaseName);

        try {
            cosmosContainer = database.getContainer(containerName);
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, "/id");
                database.createContainer(containerProperties, ThroughputProperties.createManualThroughput(400));
                System.out.println("Container created: " + containerName);
            } else {
                throw e;
            }
        }
        cosmosContainer = cosmosClient.getDatabase(databaseName).getContainer(containerName);
    }


    public static List<MovieRequest> getAllMovies() {
        List<MovieRequest> movieRequests = new ArrayList<>();

        String query = "SELECT * FROM c";

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setConsistencyLevel(ConsistencyLevel.EVENTUAL);

        CosmosPagedIterable<MovieRequest> iterable = cosmosContainer.queryItems(query, options, MovieRequest.class);

        for (MovieRequest movieRequest : iterable) {
            movieRequests.add(movieRequest);
        }

        return movieRequests;
    }


}

