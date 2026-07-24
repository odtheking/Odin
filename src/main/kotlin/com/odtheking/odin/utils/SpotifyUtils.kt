package com.odtheking.odin.utils

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference

object SpotifyUtils {
    private val isWindows = System.getProperty("os.name").orEmpty().startsWith("Windows", true)
    private val idleTitles = setOf("spotify", "spotify premium", "spotify free", "advertisement")
    private val helperTitles = setOf("default ime", "msctfime ui")

    val nowPlaying: String?
        get() {
            if (!isWindows) return null
            val title = runCatching { spotifyWindowTitle() }.getOrNull()?.trim().orEmpty()
            return title.takeIf { it.isNotEmpty() && it.lowercase() !in idleTitles }
        }

    private fun spotifyWindowTitle(): String? {
        var found: String? = null

        User32.INSTANCE.EnumWindows(object : WinUser.WNDENUMPROC {
            override fun callback(hWnd: WinDef.HWND, data: Pointer?): Boolean {
                if (!User32.INSTANCE.IsWindowVisible(hWnd)) return true

                val buffer = CharArray(512)
                User32.INSTANCE.GetWindowText(hWnd, buffer, buffer.size)
                val text = Native.toString(buffer).trim()
                if (text.isEmpty() || text.lowercase() in helperTitles || text.startsWith("GDI+ Window", true)) return true

                val executable = IntByReference().let { pid ->
                    User32.INSTANCE.GetWindowThreadProcessId(hWnd, pid)
                    ProcessHandle.of(pid.value.toLong()).flatMap { it.info().command() }.orElse("")
                }
                if (!executable.endsWith("Spotify.exe", true)) return true

                found = text
                return false
            }
        }, null)

        return found
    }
}
