package work.inabajun.iinegi.web;

import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ejb.PostActivate;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@Path("/negi")
public class NegiResource {

    private static final Logger LOG = Logger.getLogger(NegiResource.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listNegi(@QueryParam("after") String after) {
        return "hello";
    }

    @GET
    @Path("{negiId}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getNegi(@PathParam("negiId") String negiId) {
        return "hello";
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response postNegi(@MultipartForm NegiForm form) throws IOException {
        LOG.info(form.description);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        form.image.transferTo(baos);

        Response.ResponseBuilder response = Response.ok((StreamingOutput) output -> baos.writeTo(output));
        response.header("Content-Disposition", "attachment;filename=fuck.png");
        response.header("Content-Type", "image/png");
        return response.build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/iinegi/{negiId}")
    public String iinegi(@PathParam("negiId") String negiId) {
        return "hello";
    }

}
