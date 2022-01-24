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
import io.reactivex.rxjava3.schedulers.Schedulers;
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


import static kr.co.greentech.dataloggerapp.rxjava.Shape.RED;

public class Scaduler {

    @Test
    public void scaduler() {
        String[] orgs = {RED, GREEN, BLUE};
        Observable.fromArray(orgs)
                .doOnNext(data -> Log.v("Original data : " + data))
                .map(data -> "<<" + data + ">>")
                .subscribeOn(Schedulers.newThread())
                .subscribe(Log::i);
        CommonUtils.sleep(500);

        Observable.fromArray(orgs)
                .doOnNext(data -> Log.v("Original data : " + data))
                .map(data -> "##" + data + "##")
                .subscribeOn(Schedulers.newThread())
                .subscribe(Log::i);
        CommonUtils.sleep(500);
    }
}
