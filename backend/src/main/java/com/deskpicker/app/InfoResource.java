package com.deskpicker.app;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.time.OffsetDateTime;
import java.util.Map;

@Path("/api/info")
@ApplicationScoped
public class InfoResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getInfo() {
    return Map.of(
        "name", "Desk Picker",
        "status", "up",
        "timestamp", OffsetDateTime.now().toString());
  }
}
