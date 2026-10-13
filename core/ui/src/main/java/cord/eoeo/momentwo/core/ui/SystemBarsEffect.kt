package cord.eoeo.momentwo.core.ui

import androidx.compose.runtime.*
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** 활성 화면의 상태를 적용한다. ON_PAUSE에서 복원해 다음 화면의 설정을 뒤늦게 덮어쓰지 않는다. */
@Composable
fun SystemBarsEffect(controller: WindowInsetsControllerCompat, visible: Boolean) {
    val owner = LocalLifecycleOwner.current
    val currentVisible by rememberUpdatedState(visible)
    fun apply(show: Boolean) {
        if (show) controller.show(WindowInsetsCompat.Type.systemBars()) else controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = if (show) WindowInsetsControllerCompat.BEHAVIOR_DEFAULT else WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    DisposableEffect(controller, owner) {
        var applied = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { apply(currentVisible); applied = true }
                Lifecycle.Event.ON_PAUSE -> if (applied) { apply(true); applied = false }
                Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY, Lifecycle.Event.ON_ANY -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) { apply(currentVisible); applied = true }
        onDispose { owner.lifecycle.removeObserver(observer); if (applied) apply(true) }
    }
    SideEffect { if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) apply(visible) }
}
