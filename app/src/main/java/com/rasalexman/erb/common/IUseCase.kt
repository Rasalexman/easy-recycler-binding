package com.rasalexman.erb.common

interface IUseCase {

    interface SOut<T : Any> : IUseCase {
        suspend operator fun invoke(): T
    }

    interface Out<T : Any> : IUseCase {
        operator fun invoke(): T
    }
}