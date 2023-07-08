package me.sujanpoudel.playdeals

import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.runCatching

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

object UseCaseExecutor {
  suspend fun <Request, Response, Input, Output> execute(
    useCase: UseCase<Input, Output>,
    toContext: () -> Request,
    toInput: (Request) -> Input,
    toResponse: (Output) -> Response
  ) =
    runCatching { toContext.invoke() }
      .andThen { runCatching { (it as? Validated)?.validate(); it } }
      .andThen { runCatching { toInput.invoke(it) } }
      .andThen { runCatching { useCase.execute(it) } }
      .andThen { runCatching { toResponse(it) } }

  // some params - no response
  suspend fun <Request, Input> execute(
    useCase: UseCase<Input, Unit>,
    toContext: () -> Request,
    toInput: (Request) -> Input
  ) = execute(useCase, toContext, toInput) {}

  // no params - no response
  suspend fun execute(useCase: UseCase<Unit, Unit>) =
    execute(useCase, { }) { }

  // no params - some response
  suspend fun <Response, Output> execute(
    useCase: UseCase<Unit, Output>,
    toResponse: (Output) -> Response
  ) =
    execute(useCase, {}, { }, toResponse)
}
