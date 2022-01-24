package kr.co.greentech.dataloggerapp.rxjava;


import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observables.GroupedObservable;
import io.reactivex.rxjava3.schedulers.Timed;

import static kr.co.greentech.dataloggerapp.rxjava.Shape.BALL;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.BLUE;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.DIAMOND;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.GREEN;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.ORANGE;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.PENTAGON;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.PUPPLE;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.RED;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.SKY;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.STAR;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.YELLOW;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.rectangle;
import static kr.co.greentech.dataloggerapp.rxjava.Shape.triangle;

public class JavaTest {

    // map 관련 함수는 데이터 변환 용도이다.
    @Test
    public void mapTest() {
        Function<String, String> getDiamond = (ball -> ball + "◆");

        String[] balls = {"1", "2", "3", "5"};
        Observable<String> source = Observable.fromArray(balls)
                .map(getDiamond);
        source.subscribe(System.out::println);
    }

    // flatMap 은 데이터 가공시 Observable 반환해야 한다.
    @Test
    public void flatMapTest() {
        Function<String, Observable<String>> getDiamond = (ball -> Observable.just(ball + "◆"));
        String[] balls = {"1", "2", "3", "5"};
        Observable<String> source = Observable.fromArray(balls)
                .flatMap(getDiamond);
        source.subscribe(System.out::println);
    }


    @Test
    public void gugudan() {
        int dan = 4;

        Function<Integer, Observable<String>> gugudan = num ->
        Observable.range(1, 9).map(row -> num + " * " + row + " = " + dan*row);
        Observable<String> source = Observable.just(dan).flatMap(gugudan);
        source.subscribe(System.out::println);
    }

    @Test
    public void filterTest() {
        String[] objs = {"1", "2", "3"};
        Observable<String> source = Observable.fromArray(objs)
                .filter(obj -> obj.endsWith("1"));
        source.subscribe(System.out::println);
    }

    @Test
    public void reduceTest() {
        BiFunction<String, String, String> mergeBalls = (ball1, ball2) -> ball2 + "(" + ball1 + ")";

        String[] balls = {"1", "2", "3"};
        Maybe<String> source = Observable.fromArray(balls)
                .reduce(mergeBalls);
        source.subscribe(System.out::println);
    }

    // 같은 쓰레드에서 진행됨
    @Test
    public void intervalTest() {
        CommonUtils.exampleStart();
        Observable<Long> source = Observable.interval(100L, TimeUnit.MILLISECONDS)
                .map(data -> (data + 1) * 100)
                .take(5);
        source.subscribe(Log::it);
        CommonUtils.sleep(1000);
    }

    // 최조 한 번만 실행
    @Test
    public void timerTest() {
        CommonUtils.exampleStart();
        Observable<String> source = Observable.timer(500L, TimeUnit.MICROSECONDS)
                .map(notUsed -> {
                    return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                            .format(new Date());
                });
        source.subscribe(Log::it);
        CommonUtils.sleep(1000);
    }

    @Test
    public void rangeTest() {
        CommonUtils.exampleStart();
        Observable<Integer> source = Observable.range(1, 10)
                .filter(number -> number % 2 == 0);
        source.subscribe(Log::it);
    }

    @Test
    public void intervalRangeTest() {
        CommonUtils.exampleStart();
        Observable<Long> source = Observable.intervalRange(
                1,
                5,
                100L,
                100L,
                TimeUnit.MICROSECONDS
        );
        source.subscribe(Log::it);
        CommonUtils.sleep(1000);
    }

    @Test
    public void intervalToIntervalRangeTest() {
        Observable<Long> source = Observable.interval(100L, TimeUnit.MILLISECONDS)
                .map(val -> val + 1)
                .take(5);
        source.subscribe(Log::it);
        CommonUtils.sleep(1000);
    }
    
    // 구독할 때 생성함 - 나중에 확인하자
    @Test
    public void deferTest() {
        
    }

    @Test
    public void repeat() {
        String[] balls = {"1", "3", "5"};
        Observable<String> source = Observable.fromArray(balls)
                .repeat(4);

        source.doOnComplete(() -> System.out.println("onComplete"))
                .subscribe(Log::i);
    }

    // 반복 할 때마다 쓰레드가 바뀜
    @Test
    public void heartBeatTest() {
        CommonUtils.exampleStart();
        String serverUrl = "https://api.github.com/zen";

        Observable.timer(2, TimeUnit.SECONDS)
                .map(val -> serverUrl)
                .map(OkHttpHelper::get)
                .repeat()
                .subscribe(res -> Log.it("Ping Result : " + res));
        CommonUtils.sleep(100000);
    }


    // 들어온 순서가 보장됨
    @Test
    public void concatMap() {
        CommonUtils.exampleStart();

        String[] balls = {"1", "3", "5"};
        Observable<String> source = Observable.interval(100L, TimeUnit.MILLISECONDS)
                .map(Long::intValue)
                .map(idx -> balls[idx])
                .take(balls.length)
                .concatMap(ball -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                        .map(notUsed -> ball + "ㅁ")
                        .take(2)
                );
        source.subscribe(Log::it);

        CommonUtils.sleep(2000);
    }

    // 순서가 보장되지 않음
    @Test
    public void flatMap() {
        CommonUtils.exampleStart();

        String[] balls = {"1", "3", "5"};
        Observable<String> source = Observable.interval(100L, TimeUnit.MILLISECONDS)
                .map(Long::intValue)
                .map(idx -> balls[idx])
                .take(balls.length)
                .flatMap(ball -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                        .map(notUsed -> ball + "ㅁ")
                        .take(2)
                );
        source.subscribe(Log::it);

        CommonUtils.sleep(2000);
    }


    // 데이터가 들어오면 현재 작업을 중단하며, 마지막에 들어온 값을 처리함함
    // 중간에 끊기더라도 마지막 데이터 처리를 보장함
   @Test
    public void switchMap() {
       CommonUtils.exampleStart();

       String[] balls = {"1", "3", "5"};
       Observable<String> source = Observable.interval(100L, TimeUnit.MILLISECONDS)
               .map(Long::intValue)
               .map(idx -> balls[idx])
               .doOnNext(Log::it)
               .take(balls.length)
               .switchMap(ball -> Observable.interval(200L, TimeUnit.MILLISECONDS)
                       .map(notUsed -> ball + "ㅁ")
                       .take(2)
               );
       source.subscribe(Log::it);

       CommonUtils.sleep(2000);
    }

    @Test
    public void groupBy() {
        String[] objs = {"6", "4", "2-T", "2", "6-T", "4-T"};
        Observable<GroupedObservable<String, String>> source
                = Observable.fromArray(objs).groupBy(Shape::getShape);

        source.subscribe( obj -> {
            obj
//                    .filter(val -> obj.getKey().contains("TRIANGLE"))
                    .subscribe(
                    val -> System.out.println("group: " + obj.getKey() + ", value: " + val)
            );
        });
    }


    @Test
    public void scan() {
        String[] balls = {"1", "3", "5"};
        Observable<String> source = Observable.fromArray(balls)
                .scan((ball1, ball2) -> ball2 + "(" + ball1 + ")");
        source.subscribe(Log::i);
    }


    @Test
    public void zip() {
        String[] shapes = {BALL, PENTAGON, STAR};
        String[] coloredTriangles = {triangle(YELLOW), triangle(PUPPLE), triangle(SKY)};

        Observable<String> source = Observable.zip(
                Observable.fromArray(shapes).map(Shape::getSuffix),
                Observable.fromArray(coloredTriangles).map(Shape::getColor),
                (suffix, color) -> color + suffix);

        source.subscribe(Log::i);
        CommonUtils.exampleComplete();
    }

    @Test
    public void countZip() {
        Observable<Integer> source = Observable.zip(
                Observable.just(100, 200, 300),
                Observable.just(10, 20, 30),
                Observable.just(1, 2, 3),
                (a, b, c) -> a + b + c );
        source.subscribe(Log::i);
        CommonUtils.exampleComplete();
    }

    @Test
    public void zipInterval() {
        Observable<String> source = Observable.zip(
                Observable.just("RED", "GREEN", "BLUE"),
                Observable.interval(200L, TimeUnit.MILLISECONDS),
                (value,i) -> value
        );

        CommonUtils.exampleStart();
        source.subscribe(Log::it);
        CommonUtils.sleep(1000);
        CommonUtils.exampleComplete();
    }

    @Test
    public void combineLatest() {
        String[] data1 = {PUPPLE, ORANGE, SKY, YELLOW}; //6, 7, 4, 2
        String[] data2 = {DIAMOND, STAR, PENTAGON};

        Observable<String> source = Observable.combineLatest(
                Observable.fromArray(data1)
                        .zipWith( //zipWith()로 깔끔하게 코드 정리
                                Observable.interval(100L, TimeUnit.MILLISECONDS),
                                (shape, notUsed) -> Shape.getColor(shape)),
                Observable.fromArray(data2)
                        .zipWith(
                                Observable.interval(150L, 200L, TimeUnit.MILLISECONDS),
                                (shape, notUsed) -> Shape.getSuffix(shape)),
                (v1, v2) -> v1 + v2);

        source.subscribe(Log::i);
        CommonUtils.sleep(1000);
        CommonUtils.exampleComplete();
    }

    @Test
    public void merge() {
        String[] data1 = {RED, GREEN}; //1, 3
        String[] data2 = {YELLOW, SKY, PUPPLE}; //2, 4, 6

        Observable<String> source1 = Observable.interval(0L, 100L, TimeUnit.MILLISECONDS)
                .map(Long::intValue)
                .map(idx -> data1[idx])
                .take(data1.length);
        Observable<String> source2 = Observable.interval(50L, TimeUnit.MILLISECONDS)
                .map(Long::intValue)
                .map(idx -> data2[idx])
                .take(data2.length);

        Observable<String> source = Observable.merge(source1, source2);
        source.subscribe(Log::i);
        CommonUtils.sleep(1000);
        CommonUtils.exampleComplete();
    }

    // 첫 번째 observable 이 complete 되면 다음 observable 실행을 반복
    @Test
    public void concat() {
        Action onCompleteAction = () -> Log.d("onComplete()");

        String[] data1 = {"1", "3", "5"};
        String[] data2 = {"2", "4", "6"};

        Observable<String> source1 = Observable.fromArray(data1)
                .doOnComplete(onCompleteAction);

        Observable<String> source2 = Observable.interval(100L, TimeUnit.MILLISECONDS)
                .map(Long::intValue)
                .map(idx -> data2[idx])
                .take(data2.length)
                .doOnComplete(onCompleteAction);

        Observable<String> source = Observable.concat(source1, source2)
                .doOnComplete(onCompleteAction);
        source.subscribe(Log::i);
        CommonUtils.sleep(1000);

    }

    // 첫 observable 만 발행하고 나머지 무시
    @Test
    public void amb() {
        String[] data1 = {RED, GREEN, BLUE};
        String[] data2 = {rectangle(YELLOW), rectangle(SKY)};

        List<Observable<String>> sources = Arrays.asList(
                Observable.fromArray(data1)
                        .doOnComplete(() -> Log.d("Observable #1 : onComplete()")),
                Observable.fromArray(data2)
                        .delay(100L, TimeUnit.MILLISECONDS)
                        .doOnComplete(() -> Log.d("Observable #2 : onComplete()")));

        Observable.amb(sources)
                .doOnComplete(() -> Log.d("Result : onComplete()"))
                .subscribe(Log::i);
        CommonUtils.sleep(1000);
        CommonUtils.exampleComplete();
    }


    // 현재 observable 에서 take until 사용 시 데이터 발생 중지 및 complete 발생
    @Test
    public void takeUntil() {
        // 1, 2, 3, 4, 5, 6
        String[] data = {RED, YELLOW, GREEN, SKY, BLUE, PUPPLE};

        Observable<String> source = Observable.fromArray(data)
                .zipWith(Observable.interval(100L, TimeUnit.MILLISECONDS),
                        (val, notUsed) -> val)
                .takeUntil(Observable.timer(500L, TimeUnit.MILLISECONDS));

        source.subscribe(Log::i);
        CommonUtils.sleep(1000);
        CommonUtils.exampleComplete();
    }

    // 현재 observable 에서 skip until 사용 시 까지 데이터 발행을 건너뜀
    @Test
    public void skipUntil() {
        // 1, 2, 3, 4, 5, 6
        String[] data = {RED, YELLOW, GREEN, SKY, BLUE, PUPPLE};

        Observable<String> source = Observable.fromArray(data)
                .zipWith(Observable.interval(100L, TimeUnit.MILLISECONDS),
                        (val, notUsed) -> val)
                .skipUntil(Observable.timer(400L, TimeUnit.MILLISECONDS));

        source.subscribe(Log::i);
        CommonUtils.sleep(1000);
        CommonUtils.exampleComplete();
    }


    // 주어진 조건과 일치하면 true 발행
    @Test
    public void all() {
        String[] data = {"1", "2", "3", "4"};

        Single<Boolean> source = Observable.fromArray(data)
                .map(Shape::getShape)
                .all(BALL::equals);
        source.subscribe(Log::it);
    }

    // 데이터 발행 간격을 알려줌
    @Test
    public void timeInterval() {
        String[] data = {"RED", "GREEN", "ORANGE"};

        CommonUtils.exampleStart();
        Observable<Timed<String>> source = Observable.fromArray(data)
                .delay(item -> {
                    CommonUtils.doSomething();
                    return Observable.just(item);
                })
                .timeInterval();

        source.subscribe(Log::it);
        CommonUtils.sleep(1000);
    }
}
