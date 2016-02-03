
package org.coagmento.android.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Errors {

    private List<Object> input = new ArrayList<Object>();
    private List<String> general = new ArrayList<String>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The input
     */
    public List<Object> getInput() {
        return input;
    }

    /**
     * 
     * @param input
     *     The input
     */
    public void setInput(List<Object> input) {
        this.input = input;
    }


    /**
     *
     * @return
     *     The general
     */
    public List<String> getGeneral() {
        return general;
    }

    /**
     *
     * @param general
     *     The general
     */
    public void setGeneral(List<String> general) {
        this.general = general;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
