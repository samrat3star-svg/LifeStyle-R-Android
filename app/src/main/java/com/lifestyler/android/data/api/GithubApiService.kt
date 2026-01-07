package com.lifestyler.android.data.api

import com.lifestyler.android.data.model.GithubRelease
import retrofit2.Response
import retrofit2.http.GET

interface GithubApiService {
    @GET("repos/samrat3star-svg/LifeStyle-R-Android/releases/latest")
    suspend fun getLatestRelease(): Response<GithubRelease>
}
