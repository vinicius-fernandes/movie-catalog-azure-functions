package com.vinfern.functions;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GetDetails {

    @FunctionName("details")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        String id = request.getQueryParameters().get("id");
        context.getLogger().info("Getting movie for ID " + id);

        if (id == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Please provide an id").build();
        }
        var movie = CosmosDBService.getMovieRequestById(id);


        if (movie.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Movie not found").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(movie.get()).build();
        }
    }
}
