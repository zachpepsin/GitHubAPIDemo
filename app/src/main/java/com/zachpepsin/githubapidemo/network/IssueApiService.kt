package com.zachpepsin.githubapidemo.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://api.github.com/repos/"

enum class IssueApiState(val value: String) {
    SHOW_ALL("all"), SHOW_OPEN("open"), SHOW_CLOSED("closed");

    companion object {
        fun toArray(): Array<String> {
            return arrayOf("All", "Open", "Closed")
        }
    }
}

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

/**
 * A public interface that exposes the [getIssues] method
 */
interface IssueApiService {
    /**
     * Returns a Suspended [List] of [Issue] which can be fetched if in a Coroutine scope.
     * The @GET annotation indicates that the "issues" endpoint will be requested with the GET
     * HTTP method, using the provided user and repoName path
     */
    @GET("{user}/{repoName}/issues")
    suspend fun getIssues(
        @Path("user") user: String,
        @Path("repoName") repoName: String,
        @Query("state") state: String,
        @Query("page") page: Int,
        @Query("per_page") per_page: Int
    ):
    // Returns a list of the results
            List<Issue>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object IssueApi {
    val retrofitService: IssueApiService by lazy {
        retrofit.create(
            IssueApiService::class.java
        )
    }
}
