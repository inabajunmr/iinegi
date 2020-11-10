package work.inabajun.iinegi.web;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
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
    public Response listNegi(@QueryParam("after") Long after) {
        if(after == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            return Response.ok(service.listAfter(after)).build();
        }
    }

    @GET
    @Path("{negiId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNegi(@PathParam("negiId") String negiId) {
        return service.find(negiId).map(n -> Response.ok(n).build()).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postNegi(@MultipartForm NegiForm form) {
        try {
            return Response.ok(service.create(form.image, form.description)).build();
        } catch (NotImageException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/iinegi/{negiId}")
    public String iinegi(@PathParam("negiId") String negiId) {
        return "hello";
    }

}
