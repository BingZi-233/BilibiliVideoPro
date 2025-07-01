package online.bingzi.bilibili.video.pro

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.console
import taboolib.module.lang.sendInfo


object BilibiliVideoPro : Plugin() {

    override fun onLoad() {
        console().sendInfo("load")
        console().sendInfo("loaded")
    }

    override fun onEnable() {
        console().sendInfo("enable")
        console().sendInfo("enabled")
    }

    override fun onActive() {
        console().sendInfo("active")
        console().sendInfo("activated")
    }

    override fun onDisable() {
        console().sendInfo("disable")
        console().sendInfo("disabled")
    }

}