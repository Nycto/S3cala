package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}

/**
 * S3 Companion
 */
object S3 {

    /** Builds a new instance */
    def apply( client: TransferManager ): S3 = new S3( client )

    /** Builds an instance from a set of AWS credentials */
    def apply( credentials: AWSCredentials ): S3
        = apply( new TransferManager(credentials) )

    /** Builds an instance from an Access key and a Secret Key */
    def apply( accessKey: String, secretKey: String ): S3
        = apply( new BasicAWSCredentials( accessKey, secretKey ) )

}

/**
 * S3 Client wrapper
 */
class S3 ( private val client: TransferManager ) {

    /** Builds a bucket specific client */
    def bucket ( bucket: String ) = new Bucket( client, bucket )

    /** Closes this connection */
    def close: Unit = client.shutdownNow

}


