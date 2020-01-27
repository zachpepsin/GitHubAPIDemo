package com.zachpepsin.githubapidemo.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://api.github.com/"

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
 * A public interface that exposes the [getRepositories] method
 */
interface RepositoryApiService {
    /**
     * Returns a Suspended [List] of [Repository] which can be fetched if in a Coroutine scope.
     * The @GET annotation indicates that the "repos" endpoint will be requested with the GET
     * HTTP method, using the provided user path.
     * @param user the username to query
     * @param page the page that we are loading
     * @param per_page the number of items we are loading per page
     */
    @GET("users/{user}/repos")
    suspend fun getRepositories(
        @Path("user") user: String,
        @Query("page") page: Int,
        @Query("per_page") per_page: Int
    ):
    // Returns a list of the results
            List<Repository>

    /**
     * Returns a Suspended [RepositoryQuery] which can be fetched if in a Coroutine scope.
     * The @GET annotation indicates that the "repos" endpoint will be requested with the GET
     * HTTP method, using the provided user path
     * @param query the keyword(s) of repositories that we are searching for
     * @param page the page that we are loading
     * @param per_page the number of items we are loading per page
     */
    @GET("search/repositories")
    suspend fun getRepositoriesQuery(
        @Query("q") query : String,
        @Query("page") page: Int,
        @Query("per_page") per_page: Int
    ):
    // Returns a RepositoryQuery object of the results
            RepositoryQuery
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object RepositoryApi {
    val retrofitService: RepositoryApiService by lazy {
        retrofit.create(
            RepositoryApiService::class.java
        )
    }
}