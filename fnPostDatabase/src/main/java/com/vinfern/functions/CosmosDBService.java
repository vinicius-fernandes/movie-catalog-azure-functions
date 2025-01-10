package com.vinfern.functions;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;

import java.util.UUID;

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

    public static MovieRequest saveMovieRequest(MovieRequest movieRequest) {
        try {
            movieRequest.setId(UUID.randomUUID().toString());
            cosmosContainer.createItem(movieRequest);
            System.out.println("Movie Request saved with ID: " + movieRequest.getId());
            return movieRequest;
        } catch (CosmosException e) {
            System.err.println("Error saving movie request to Cosmos DB: " + e.getMessage());
            return null;
        }
    }


}

