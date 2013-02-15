package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.model.{ProgressListener, ProgressEvent}
import com.amazonaws.AmazonServiceException

import java.io.{InputStream, File, FileInputStream}

import scala.concurrent.{Promise, Future, ExecutionContext}

/**
 * A specific S3 bucket
 */
class Bucket (
    private val client: TransferManager,
    val bucket: String
) {

    /** Downloads a key into a file */
    def get ( key: String, file: File ): Future[Unit] = {
        val result = Promise[Unit]

        try {
            val upload = client.download( bucket, key, file )

            upload.addProgressListener(new ProgressListener {
                override def progressChanged ( event: ProgressEvent ): Unit = {
                    event.getEventCode match {
                        case ProgressEvent.FAILED_EVENT_CODE
                            => result.failure(
                                new S3Failed(upload.waitForException)
                            )
                        case ProgressEvent.CANCELED_EVENT_CODE
                            => result.failure(
                                new S3Failed("Request Cancelled")
                            )
                        case ProgressEvent.COMPLETED_EVENT_CODE
                            => result.success( Unit )
                        case _
                            => ()
                    }
                }
            })
        } catch {
            case err: AmazonServiceException if err.getStatusCode == 404
                => result.failure( new S3NotFound(bucket, key) )
            case err: Throwable => result.failure( new S3Failed(err) )
        }

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


