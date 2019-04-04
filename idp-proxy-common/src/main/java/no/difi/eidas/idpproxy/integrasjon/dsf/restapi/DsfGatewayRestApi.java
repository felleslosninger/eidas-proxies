package no.difi.eidas.idpproxy.integrasjon.dsf.restapi;

import no.difi.dsfgateway.DSFResponseResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/dsf")
public interface DsfGatewayRestApi {
    @Produces({"application/xml", "application/json"})
    @GET
    @Path("/{ssn}")
    DSFResponseResource bySsn(@PathParam("ssn") String ssn);

    @Produces({"application/xml", "application/json"})
    @GET
    @Path("/{firstName}/{lastName}/{birth}")
    DSFResponseResource byNameAndBirth(
            @PathParam("firstName") String firstName,
            @PathParam("lastName") String lastName,
            @PathParam("birth") String birth
    );
}
