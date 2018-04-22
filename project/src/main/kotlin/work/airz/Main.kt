package work.airz

import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.bytedeco.javacpp.opencv_core.*
import java.io.File


fun main(args: Array<String>) {
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
            .setAudioCodec("aac")        // using the aac codec
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


fun getExtByPlatform(prefix: String): String {
    return when {
        getPlatform() == PlatForm.WINDOWS -> "$prefix.exe"
        else -> prefix
    }
}

fun rotate(src: IplImage, angle: Int): IplImage {
    val img = IplImage.create(src.height(), src.width(), src.depth(), src.nChannels())
    cvTranspose(src, img)
    cvFlip(img, img, angle)
    return img
}