package com.lifestyler.android.data.repository

import com.lifestyler.android.data.api.GithubApiService
import com.lifestyler.android.domain.entity.UpdateInfo
import com.lifestyler.android.domain.repository.UpdateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    private val githubApiService: GithubApiService
) : UpdateRepository {

    override suspend fun getLatestUpdate(): Result<UpdateInfo?> {
        return try {
            val response = githubApiService.getLatestRelease()
            if (response.isSuccessful) {
                val release = response.body()
                if (release != null) {
                    // Find the APK asset
                    val apkAsset = release.assets.find { it.name.endsWith(".apk") }
                    if (apkAsset != null) {
                        Result.success(
                            UpdateInfo(
                                version = release.tagName,
                                releaseNotes = release.body,
                                downloadUrl = apkAsset.downloadUrl
                            )
                        )
                    } else {
                        Result.success(null)
                    }
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception("Failed to fetch latest release: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
