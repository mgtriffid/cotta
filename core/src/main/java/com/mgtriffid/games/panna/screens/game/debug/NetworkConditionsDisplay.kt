package com.mgtriffid.games.panna.screens.game.debug

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.panna.screens.menu.MenuScreen
import com.mgtriffid.games.panna.shared.game.ConfigurableIssues
import com.mgtriffid.games.panna.shared.game.ConfigurableNetworkConditions

class NetworkConditionsDisplay(spriteBatch: SpriteBatch) {
    private val viewport: Viewport = FitViewport(960f, 540f)
    val stage: Stage = Stage(viewport, spriteBatch)
    private val sendingConditionsData = ConditionsData()
    private val receivingConditionsData = ConditionsData()

    init {
        val sendingTable = Table()
        sendingTable.add(Label("Sending", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        sendingTable.row()
        sendingTable.add(Label("Min Latency", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        sendingTable.add(sendingConditionsData.minLatency).expand().right()
        sendingTable.row()
        sendingTable.add(Label("Max Latency", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        sendingTable.add(sendingConditionsData.maxLatency).expand().right()
        sendingTable.row()
        sendingTable.add(Label("Packet Loss", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        sendingTable.add(sendingConditionsData.packetLoss).expand().right()
        sendingTable.row()
        sendingTable.setPosition(800f, 300f)
        val receivingTable = Table()
        receivingTable.add(Label("Receiving", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        receivingTable.row()
        receivingTable.add(Label("Min Latency", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        receivingTable.add(receivingConditionsData.minLatency).expand().right()
        receivingTable.row()
        receivingTable.add(Label("Max Latency", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        receivingTable.add(receivingConditionsData.maxLatency).expand().right()
        receivingTable.row()
        receivingTable.add(Label("Packet Loss", MenuScreen.Styles.formInputLabelStyle)).expand().right()
        receivingTable.add(receivingConditionsData.packetLoss).expand().right()
        receivingTable.row()
        receivingTable.setPosition(800f, 150f)
        stage.addActor(sendingTable)
        stage.addActor(receivingTable)
        stage.isDebugAll = true
    }

    fun updateNetworkConditions(
        networkConditions: ConfigurableNetworkConditions
    ) {
        updateConditions(networkConditions.sending, sendingConditionsData)
        updateConditions(networkConditions.receiving, receivingConditionsData)
    }

    private fun updateConditions(
        sending: ConfigurableIssues,
        conditionsLabels: ConditionsData
    ) {
        conditionsLabels.minLatency.setText(sending.latency.min.toString())
        conditionsLabels.maxLatency.setText(sending.latency.max.toString())
        conditionsLabels.packetLoss.setText(sending.packetLoss.toString())
    }

    private class ConditionsData {
        var minLatency = Label("", MenuScreen.Styles.formInputLabelStyle)
        var maxLatency = Label("", MenuScreen.Styles.formInputLabelStyle)
        var packetLoss = Label("", MenuScreen.Styles.formInputLabelStyle)
    }

    fun dispose() {
        stage.dispose()
    }
}
