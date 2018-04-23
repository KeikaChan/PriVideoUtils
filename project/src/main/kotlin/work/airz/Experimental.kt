package work.airz

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.bytedeco.javacpp.*
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.io.File

fun faceDetect() {
    var converterToMat = OpenCVFrameConverter.ToMat()

    val face_cascade = opencv_objdetect.CascadeClassifier("lbpcascade_animeface.xml")


    var grabber = OpenCVFrameGrabber.createDefault(File("output.mp4"))
    grabber.start()

    var videoMat: opencv_core.Mat
    var count = 0

    while (grabber.frameNumber < grabber.lengthInFrames) {
        var grabbed = grabber.grab() ?: break
        videoMat = converterToMat.convert(grabbed)
        var videoMatGray = opencv_core.Mat()
        opencv_imgproc.cvtColor(videoMat, videoMatGray, opencv_imgproc.COLOR_BGRA2GRAY)
        opencv_imgproc.equalizeHist(videoMatGray, videoMatGray)
        var faces = opencv_core.RectVector()

        face_cascade.detectMultiScale(videoMatGray, faces, 1.1, 5, 0, opencv_core.Size(100, 100), opencv_core.Size(1000, 1000))
        for (i in 0 until faces.size().toInt()) {
            val face_i = faces[i.toLong()]
            opencv_imgproc.rectangle(videoMat, face_i, opencv_core.Scalar(0.0, 255.0, 0.0, 4.0), 10, 1, 0)
        }
        if (faces.size() > 0) {
            opencv_highgui.imshow("face_recognizer", videoMat)
            opencv_imgcodecs.imwrite("out-$count.png", videoMat)
            count++
            println("face detected!")
        }
    }
}

/**
 * 若干の犠牲は出るものの出来る限りキレイに出来てる
 */
fun videoRotate(){
    val currentDir = System.getProperty("user.dir")
    val ffmpegExec = File(currentDir + File.separator + "encoder", getExtByPlatform("ffmpeg"))
    val ffprobeExec = File(currentDir + File.separator + "encoder", getExtByPlatform("ffprobe"))
    if (!ffmpegExec.exists() || !ffprobeExec.exists()) {
        println("encoder does not exist!")
    }


    val ffmpeg = FFmpeg(ffmpegExec.absolutePath)
    val ffmprobe = FFprobe(ffprobeExec.absolutePath)
    val fFmpegProbeResult = ffmprobe.probe(File("encoder", "output.mp4").absolutePath)

    /**
     * formatについて
     * streams[0]がビデオ,streams[1]がオーディオ
     */
    val format = ffmprobe.probe(File("encoder", "output.mp4").absolutePath)
    val videoFormat = format.streams[0]
    val audioFormat = format.streams[1]

//    //now encoding
    val ffmpegBuilder = FFmpegBuilder()
            .setInput(File("encoder", "output.mp4").absolutePath)
            .overrideOutputFiles(true)
            .addOutput(File("encoder", "output_neko.mp4").absolutePath)
            .setAudioChannels(audioFormat.channels)
            .setAudioCodec(audioFormat.codec_name)        // using the aac codec
            .setAudioSampleRate(audioFormat.sample_rate)  // at 48KHz
            .setAudioBitRate(audioFormat.bit_rate)      // at 32 kbit/s

            .setVideoCodec("libx264")     // Video using x264
            .setVideoFrameRate(videoFormat.r_frame_rate.numerator, videoFormat.r_frame_rate.denominator)     // at 24 frames per second
            .setVideoBitRate(videoFormat.bit_rate)
            .setVideoResolution(1080, 1920) // at 640x480 resolution
            .setFormat("mp4")
            .addExtraArgs("-vf", "transpose=2")
//            .setVideoQuality(16.0)
            .done()

    val executor = FFmpegExecutor(ffmpeg, ffmprobe)
    val job = executor.createTwoPassJob(ffmpegBuilder)

    job.run()
}