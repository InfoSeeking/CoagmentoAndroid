
package org.coagmento.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
@SuppressWarnings("serial")
public class Result implements Serializable {

    private User user;
    private Integer id;
    private String name;
    private String email;
    private Integer project_id;
    private Integer user_id;
    private String level;
    private String created_at;
    private String updated_at;
    private String title;
    private String description;
    private Integer creator_id;
    private Object deleted_at;
    private String url;
    private String notes;

    public String getSearch_engine() {
        return search_engine;
    }

    public void setSearch_engine(String search_engine) {
        this.search_engine = search_engine;
    }

    private String message;
    private String search_engine;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;
    private Thumbnail thumbnail;
    @SerializedName("is_query") private String isQuery;

    public String getIsQuery() {
        return isQuery;
    }

    public void setIsQuery(String isQuery) {
        this.isQuery = isQuery;
    }

    private Page page;

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    @SerializedName("private") private Integer _private;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return
     *     The user
     */
    public User getUser() {
        return user;
    }

    /**
     * 
     * @param user
     *     The user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     *
     * @return
     *     The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     *     The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     *     The project_id
     */
    public Integer getProjectId() {
        return project_id;
    }

    /**
     *
     * @param projectId
     *     The project_id
     */
    public void setProjectId(Integer project_id) {
        this.project_id = project_id;
    }

    /**
     *
     * @return
     *     The user_id
     */
    public Integer getuser_id() {
        return user_id;
    }

    /**
     *
     * @param user_id
     *     The user_id
     */
    public void setuser_id(Integer user_id) {
        this.user_id = user_id;
    }

    /**
     *
     * @return
     *     The level
     */
    public String getLevel() {
        return level;
    }

    /**
     *
     * @param level
     *     The level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     *
     * @return
     *     The created_at
     */
    public String getcreated_at() {
        return created_at;
    }

    /**
     *
     * @param created_at
     *     The created_at
     */
    public void setcreated_at(String created_at) {
        this.created_at = created_at;
    }

    /**
     *
     * @return
     *     The updated_at
     */
    public String getupdated_at() {
        return updated_at;
    }

    /**
     *
     * @param updated_at
     *     The updated_at
     */
    public void setupdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    /**
     *
     * @return
     *     The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     *     The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     *     The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     *     The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     *     The creator_id
     */
    public Integer getcreator_id() {
        return creator_id;
    }

    /**
     *
     * @param creator_id
     *     The creator_id
     */
    public void setcreator_id(Integer creator_id) {
        this.creator_id = creator_id;
    }

    /**
     *
     * @return
     *     The deleted_at
     */
    public Object getdeleted_at() {
        return deleted_at;
    }

    /**
     *
     * @param deleted_at
     *     The deleted_at
     */
    public void setdeleted_at(Object deleted_at) {
        this.deleted_at = deleted_at;
    }

    /**
     *
     * @return
     *     The _private
     */
    public Integer getPrivate() {
        return _private;
    }

    /**
     *
     * @param _private
     *     The private
     */
    public void setPrivate(Integer _private) {
        this._private = _private;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
