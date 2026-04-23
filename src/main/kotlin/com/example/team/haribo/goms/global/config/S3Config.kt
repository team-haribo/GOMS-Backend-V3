package com.example.team.haribo.goms.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class S3Config {

    @Bean
    fun s3Client(s3Properties: S3Properties): S3Client {
        val credentials = AwsBasicCredentials.create(
            s3Properties.credentials.accessKey,
            s3Properties.credentials.secretKey
        )

        return S3Client.builder()
            .region(Region.of(s3Properties.region.static))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}