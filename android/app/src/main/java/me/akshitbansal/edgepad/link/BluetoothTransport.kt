package me.akshitbansal.edgepad.link

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import me.akshitbansal.edgepad.link.net.Sas
import me.akshitbansal.edgepad.protocol.ProtocolConstants
import java.io.IOException

// Test build: no hold at all, so every motion batch goes straight to the socket. The 8 ms pace held a batch before
// writing, and that measured out at half the pointer rate over Bluetooth: 69/s against 129/s over Wi-Fi. A raw probe
// showed the link carries about 8,000 five-byte frames per second, so the pace, not the link, was the limit.
private const val BLUETOOTH_PACE_MS = 0

/**
 * An RFCOMM socket to a bonded laptop. Bluetooth pairing already encrypts the link, so nothing runs in front of
 * the frames and [open] never shows a code. A missing Nearby devices permission comes out as a SecurityException.
 */
class BluetoothTransport(
    private val device: BluetoothDevice,
) : LinkTransport {
    @Volatile private var socket: BluetoothSocket? = null

    @Volatile private var closed = false

    override val writePaceMs: Int get() = BLUETOOTH_PACE_MS

    override fun open(
        showCode: (Sas) -> Unit,
        confirmed: () -> Boolean,
    ): Streams {
        val s = device.createRfcommSocketToServiceRecord(ProtocolConstants.SERVICE_ID)
        socket = s
        // A close() that ran before the line above found no socket to close.
        if (closed) {
            s.close()
            throw IOException("Closed before connecting")
        }
        s.connect()
        return Streams(s.inputStream, s.outputStream)
    }

    override fun close() {
        closed = true
        try {
            socket?.close()
        } catch (e: IOException) {
            // Already broken; closing is all that was asked for.
        }
    }
}
