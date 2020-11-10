package work.inabajun.iinegi.web;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

public class NegiForm {

    @FormParam("image")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream image;

    @FormParam("description")
    @PartType(MediaType.TEXT_PLAIN)
    public String description;

}
