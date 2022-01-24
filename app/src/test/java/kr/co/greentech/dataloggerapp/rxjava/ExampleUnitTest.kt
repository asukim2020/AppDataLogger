package kr.co.greentech.dataloggerapp.rxjava

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.functions.Function
import org.junit.Test
import java.util.concurrent.Callable

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        Observable.just("Hello", "RxJava3!!").subscribe(System.out::println)
    }

    @Test
    fun just() {
        val source = Observable.just("RED", "GREEN", "YELLOW")

        val dispose = source.subscribe(
                { result -> println("onNext() : value : $result") },
                { err -> println("onError() : err : " + err.message) },
                { println("onComplete()") }
        )

        println("isDisposed() : " + dispose.isDisposed)
    }

    @Test
    fun create() {
        val source: Observable<Int> = Observable.create { emit ->
            emit.onNext(100)
            emit.onNext(200)
            emit.onNext(300)
            emit.onComplete()
        }

        source.subscribe(System.out::println)
    }

    @Test
    fun fromArray() {
        val arr = listOf<Int>(100, 200, 300)
        val source = Observable.fromArray(arr)
        source.subscribe(System.out::println)
    }

    @Test
    fun callable() {
        // 비동기
        val callable: Callable<String> = Callable<String> {
            Thread.sleep(1000)
            "Hello Callable"
        }

        val source = Observable.fromCallable(callable)
        source.subscribe(System.out::println)
    }

//    @Test
//    fun future() {
//        val future = Executors.newSingleThreadExecutor().submit {
//            Thread.sleep(1000)
//            "Hello Future"
//        }
//
//        val source = Observable.fromFuture(future)
//        source.subscribe(System.out::println)
//    }

    @Test
    fun single() {
//        val source: Single<String> = Single.just("Hello Single")
//        source.subscribe(System.out::println)

        // 1. 기존 Observable에서 Single 객체로 변환하기.
        val source = Observable.just("Hello Single")
        Single.fromObservable(source).subscribe { s: String? ->
            println(s)
        }

        // 2. single() 함수를 호출해 Single 객체 생성하기.
        Observable.just("Hello Single").single("default item").subscribe { s ->
            println(s)
        }

        // 3. first() 함수를 호출해 Single 객체 생성하기.
        val colors = arrayOf("Red", "Blue", "Glod")
        Observable.fromArray(*colors).first("default value").subscribe { s: String? ->
            println(s)
        }

        // 4. empty Observable에서 Single 객체 생성하기.
        Observable.empty<String>().single("default value").subscribe { s ->
            println(s)
        }
    }

    @Test
    fun mapTest() {
        val getDiamond = Function { ball: String -> "$ball◆" }

        val balls = arrayOf("1", "2", "3", "5")
        val source = Observable.fromArray(*balls)
                .map(getDiamond)
        source.subscribe { s: String? -> println(s) }
    }

    @Test
    fun flatMapTest() {
        val getDiamond = Function { ball: String -> Observable.just("$ball◆") }
        val balls = arrayOf("1", "2", "3", "5")
        val source = Observable.fromArray(*balls)
                .flatMap(getDiamond)
        source.subscribe { s: String? -> println(s) }
    }


    @Test
    fun gugudan() {
        val dan = 4

        val gugudan = Function { num: Int ->
            Observable.range(1, 9)
                    .map { row: Int -> num.toString() + " * " + row + " = " + dan * row
                    }
        }
        val source = Observable.just(dan).flatMap(gugudan)
        source.subscribe { s: String? -> println(s) }
    }

    @Test
    fun filterTest() {
        val objs = arrayOf("1", "2", "3")
        val source = Observable.fromArray(*objs)
                .filter { obj: String -> obj.endsWith("1") }
        source.subscribe { s: String? -> println(s) }
    }

    @Test
    fun reduceTest() {
        val mergeBalls = BiFunction { ball1: String, ball2: String -> "$ball2($ball1)" }
        val balls = arrayOf("1", "2", "3")
        val source = Observable.fromArray(*balls)
                .reduce(mergeBalls)
        source.subscribe { s: String? -> println(s) }
    }
}




