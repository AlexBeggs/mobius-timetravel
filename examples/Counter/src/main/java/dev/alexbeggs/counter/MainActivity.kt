package dev.alexbeggs.counter

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.spotify.mobius.*
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.extras.Connectables.contramap
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx3.RxMobius
import dev.alexbeggs.mobius.timetravel.TimeTravelController
import dev.alexbeggs.mobius.timetravel.TimeTravelServer
import dev.alexbeggs.mobius.timetravel.TimeTravelUpdate
import io.reactivex.rxjava3.core.ObservableTransformer

// Steps:
// 1) Add mobius-timetravel-core dependency
// 2) Add mobius-timetravel-server-<server-type> e.g. mobius-timetravel-server-http dependency
// 3) Compose Update<M,E,F> with TimeTravelUpdate<M,E,F>
// 4) Compose MobiusLoop.Controller<M,E,F> with TimeTravelController<M,E,F>
// 5) Connect TimeTravelController and TimeTravelUpdate
// 6) Add timeTravelServer.start() to Application/Activity#onCreate
// 7) Profit
class MainActivity : Connectable<NumberView, NumberEvent>, AppCompatActivity() {

    private lateinit var controller: MobiusLoop.Controller<NumberModel, NumberEvent>

    private lateinit var numberView: TextView
    private lateinit var increaseButton: Button
    private lateinit var decreaseButton: Button

    private val viewModel: NumberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        numberView = findViewById(R.id.number_view)
        increaseButton = findViewById(R.id.increase_button)
        decreaseButton = findViewById(R.id.decrease_button)
        val effectHandlers: ObservableTransformer<NumberEffect, NumberEvent> =
            RxMobius.subtypeEffectHandler<NumberEffect, NumberEvent>().build()
        controller = createController(effectHandlers, viewModel.state, NumberUpdate())
        controller.connect(contramap({ value ->
            // retain the state between configuration changes (however, not the whole timeline)
            viewModel.state = value
            modelToViewModelMapper(value)
        }, this))
    }

    override fun onResume() {
        super.onResume()
        controller.start()
    }

    override fun onPause() {
        controller.stop()
        super.onPause()
    }


    override fun onDestroy() {
        controller.disconnect()
        super.onDestroy()
    }

    override fun connect(output: Consumer<NumberEvent>): Connection<NumberView> {
        increaseButton.setOnClickListener { output.accept(IncreaseNumber) }
        decreaseButton.setOnClickListener { output.accept(DecreaseNumber) }
        return object : Connection<NumberView> {
            override fun dispose() {
            }

            override fun accept(model: NumberView) {
                render(model)
            }
        }
    }

    fun render(model: NumberView) {
        this.numberView.text = model.number.toString()
    }


    companion object {
        @JvmStatic
        fun modelToViewModelMapper(model: NumberModel): NumberView {
            return NumberView(model.number)
        }
    }

}

class NumberUpdate : Update<NumberModel, NumberEvent, NumberEffect> {
    override fun update(model: NumberModel, event: NumberEvent): Next<NumberModel, NumberEffect> {
        return Next.next(when (event) {
            is IncreaseNumber ->
                if (model.number != 0 && model.number % 3 == 0) {
                    model.copy(number = model.number + 20)
                } else {
                    model.copy(number = model.number + 1)
                }
            is DecreaseNumber -> model.copy(number = model.number - 1)
        })
    }
}

fun createController(
    effectHandlers: ObservableTransformer<NumberEffect, NumberEvent>, defaultModel: NumberModel,
    update: Update<NumberModel, NumberEvent, NumberEffect>,
): MobiusLoop.Controller<NumberModel, NumberEvent> {
    val timeTravelUpdate: TimeTravelUpdate<NumberModel, NumberEvent, NumberEffect> =
        TimeTravelUpdate.from(update)
    val timeTravelController =
        TimeTravelController(
            MobiusAndroid.controller(createLoop(effectHandlers, timeTravelUpdate), defaultModel),
            timeTravelUpdate,
            TimeTravelServer.timeTravelServer
        )
    TimeTravelController.connect(timeTravelController, timeTravelUpdate)
    return timeTravelController
}

private fun createLoop(
    effectHandlers: ObservableTransformer<NumberEffect, NumberEvent>,
    update: Update<NumberModel, NumberEvent, NumberEffect>,
): MobiusLoop.Factory<NumberModel, NumberEvent, NumberEffect> {
    return RxMobius.loop(update, effectHandlers)
        .logger(AndroidLogger.tag("Number View"))
}

data class NumberModel(val number: Int)

sealed class NumberEffect

sealed class NumberEvent
object IncreaseNumber : NumberEvent()
object DecreaseNumber : NumberEvent()

data class NumberView(val number: Int)

internal class NumberViewModel : ViewModel() {
    var state: NumberModel = NumberModel(0)
}