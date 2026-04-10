package com.smartkrishi.voice

object VoiceHandler {

    var processor: VoiceCommandProcessor? = null

    fun handle(command: String) {
        processor?.process(command)
    }
}