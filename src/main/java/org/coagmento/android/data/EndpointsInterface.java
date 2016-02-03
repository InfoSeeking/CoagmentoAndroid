package org.coagmento.android.data;

import org.coagmento.android.models.NullResponse;
import org.coagmento.android.models.ProjectListResponse;
import org.coagmento.android.models.ProjectResponse;
import org.coagmento.android.models.UserListResponse;
import org.coagmento.android.models.UserResponse;

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

}
