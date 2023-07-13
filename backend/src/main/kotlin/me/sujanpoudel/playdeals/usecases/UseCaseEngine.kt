package me.sujanpoudel.playdeals.usecases

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import io.vertx.ext.web.RoutingContext
import me.sujanpoudel.playdeals.common.handleExceptions

interface Validated {
  suspend fun validate()
}

interface UseCase<in Input, out Output> {
  suspend fun execute(input: Input): Output {
    validate(input)
    return doExecute(input)
  }

  suspend fun validate(input: Input) {}
  suspend fun doExecute(input: Input): Output
}

suspend fun <Request, Input, Output> RoutingContext.executeUseCase(
  useCase: UseCase<Input, Output>,
  toContext: suspend () -> Request,
  toInput: (Request) -> Input,
  onError: (Throwable) -> Unit = this::handleExceptions,
  onSuccess: (Output) -> Unit,
): Result<Output, Throwable> = runCatching { toContext.invoke() }
  .andThen { runCatching { (it as? Validated)?.validate(); it } }
  .andThen { runCatching { toInput.invoke(it) } }
  .andThen { runCatching { useCase.execute(it) } }
  .onSuccess(onSuccess)
  .onFailure(onError)
