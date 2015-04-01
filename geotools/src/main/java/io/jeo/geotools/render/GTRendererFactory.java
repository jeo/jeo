package io.jeo.geotools.render;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.jeo.map.View;
import io.jeo.render.RendererFactory;
import io.jeo.util.Key;

public class GTRendererFactory implements RendererFactory<GTRenderer> {

    /**
     * Key specifying directly a buffered image
     */
    public static Key<BufferedImage> IMAGE = new Key<BufferedImage>("image", BufferedImage.class);

    /**
     * Key specifying buffered image type.
     * <p>
     *  See {@link BufferedImage} class for values.
     * </p>
     *
     * {@see BufferedImage}
     */
    public static Key<Integer> IMAGE_TYPE = 
        new Key<Integer>("image-type", Integer.class, BufferedImage.TYPE_4BYTE_ABGR); 

    /**
     * Format of final image to encode.
     */
    public static Key<String> IMAGE_FORMAT = new Key<String>("image-format", String.class, "png");

    @Override
    public String getName() {
        return "GeoTools";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("gt");
    }

    @Override
    public List<String> getFormats() {
        return Arrays.asList("png", "image/png", "jpeg", "image/jpeg");
    }

    @Override
    public GTRenderer create(View view, Map<?, Object> opts) {
        // first look directly for an image
        BufferedImage img = IMAGE.get(opts);
        if (img == null) {
            // create one from dimensions
            int type = IMAGE_TYPE.get(opts);
            img = new BufferedImage(view.getWidth(), view.getHeight(), type);
        }

        return new GTRenderer(img);
    }
}
