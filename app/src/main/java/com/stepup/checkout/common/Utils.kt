package com.stepup.checkout.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.OffsetMapping
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder

/** A [CoroutineScope] with a [SupervisorJob] */
@Suppress("FunctionName")
fun SupervisorScope(parent: CoroutineScope) = parent + SupervisorJob(parent.coroutineContext[Job])

/**
 * A [CoroutineScope] that will emit exceptions that occur in scope to [onError].
 * Uses [SupervisorJob] so that the scope isn't cancelled on exception.
 */
@Suppress("FunctionName")
fun ErrorHandlerScope(
    scope: CoroutineScope,
    onError: suspend (Throwable) -> Unit
) = SupervisorScope(scope) + CoroutineExceptionHandler { _, error ->
    scope.launch { onError(error) }
}

/**
 * Same as [launch] but doesn't return the created [Job].
 * Useful for more concise code
 */
fun CoroutineScope.perform(action: suspend () -> Unit) {
    launch { action() }
}

/**
 * Wraps a [BUFFERED][Channel.BUFFERED] [Channel].
 * Sends and collects its values using Dispatchers.Main.immediate.
 * This helps avoid the edge case of [Channel] losing events when the collector disappears after
 * the value is sent.
 */
class EventsContainer<T> : Flow<T>, FlowCollector<T> {

    private val channel = Channel<T>(Channel.BUFFERED)

    override suspend fun emit(value: T) = withContext(Dispatchers.Main.immediate) {
        channel.send(value)
    }

    override suspend fun collect(collector: FlowCollector<T>) {
        withContext(Dispatchers.Main.immediate) {
            channel.receiveAsFlow().collect(collector)
        }
    }
}

suspend inline fun withLoading(
    crossinline update: suspend (isLoading: Boolean) -> Unit,
    block: () -> Unit
) {
    update(true)
    try {
        block()
    } finally {
        update(false)
    }
}

@Composable
inline fun OnLifecycleEvent(event: Lifecycle.Event, vararg keys: Any?, crossinline action: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, *keys) {
        val observer = LifecycleEventObserver { _, e -> if (e == event) action() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

/**
 * Collects the receiver [Flow] using the given [collector].
 * Cancels and restarts collection if receiver changes.
 * Cancels collection if the current Lifecycle falls below the [minActiveState],
 * restarts it if it's in that state again.
 */
@Composable
fun <T> Collect(
    flow: Flow<T>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit = {}
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(flow, collector, lifecycle) {
        flow.flowWithLifecycle(lifecycle, minActiveState)
            .collect { launch { collector(it) } }
    }
}

operator fun OffsetMapping.plus(other: OffsetMapping): OffsetMapping =
    CombinedOffsetMapping(this, other)

private data class CombinedOffsetMapping(
    val first: OffsetMapping,
    val second: OffsetMapping
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        return first.originalToTransformed(offset).let(second::originalToTransformed)
    }

    override fun transformedToOriginal(offset: Int): Int {
        return first.transformedToOriginal(offset).let(second::transformedToOriginal)
    }
}

fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8)
fun String.urlDecode(): String = URLDecoder.decode(this, Charsets.UTF_8)
