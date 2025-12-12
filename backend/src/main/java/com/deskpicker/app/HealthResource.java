package com.deskpicker.app;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/")
public class HealthResource {

  @GET
  @Path("/healthz")
  @Produces(MediaType.APPLICATION_JSON)
  public Response health() {
    return Response.ok(Map.of("status", "ok", "service", "desk-picker-backend")).build();
  }

  @GET
  @Path("/readyz")
  @Produces(MediaType.APPLICATION_JSON)
  public Response readiness() {
    return Response.ok(Map.of("status", "ready")).build();
  }
}
