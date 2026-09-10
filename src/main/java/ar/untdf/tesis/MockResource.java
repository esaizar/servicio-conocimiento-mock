package ar.untdf.tesis;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MockResource {

    @Inject
    FixtureStore store;

    @Inject
    ObjectMapper mapper;

    @GET
    @Path("health")
    public Response health() {
        return Response.ok().entity(new HealthResponse("ok")).build();
    }

    @GET
    @Path("metadata")
    public Response metadata() {
        return Response.ok(store.metadata()).build();
    }

    @POST
    @Path("consultar_normativa")
    public Response consultarNormativa(ConsultaRequest payload) {
        String validation = validatePayload(payload);
        if (validation != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonError(validation)).build();
        }

        ObjectNode request = mapper.valueToTree(payload);
        ArrayNode result = store.findNormativa(request);
        return Response.ok(result).build();
    }

    @POST
    @Path("candidatos_de_tramite")
    public Response candidatosDeTramite(ConsultaRequest payload) {
        String validation = validatePayload(payload);
        if (validation != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonError(validation)).build();
        }

        ObjectNode request = mapper.valueToTree(payload);
        ArrayNode result = store.findTramites(request);
        return Response.ok(result).build();
    }

    @GET
    @Path("normativa_tramite/{codTramite}")
    public Response normativaTramite(@PathParam("codTramite") Integer codTramite,
                                     @QueryParam("vigente_a") String vigenteA) {
        if (codTramite == null || codTramite <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new JsonError("El parametro 'codTramite' debe ser un entero positivo"))
                    .build();
        }

        ArrayNode result = store.findNormativaPorTramite(codTramite, vigenteA);
        return Response.ok(result).build();
    }

    private static String validatePayload(ConsultaRequest payload) {
        if (payload == null) {
            return "Body JSON requerido";
        }

        String text = payload.texto() == null ? "" : payload.texto();
        if (text.isBlank()) {
            return "Falta el campo obligatorio 'texto'";
        }

        return null;
    }

    public record JsonError(String error) {
    }

    public record HealthResponse(String status) {
    }

    public record ConsultaRequest(String texto, Integer k, FiltrosRequest filtros) {
    }

    public record FiltrosRequest(String vigente_a, String tributo) {
    }
}
