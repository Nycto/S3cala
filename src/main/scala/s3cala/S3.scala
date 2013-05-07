package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import scala.concurrent.{Future, ExecutionContext}
import java.io.{InputStream, File, ByteArrayInputStream}

/**
 * S3 Companion
 */
object S3 {

    /** Builds a new instance */
    def apply
        ( client: TransferManager )
        ( implicit context: ExecutionContext )
    : S3 = new LiveS3( client, context )

    /** Builds an instance from a set of AWS credentials */
    def apply
        ( credentials: AWSCredentials )
        ( implicit context: ExecutionContext )
    : S3 = apply( new TransferManager(credentials) )

    /** Builds an instance from an Access key and a Secret Key */
    def apply
        ( accessKey: String, secretKey: String )
        ( implicit context: ExecutionContext )
    : S3 = apply( new BasicAWSCredentials( accessKey, secretKey ) )

    /** Generates a local S3 interface using the given directory */
    def local ( root: File )( implicit context: ExecutionContext )
        = new LocalS3( root, context )

    /** Generates a temporary local S3 interface */
    def temp ( implicit context: ExecutionContext ) = new LocalS3( context )
}

/**
 * S3 Client wrapper
 */
trait S3 {

    /** Builds a bucket specific client */
    def bucket ( bucket: String ): Bucket

    /** Closes this connection */
    def close: Unit
}

/**
 * A specific S3 bucket
 */
trait Bucket {

    /** Downloads a key into a file */
    def get ( key: String, file: File ): Future[Unit]

    /** Downloads a key */
    def get ( key: String ): Future[InputStream]

    /** Uploads a file into a key */
    def put ( key: String, file: File ): Future[Unit]

    /** Uploads an input stream into a key */
    def put ( key: String, stream: InputStream ): Future[Unit]

    /** Uploads a Byte Array into a key */
    def put ( key: String, bytes: Array[Byte] ): Future[Unit]
        = put( key, new ByteArrayInputStream( bytes ) )

    /** Uploads a String into a key */
    def put ( key: String, str: String ): Future[Unit]
        = put( key, str.getBytes("UTF8") )
}


