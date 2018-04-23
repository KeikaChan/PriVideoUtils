package work.airz

import net.bramp.ffmpeg.FFprobe
import org.bytedeco.javacpp.*
import org.bytedeco.javacpp.opencv_imgcodecs.imwrite
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
//    println(ImageSearch().getVector(ImageIO.read(File("./open.png"))))
    var converterToMat = OpenCVFrameConverter.ToMat()
    var converter = Java2DFrameConverter()
    val input = File("キラキラ.mp4")
    var grabber = OpenCVFrameGrabber.createDefault(input)
    grabber.start()

    print("動画の長さ")
    calcTime(grabber.lengthInFrames, grabber.frameRate + 1)

    val currentDir = System.getProperty("user.dir")
    val ffprobeExec = File(currentDir + File.separator + "encoder", getExtByPlatform("ffprobe"))
    if (!ffprobeExec.exists()) {
        println("encoder does not exist!")
        return
    }
    val ffmprobe = FFprobe(ffprobeExec.absolutePath)
    val format = ffmprobe.probe(input.absolutePath)
    val videoFormat = format.streams[0]


    println("フレームレート ${videoFormat.r_frame_rate.toDouble()}")
//レディーアクション　開始フレーム　4000 5559700559225408535
    while (grabber.frameNumber < grabber.lengthInFrames) {
        val grabbed = grabber.grab() ?: break
        if (grabbed.image == null) continue
        val image = converter.convert(grabbed)
//        if (ImageSearch().isSameImage(ImageSearch().getVector(image), 2, 5559700559225408535L)) {
        /**
         *
         * プリ
         * キラッとボタンを押した時に真っ白になるからそこで時間判定
         *
         * 曲ごとの秒数
         * レディー・アクション            120秒　赤ステージ 開始        5559700559225408535
         * Let'sプリ☆チャン              143秒　町        開始        1122503488345012281
         * キラキラプリ☆チャンワールド     127秒　町
         * ワン・ツー・スウィーツ          119秒　花舞台     開始        3978760074848754912
         */
//        println("同じのあった")
        imwrite("${String.format("%06d", grabber.frameNumber)}_${String.format("%d", ImageSearch().getVector(image))}.png", converterToMat.convert(grabbed))
//        calcTime(grabber.frameNumber, videoFormat.r_frame_rate.toDouble())//フレームレートが多分おかしい
//        println("フレーム数" + grabber.frameNumber)
//        }

    }
}

fun calcTime(frame: Int, frameRate: Double): Double {
    val time = frame / frameRate
    println("時間  ${String.format("%02d", (time / 60).toInt())}:${String.format("%05.2f", time % 60)}")
    return time //なんフレーム目か分かれば求まる
}

fun convert() {
    var converterToMat = OpenCVFrameConverter.ToMat()
    var converter = Java2DFrameConverter()
    var grabber = OpenCVFrameGrabber.createDefault(File("output.mp4"))
    grabber.start()

    var videoMat: opencv_core.Mat
    var count = 0
    while (grabber.frameNumber < grabber.lengthInFrames) {
        val grabbed = grabber.grab() ?: break
        val image = converter.convert(grabbed)
        if (ImageSearch().getVector(image) == -8887692564028994481L) {
            println("同じの会った")
            println(grabbed.timestamp)
            imwrite("${String.format("%06d", count)}_${String.format("%06d", ImageSearch().getVector(image))}.png", converterToMat.convert(grabbed))
            count++
        }

    }
}