package work.inabajun.iinegi.web;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import work.inabajun.iinegi.domain.NegiNotFoundException;
import work.inabajun.iinegi.domain.NotImageException;
import work.inabajun.iinegi.domain.NegiService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/negi")
public class NegiResource {

    private static final Logger LOG = Logger.getLogger(NegiResource.class.getName());

    @Inject
    NegiService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listNegi(@QueryParam("before") Long after) {
        LOG.info("Call listNegi. before=" + after);
        if(after == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            return Response.ok(service.listBefore(after))
                    .header("Access-Control-Allow-Origin", "*") // TODO for test
                    .build();
        }
    }

    @GET
    @Path("{negiId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNegi(@PathParam("negiId") String negiId) {
        return service.find(negiId).map(n -> Response.ok(n).build()).orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                .header("Access-Control-Allow-Origin", "*") // TODO for test
                .build());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postNegi(@MultipartForm NegiForm form) {
        try {
            return Response.ok(service.create(form.image, form.description))
                    .header("Access-Control-Allow-Origin", "*") // TODO for test
                    .build();
        } catch (NotImageException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{negiId}/iinegi")
    public Response iinegi(@PathParam("negiId") String negiId) {
        try{
            service.iinegi(negiId);
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*") // TODO for test
                    .build();
        }catch (NegiNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
