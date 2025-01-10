package com.vinfern.functions;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;


public class SaveMovie {

    @FunctionName("movie")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<MovieRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("Saving movie request to Cosmos DB");

        MovieRequest movieRequest = request.getBody().orElse(null);

        if (movieRequest == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid movie request data").build();
        }

        try {

            MovieRequest savedMovieRequest = CosmosDBService.saveMovieRequest(movieRequest);

            if (savedMovieRequest != null) {
                return request.createResponseBuilder(HttpStatus.OK)
                        .body(savedMovieRequest)
                        .build();
            } else {
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to save the movie request")
                        .build();
            }
        } catch (Exception e) {
            context.getLogger().severe("Error saving movie request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving movie request: " + e.getMessage())
                    .build();
        }
    }
}
