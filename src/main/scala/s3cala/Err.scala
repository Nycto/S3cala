package com.roundeights.s3cala

/** Thrown when an S3 request fails */
class S3Failed (
    message: String, cause: Throwable
) extends Exception( message, cause ) {

    /** Alternate constructor */
    def this ( cause: Throwable ) = this( null, cause )

    /** Alternate constructor */
    def this ( message: String ) = this( message, null )

}

/** Thrown when an S3 resource is not found */
class S3NotFound ( val bucket: String, val key: String ) extends S3Failed (
    "S3 resource not found: %s/%s".format(bucket, key)
)

