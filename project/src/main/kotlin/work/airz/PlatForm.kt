package work.airz

enum class PlatForm {
    LINUX,
    WINDOWS,
    MACOS,
    UNKNOWN
}

fun getPlatform(): Enum<PlatForm> {
    val osName = System.getProperty("os.name").toLowerCase()
    return when {
        osName.startsWith("linux") -> PlatForm.LINUX
        osName.startsWith("windows") -> PlatForm.WINDOWS
        osName.startsWith("mac") -> PlatForm.MACOS
        else -> PlatForm.UNKNOWN
    }
}


fun getExtByPlatform(prefix: String): String {
    return when {
        getPlatform() == PlatForm.WINDOWS -> "$prefix.exe"
        else -> prefix
    }
}
