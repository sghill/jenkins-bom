package internal

import retrofit2.Call
import retrofit2.http.GET

interface JenkinsService {
    @GET("stable/update-center.actual.json")
    fun ltsUpdateCenter(): Call<UpdateCenter>
}
