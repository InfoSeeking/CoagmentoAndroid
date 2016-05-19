package com.coagmento.mobile.data;

import com.coagmento.mobile.models.BookmarksListResponse;
import com.coagmento.mobile.models.ChatListResponse;
import com.coagmento.mobile.models.CreateChatMessageResponse;
import com.coagmento.mobile.models.DeleteBookmarkResponse;
import com.coagmento.mobile.models.DocumentTextResponse;
import com.coagmento.mobile.models.ListResponse;
import com.coagmento.mobile.models.NullResponse;
import com.coagmento.mobile.models.CreatePageResponse;
import com.coagmento.mobile.models.PageResponse;
import com.coagmento.mobile.models.PagesListResponse;
import com.coagmento.mobile.models.ProjectListResponse;
import com.coagmento.mobile.models.ProjectResponse;
import com.coagmento.mobile.models.QueryListResponse;
import com.coagmento.mobile.models.SnippetListResponse;
import com.coagmento.mobile.models.SnippetResponse;
import com.coagmento.mobile.models.UserListResponse;
import com.coagmento.mobile.models.UserResponse;

import retrofit.Call;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by shahh on 1/2/2016.
 */
public interface EndpointsInterface {

    // USER

    // User-Create
    @FormUrlEncoded
    @POST("/api/v1/users")
    Call<UserResponse> createUser(
            @Field("email") String email,
            @Field("password") String password,
            @Field("name") String name);

    // User- Get Current
    @GET("/api/v1/users/current")
    Call<UserResponse> authenticateUser(@Query("auth_email") String email, @Query("auth_password") String password);

    // User - Get
    @GET("/api/v1/users")
    Call<UserResponse> getUser(@Query("email") String email);

    // User - Get
    @GET("/api/v1/users/{id}")
    Call<UserResponse> getUser(@Path("id") int userId);

    // User - Get Multiple
    @GET("/api/v1/users")
    Call<UserListResponse> getUserList(@Query("project_id") int project_id);

    @GET("/api/v1/users")
    Call<UserListResponse> getUserList();

    // PROJECTS

    // Project-Create
    @FormUrlEncoded
    @POST("/api/v1/projects")
    Call<ProjectResponse> createProject(@Field("title") String title, @Field("private") int project_privacy, @Query("auth_email") String email, @Query("auth_password") String password);
    @FormUrlEncoded
    @POST("/api/v1/projects")
    Call<ProjectResponse> createProject(@Field("title") String title, @Field("description") String description, @Field("private") int project_privacy, @Query("auth_email") String email, @Query("auth_password") String password);

    // Delete Projects
    @DELETE("/api/v1/projects/")
    Call<ProjectResponse> deleteMultipleProject(@Query("ids[0]") int id1, @Query("ids[1]") int id2, @Query("auth_email") String email, @Query("auth_password") String password);
    @DELETE("/api/v1/projects/")
    Call<ProjectResponse> deleteMultipleProject(@Query("ids[0]") int id1, @Query("ids[1]") int id2, @Query("ids[2]") int id3, @Query("auth_email") String email, @Query("auth_password") String password);
    @DELETE("/api/v1/projects/")
    Call<ProjectResponse> deleteMultipleProject(@Query("ids[0]") int id1, @Query("ids[1]") int id2, @Query("ids[2]") int id3, @Query("ids[3]") int id4, @Query("auth_email") String email, @Query("auth_password") String password);
    @DELETE("/api/v1/projects/")
    Call<ProjectResponse> deleteMultipleProject(@Query("ids[0]") int id1, @Query("ids[1]") int id2, @Query("ids[2]") int id3, @Query("ids[3]") int id4, @Query("ids[4]") int id5, @Query("auth_email") String email, @Query("auth_password") String password);

    // Delete Single Project
    @DELETE("/api/v1/projects/{id}")
    Call<ProjectResponse> deleteProject(@Path("id") int id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Get Multiple Projects
    @GET("/api/v1/projects")
    Call<ProjectListResponse> getProjectList(@Query("auth_email") String email, @Query("auth_password") String password);

    // Get Single Project
    @GET("/api/v1/projects/{id}")
    Call<ProjectResponse> getProject(@Path("id") int id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Get Tags for a Project
    //TODO: Figure out how tags work

    // Share Project with Users
    @FormUrlEncoded
    @POST("/api/v1/projects/{id}/share")
    Call<NullResponse> shareProject(@Path("id") int project_id, @Field("user_email") String user_email, @Field("permission") String permission, @Query("auth_email") String email, @Query("auth_password") String password);

    // Project Update
    @PUT("/api/v1/projects/{id}")
    Call<NullResponse> updateProjectTitle(@Path("id") int id, @Query("title") String newTitle, @Query("auth_email") String email, @Query("auth_password") String password);

    /*
     * BOOKMARKS
     */

    // Bookmarks - Get Multiple
    @GET("/api/v1/bookmarks")
    Call<BookmarksListResponse> getBookmarks(@Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    //Bookmarks - Delete
    @DELETE("/api/v1/bookmarks/{id}")
    Call<DeleteBookmarkResponse> deleteBookmark(@Path("id") int bookmark_id, @Query("auth_email") String email, @Query("auth_password") String password);

    //Bookmarks - Move to Project
    @PUT("/api/v1/bookmarks/{id}/move")
    Call<NullResponse> moveProject(@Path("id") int bookmark_id, @Query("project_id")int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Bookmarks - Create
    @POST("/api/v1/bookmarks")
    Call<NullResponse> createBookmark(@Query("project_id") int project_id, @Query("url") String url, @Query("notes") String notes,
                                      @Query("title") String title, @Query("auth_email") String email, @Query("auth_password") String password);

    // Bookmarks - Update
    @PUT("/api/v1/bookmarks/{id}")
    Call<NullResponse> updateBookmarksURL(@Path("id") int bookmark_id, @Query("url") String url, @Query("auth_email") String email, @Query("auth_password") String password);


    @PUT("/api/v1/bookmarks/{id}")
    Call<NullResponse> updateBookmarksTITLE(@Path("id") int bookmark_id, @Query("title") String title, @Query("auth_email") String email, @Query("auth_password") String password);


    /*
     *  PAGES
     */

    // Pages - Create
    @POST("/api/v1/pages")
    Call<CreatePageResponse> createPage(@Query("project_id") int project_id, @Query("url") String url, @Query("auth_email") String email, @Query("auth_password") String password);
    Call<CreatePageResponse> createPage(@Query("project_id") int project_id, @Query("url") String url, @Query("title") String title, @Query("auth_email") String email, @Query("auth_password") String password);

    // Pages - Delete
    @DELETE("/api/v1/pages/{id}")
    Call<NullResponse> deletePage(@Path("id") int page_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Pages - Get Multipled
    @GET("/api/v1/pages")
    Call<PagesListResponse> getMultiplePages(@Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Pages Get
    @GET("/api/v1/pages/{id}")
    Call<PageResponse> getPage(@Path("id") int page_id, @Query("auth_email") String email, @Query("auth_password") String password);



    /*
     *  SNIPPET
     */
    // Snippet - Create
    @POST("/api/v1/snippets")
    Call<SnippetResponse> createSnippet(@Query("project_id") int project_id, @Query("url") String url, @Query("text") String text, @Query("auth_email") String email, @Query("auth_password") String password);

    @POST("/api/v1/snippets")
    Call<SnippetResponse> createSnippet(@Query("project_id") int project_id, @Query("url") String url, @Query("text") String text, @Query("title") String title, @Query("auth_email") String email, @Query("auth_password") String password);

    // Snippet - Delete
    @DELETE("/api/v1/snippets/{id}")
    Call<NullResponse> deleteSnippet(@Path("id") int snippet_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Snippet - Get
    @GET("/api/v1/snippets/{id}")
    Call<SnippetResponse> getSnippet(@Path("id") int snippet_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Snippet - Get Multiple
    @GET("/api/v1/snippets")
    Call<SnippetListResponse> getMultipleSnippets(@Query("auth_email") String email, @Query("auth_password") String password);

    // Snippet - Update
    @PUT("/api/v1/snippets/{id}")
    Call<SnippetResponse> updateSnippetURL(@Path("id") int snippet_id, @Query("url") String url, @Query("auth_email") String email, @Query("auth_password") String password);


    @PUT("/api/v1/snippets/{id}")
    Call<SnippetResponse> updateSnippetTEXT(@Path("id") int snippet_id, @Query("text") String text, @Query("auth_email") String email, @Query("auth_password") String password);


    /*
    *   CHAT
     */

    @GET("/api/v1/chatMessages")
    Call<ChatListResponse> getChatMessages(@Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    @POST("/api/v1/chatMessages")
    Call<CreateChatMessageResponse> sendMessage(@Query("message") String message, @Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);


    /*
    *
    *  Queries
    *
     */

    // Queries - Create
    @POST("/api/v1/queries")
    Call<NullResponse> createQuery(@Query("project_id") int project_id, @Query("text") String text, @Query("search_engine") String search_engine, @Query("auth_email") String email, @Query("auth_password") String password);

    // Query - Delete
    @DELETE("/api/v1/queries/{id}")
    Call<NullResponse> deleteQuery(@Path("id") int query_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Query - Get
    @GET("/api/v1/queries")
    Call<QueryListResponse> getQueries(@Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    /*
    *
    *   DOCUMENTs
    *
     */

    // Documents - Get Multiple
    @GET("/api/v1/docs")
    Call<ListResponse> getDocuments(@Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Documents - Get Text
    @GET("/api/v1/docs/{id}/text")
    Call<DocumentTextResponse> getDocumentText(@Path("id") int doc_id, @Query("project_id") int project_id, @Query("auth_email") String email, @Query("auth_password") String password);

    // Documents - Create
    @DELETE("/api/v1/docs/{id}")
    Call<NullResponse> deleteDocument(@Path("id") int doc_id, @Query("auth_email") String email, @Query("auth_password") String password);
}


