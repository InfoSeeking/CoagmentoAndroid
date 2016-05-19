
package com.coagmento.mobile.models;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class SnippetResponse {

    private String status;
    private Errors errors;
    private Result result;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The errors
     */
    public Errors getErrors() {
        return errors;
    }

    /**
     * 
     * @param errors
     *     The errors
     */
    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    /**
     * 
     * @return
     *     The result
     */
    public Result getResult() {
        return result;
    }

    /**
     * 
     * @param result
     *     The result
     */
    public void setResult(Result result) {
        this.result = result;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
