package com.sandhirizki0088.snaptrip.network


import com.sandhirizki0088.snaptrip.model.Travel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://6844276971eb5d1be03289ca.mockapi.io/api/v1/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface TravelApiService {
    @GET("travel")
    suspend fun getTravel(@Query("userId") userId: String): List<Travel>

    @GET("travel")
    suspend fun getTravelAll(): List<Travel>

    @FormUrlEncoded
    @POST("travel")
    suspend fun postTravel(
        @Field("tempat") tempat: String,
        @Field("tanggal") tanggal: String,
        @Field("imageId") imageId: String,
        @Field("userId") userId: String
    ): Travel

    @DELETE("travel/{id}")
    suspend fun deleteTravel(
        @Path("id") id: String)

    @FormUrlEncoded
    @PUT("travel/{id}")
    suspend fun updateTravel(
        @Path("id") id: String,
        @Field("tempat") tempat: String,
        @Field("tanggal") tanggal: String,
        @Field("imageId") imageId: String,
        @Field("userId") userId: String
    ): Travel


}


object TravelApi {
    val service: TravelApiService = retrofit.create(TravelApiService::class.java)

}

enum class ApiStatus { LOADING, SUCCESS, FAILED }