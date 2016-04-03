
package org.coagmento.android.models;

import android.provider.DocumentsContract;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class DocumentTextResponse {

    private String status;
    private Errors errors;
    private DocumentResult result;
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

    public DocumentResult getResult() {
        return result;
    }

    public void setResult(DocumentResult result) {
        this.result = result;
    }

    /**

     * 
     * @param errors
     *     The errors
     */
    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
