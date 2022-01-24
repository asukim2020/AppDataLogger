package kr.co.greentech.dataloggerapp

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.AsyncSubject
import org.junit.Test
import java.util.concurrent.Callable

class rxJavaTest {

    @Test
    fun just() {
        // Hello
        Observable.just("Hello", "RxJava3!!").subscribe(System.out ::println)

        // Default
        Observable.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println)

        // Color
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

        source.subscribe {
                data -> println("Result: $data")
        }
    }

    @Test
    fun fromArray() {
        val arr = listOf<Int>(100, 200, 300)
        val source = Observable.fromArray(arr)
        source.subscribe(System.out::println)

        val list = ArrayList<String>()
        list.add("Jerry")
        list.add("William")
        list.add("Bob")

        val observable = Observable.fromIterable(list)
        observable.subscribe(System.out::println)
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
    fun asyncSubject() {
        val subject = AsyncSubject.create<String>()
        subject.subscribe { data -> println("1_$data") }
        subject.onNext("1")
        subject.onNext("3")
        subject.subscribe { data -> println("2_$data") }
        subject.onNext("5")
        subject.onComplete()
    }

    @Test
    fun asyncSubject2() {
        val subject = AsyncSubject.create<Int>()
        subject.onNext(10)
        subject.onNext(11)
        subject.subscribe { data -> println("1_$data") }
        subject.onNext(12)
        subject.onComplete()
        subject.onNext(13)
        subject.subscribe { data -> println("2_$data") }
        subject.subscribe { data -> println("3_$data") }
    }
}