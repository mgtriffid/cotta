package com.mgtriffid.games.panna.screens.game.debug

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.mgtriffid.games.panna.screens.menu.MenuScreen

// TODO make table even prettier, now it's kind ugly. Use NinePatch?
class MetricsDisplay(spriteBatch: SpriteBatch) {
    private val viewport: Viewport = FitViewport(960f, 540f)
    val stage: Stage = Stage(viewport, spriteBatch)
    private val rows = mapOf(
        "buffer_ahead" to Row("buffer_ahead"),
        "sent_chunk_size" to Row("sent_chunk_size"),
        "server_buffer_ahead" to Row("server_buffer_ahead"),
    )

    init {
        val table = Table()
//        table.setFillParent(true)
        addMetricsHeader(table)
        rows.forEach {
            table.add(it.value.name).expand().right()
            table.add(it.value.min).expand().right()
            table.add(it.value.avg).expand().right()
            table.add(it.value.max).expand().right()
            table.add(it.value.stdDev).expand().right()
            table.row()
        }
        table.setPosition(800f, 500f)
        stage.addActor(table)
        stage.isDebugAll = true
    }

    fun updateBufferLength(bufferLength: MetricStats) {
        rows["buffer_ahead"]?.update(bufferLength)
    }

    fun updateSentChunkSize(sentChunkSize: MetricStats) {
        rows["sent_chunk_size"]?.update(sentChunkSize)
    }

    fun updateServerBufferLength(serverBufferLength: MetricStats) {
        rows["server_buffer_ahead"]?.update(serverBufferLength)
    }

    fun dispose() {
        stage.dispose()
    }

    private class Row(
        name: String
    ) {
        val name = Label(name, MenuScreen.Styles.formInputLabelStyle)
        val min = Label("", MenuScreen.Styles.formInputLabelStyle)
        val avg = Label("", MenuScreen.Styles.formInputLabelStyle)
        val max = Label("", MenuScreen.Styles.formInputLabelStyle)
        val stdDev = Label("", MenuScreen.Styles.formInputLabelStyle)

        fun update(stats: MetricStats) {
            min.setText(String.format("%.2f", stats.min))
            avg.setText(String.format("%.2f", stats.avg))
            max.setText(String.format("%.2f", stats.max))
            stdDev.setText(String.format("%.2f", stats.stdDev))
        }
    }
}

fun addMetricsHeader(table: Table) {
    table.add(Label("Metric", MenuScreen.Styles.formInputLabelStyle)).expand()
        .right()
    table.add(Label("Min", MenuScreen.Styles.formInputLabelStyle)).expand()
        .right()
    table.add(Label("Avg", MenuScreen.Styles.formInputLabelStyle)).expand()
        .right()
    table.add(Label("Max", MenuScreen.Styles.formInputLabelStyle)).expand()
        .right()
    table.add(Label("StdDev", MenuScreen.Styles.formInputLabelStyle)).expand()
        .right()
    table.row()
}
