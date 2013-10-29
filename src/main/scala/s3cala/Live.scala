package com.roundeights.s3cala

import com.amazonaws.services.s3.transfer.{TransferManager, Transfer}
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.event.{ProgressListener, ProgressEvent}
import com.amazonaws.AmazonServiceException

import java.io.{InputStream, File, FileInputStream}

import scala.concurrent.{Promise, Future, ExecutionContext}


/**
 * S3 Client wrapper
 */
class LiveS3 (
    private val client: TransferManager,
    private val context: ExecutionContext
) extends S3 {

    /** {@inheritDoc} */
    override def bucket ( bucket: String )
        = new LiveBucket( client, bucket, context )

    /** {@inheritDoc} */
    override def close: Unit = client.shutdownNow
}


/** An asynchronous S3 progress listener, per the AWS api */
private[s3cala] class Listener (
    private val result: Promise[Unit],
    private val transfer: Transfer
) extends ProgressListener {

    /** {@inheritDoc} */
    override def progressChanged ( event: ProgressEvent ): Unit = {
        if ( !result.isCompleted ) {
            event.getEventCode match {
                case ProgressEvent.COMPLETED_EVENT_CODE => result.success( () )
                case ProgressEvent.CANCELED_EVENT_CODE
                    => result.failure( new S3Failed("Request Cancelled") )
                case ProgressEvent.FAILED_EVENT_CODE
                    => result.failure( transfer.waitForException )
                case _ => ()
            }
        }
    }
}


/**
 * A specific S3 bucket
 */
class LiveBucket (
    private val client: TransferManager,
    private val bucket: String,
    implicit private val context: ExecutionContext
) extends Bucket {

    /** Executes a transfer */
    private def transfer ( key: String, body: => Transfer ): Future[Unit] = {
        val result = Promise[Unit]

        try {
            val transfer = body
            transfer.addProgressListener( new Listener(result, transfer) )
        } catch {
            case err: AmazonServiceException if err.getStatusCode == 404
                => result.failure( new S3NotFound(bucket, key) )
            case err: Throwable => result.failure( new S3Failed(err) )
        }

        result.future
    }

    /** {@inheritDoc} */
    override def get ( key: String, file: File ): Future[Unit]
        = transfer( key, client.download( bucket, key, file ) )

    /** {@inheritDoc} */
    override def get ( key: String ): Future[InputStream] = {
        val file = File.createTempFile("S3-",".tmp")
        get( key, file ).map( _ => new FileInputStream(file) )
    }

    /** {@inheritDoc} */
    override def put ( key: String, file: File ): Future[Unit]
        = transfer( key, client.upload( bucket, key, file ) )

    /** {@inheritDoc} */
    override def put ( key: String, stream: InputStream ): Future[Unit] = {
        transfer( key, client.upload(
            bucket, key, stream, new ObjectMetadata
        ) )
    }

}


