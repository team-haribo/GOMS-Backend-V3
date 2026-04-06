package com.example.team.haribo.goms.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloud.aws")
data class S3Properties(
    val credentials: Credentials,
    val region: Region,
    val s3: S3
) {
    data class Credentials(
        val accessKey: String,
        val secretKey: String
    )

    data class Region(
        val static: String
    )

    data class S3(
        val bucket: String
    )
}