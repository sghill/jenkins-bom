package alt

import retrofit2.Call
import retrofit2.http.*

interface BintrayService {
    @HEAD("/packages/{subject}/{repo}/{pkg}/versions/{version}")
    fun checkVersionExists(
            @Path("subject") subject: String,
            @Path("repo") repo: String,
            @Path("pkg") pkg: String,
            @Path("version") version: String
    ): Call<Void>

    @Headers("Content-Type: application/json")
    @POST("/packages/{subject}/{repo}/{pkg}/versions")
    fun createVersion(
            @Path("subject") subject: String,
            @Path("repo") repo: String,
            @Path("pkg") pkg: String,
            @Body createVersionRequest: CreateVersionRequest
    ): Call<Void>

    @POST("/content/{subject}/{repo}/{pkg}/versions/{version}/publish")
    fun publishVersion(
            @Path("subject") subject: String,
            @Path("repo") repo: String,
            @Path("pkg") pkg: String,
            @Path("version") version: String,
            @Body body: PublishRequest
    ): Call<Void>
}
