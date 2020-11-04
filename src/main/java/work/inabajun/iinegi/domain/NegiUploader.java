package work.inabajun.iinegi.domain;

import java.io.InputStream;

public class NegiUploader {

    public Negi upload(InputStream image, String description) {
        // TODO upload to S3
        final Negi negi = new Negi(description, "TODO S3 path");
        // TODO persist
        return negi;
    }
}
