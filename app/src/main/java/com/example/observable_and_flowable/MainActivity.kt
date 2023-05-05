package com.example.observable_and_flowable

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.ReplaySubject
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main1()
        main2() 
    }

    fun main1() {
        // Создание Observable
        val observable = Observable.create<Int> { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onComplete()
        }

        // Создание Flowable
        val flowable = Flowable.range(1, 5)

        // Применение операторов для Observable
        observable
            .map { it * 2 }
            .filter { it > 3 }
            .distinct()
            .take(2)
            .subscribe { println("Observable result1: $it") }

        observable
            .flatMap { Observable.just(it, it * 10) }
            .subscribe { println("Observable result2: $it") }

        observable
            .delay(1, TimeUnit.SECONDS)
            .subscribe { println("Observable result3: $it") }

        observable
            .groupBy { if (it % 2 == 0) "even" else "odd" }
            .flatMapSingle { group -> group.toList() }
            .subscribe { println("Observable result4: $it") }

        observable
            .count()
            .subscribe ({ println("Observable result5: $it") }, { it.printStackTrace() })

        // Применение операторов для Flowable
        flowable
            .skip(2)
            .concatWith(Flowable.just(6, 7))
            .reduce(0) { acc, value -> acc + value }
            .subscribe({ println("Flowable result1: $it") }, { it.printStackTrace() })

        flowable
            .scan(0) { acc, value -> acc + value }
            .subscribe { println("Flowable result2: $it") }

        flowable
            .window(2)
            .flatMap { it.reduce(0) { acc, value -> acc + value }.toFlowable() }
            .subscribe { println("Flowable result3: $it") }

        flowable
            .flatMapSingle { value -> Single.just(value * 3) }
            .subscribe { println("Flowable result4: $it") }

        flowable
            .sorted()
            .subscribe { println("Flowable result5: $it") }
    }

    fun main2() {
        val source: ReplaySubject<Int> = ReplaySubject.create()
        // Он получит 1, 2, 3, 4
        source.subscribe(getFirstObserver())
        source.onNext(1)
        source.onNext(2)
        source.onNext(3)
        source.onNext(4)
        source.onComplete()
        // Он также получит 1, 2, 3, 4 так как он использует Replay Subject
        source.subscribe(getSecondObserver())

        val source2: BehaviorSubject<Int> = BehaviorSubject.create()
        // Получит 1, 2, 3, 4 and onComplete
        source2.subscribe(getFirstObserver())
        source2.onNext(1)
        source2.onNext(2)
        source2.onNext(3)
        // Получит 3(последний элемент) и 4(последующие элементы) и onComplete
        source2.subscribe(getSecondObserver())
        source2.onNext(4)
        source2.onComplete()

        val source3: AsyncSubject<Int> = AsyncSubject.create()
// Получит только 4 и onComplete
        source3.subscribe(getFirstObserver())
        source3.onNext(1)
        source3.onNext(2)
        source3.onNext(3)
// Тоже получит только 4 и onComplete
        source3.subscribe(getSecondObserver())
        source3.onNext(4)
        source3.onComplete()
    }
}

private fun getFirstObserver(): Observer<Int> {
    return object : Observer<Int> {
        override fun onSubscribe(d: Disposable) {
            // вызывается при подписке на Observable
        }

        override fun onNext(value: Int) {
            println("Первый получил значение $value")
        }

        override fun onError(e: Throwable) {
            // вызывается при возникновении ошибки
        }

        override fun onComplete() {
            println("Первый получил onComplete")
        }
    }
}

private fun getSecondObserver(): Observer<Int> {
    return object : Observer<Int> {
        override fun onSubscribe(d: Disposable) {
            // вызывается при подписке на Observable
        }

        override fun onNext(value: Int) {
            println("Второй получил значение $value")
        }

        override fun onError(e: Throwable) {
            // вызывается при возникновении ошибки
        }

        override fun onComplete() {
            println("Второй получил onComplete")
        }
    }
}