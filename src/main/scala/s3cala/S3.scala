package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.model.{ProgressListener, ProgressEvent}
import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}

import java.io.{InputStream, File, FileInputStream}

import scala.concurrent.{Promise, Future, ExecutionContext}

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
    def bucket ( bucket: String ) = new Bucket( bucket )

    /** Closes this connection */
    def close: Unit = client.shutdownNow

    /**
     * A specific S3 bucket
     */
    class Bucket ( private val bucket: String ) {

        /** Downloads a key into a file */
        def get ( key: String, file: File ): Future[Unit] = {
            val result = Promise[Unit]

            val upload = client.download( bucket, key, file )

            upload.addProgressListener(new ProgressListener {
                override def progressChanged ( event: ProgressEvent ): Unit = {
                    event.getEventCode match {
                        case ProgressEvent.FAILED_EVENT_CODE
                            => result.failure( upload.waitForException )
                        case ProgressEvent.CANCELED_EVENT_CODE
                            => result.failure( new Exception )
                        case ProgressEvent.COMPLETED_EVENT_CODE
                            => result.success( Unit )
                        case _ => ()
                    }
                }
            })

            result.future
        }

        /** Downloads a key */
        def get
            ( key: String )
            ( implicit context: ExecutionContext )
        : Future[InputStream] = {
            val file = File.createTempFile("S3-",".tmp")
            get( key, file ).map( _ => new FileInputStream(file) )
        }

    }

}


