package com.vinfern.functions;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;

import java.util.Optional;
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


    public static Optional<MovieRequest> getMovieRequestById(String id) {
        try {
            CosmosItemResponse<MovieRequest> response = cosmosContainer.readItem(id, new PartitionKey(id), MovieRequest.class);

            return Optional.ofNullable(response.getItem());
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                System.err.println("MovieRequest with ID " + id + " not found.");
            } else {
                System.err.println("Error fetching MovieRequest from Cosmos DB: " + e.getMessage());
            }
            return Optional.empty();
        }
    }


}

