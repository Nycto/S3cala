package com.roundeights.s3cala

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}

import java.io.InputStream

/**
 * S3 Companion
 */
object S3 {

    /** Builds a new instance */
    def apply( client: AmazonS3Client ): S3 = new S3( client )

    /** Builds an instance from a set of AWS credentials */
    def apply( credentials: AWSCredentials ): S3
        = apply( new AmazonS3Client(credentials) )

    /** Builds an instance from an Access key and a Secret Key */
    def apply( accessKey: String, secretKey: String ): S3
        = apply( new BasicAWSCredentials( accessKey, secretKey ) )

}

/**
 * S3 Client wrapper
 */
class S3 ( private val client: AmazonS3Client ) {

    /**
     * A specific S3 bucket
     */
    class Bucket ( private val bucket: String )

    /** Builds a bucket specific client */
    def bucket ( bucket: String ) = new Bucket( bucket )

}


