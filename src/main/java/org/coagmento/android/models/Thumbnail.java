
package org.coagmento.android.models;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Thumbnail {

    private String id;
    private String image_small;
    private String image_large;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The imageSmall
     */
    public String getImageSmall() {
        return image_small;
    }

    /**
     * 
     * @param imageSmall
     *     The image_small
     */
    public void setImageSmall(String imageSmall) {
        this.image_small = imageSmall;
    }

    /**
     * 
     * @return
     *     The imageLarge
     */
    public String getImageLarge() {
        return image_large;
    }

    /**
     * 
     * @param imageLarge
     *     The image_large
     */
    public void setImageLarge(String imageLarge) {
        this.image_large = imageLarge;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
