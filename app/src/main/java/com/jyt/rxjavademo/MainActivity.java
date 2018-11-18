package com.jyt.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Observer<String> observer;
    private Observable<String> observable1;
    private Observable<String> observable2;
    private Action0 completeAction;
    private Action1<String> nextAction;
    private Action1<Throwable> errorAction;
    private ArrayList<User> mUserData = new ArrayList<>();
    private ArrayList<String> mInterests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInterests.add("baskball");
        mInterests.add("football");
        mUserData.add(new User("zhangsan", mInterests));
        mUserData.add(new User("lisi", mInterests));

        observerMethod();
        actionMethod();

    }

    private void mapMethod() {
        Observable.from(mUserData).map(new Func1<User, String>() {
            @Override
            public String call(User user) {
                return user.name;
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                //在这里可以用成员变量中的一个集合把这些name给装起来, 在这里直接打印了
                Log.e(TAG, s);
            }
        });
    }

    private void actionMethod() {
        //因为使用Observer, 有可能其中的方法不是全部被用到的, 因此可以使用action0和action1
        completeAction = new Action0() {
            @Override
            public void call() {
                Log.e(TAG, "complete");
            }
        };

        nextAction = new Action1<String>() {
            @Override
            public void call(String string) {
                Log.e(TAG, string);
            }
        };

        errorAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, "error");
            }
        };
    }

    private void observerMethod() {
        //这里的对象也可以是subscriber
        observer = new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.e(TAG, "complete");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "error");
            }

            @Override
            public void onNext(String string) {
                Log.e(TAG, string);
            }
        };

        observable1 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("string1");
                subscriber.onNext("string2");

                subscriber.onCompleted();
            }
        });

        observable2 = Observable.just("string1", "string2");
    }

    /**
     * 被观察者是用create方法创建的
     * @param view
     */
    public void create(View view) {
        observable1.subscribe(observer);
    }

    /**
     * 被观察者是用just方法创建的
     * @param view
     */
    public void just(View view) {
        observable2.subscribe(observer);
    }

    /**
     * 观察者的对象是action接口类型的, 底下有两种具体类型: action0和action1
     * @param v
     */
    public void action(View v) {
        observable2.subscribe(nextAction);
        observable2.subscribe(nextAction, errorAction);
        //error和complete是互斥的, 一个被调用了, 另一个就不被调用了
        observable2.subscribe(nextAction, errorAction, completeAction);
    }

    /**
     * 用来取出user类中的姓名, 然后将每个user中的姓名组合成一个集合(每一个user中只有一个name属性)
     * @param view
     */
    public void map(View view) {
        mapMethod();
    }

    /**
     * 用来取出user类中的爱好, 然后依次打印(每个user对应多个爱好), 其实map和flatmap这两个操作符的作用就是避免嵌套循环
     * @param view
     */
    public void flatMap(View view) {
        flatMapMethod();
    }

    /**
     * 事件发起者(订阅事件)在子线程, 事件消费者(观察事件)在主线程
     * @param view
     */
    public void thread(View view) {
        threadMethod();
    }

    private void threadMethod() {
        Observable.just("string1", "string2")
                .subscribeOn(Schedulers.newThread()) //表示在子线程中订阅
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<String>() { //在请求网络的时候可以用来将获取的数据保存下来, 先执行完成这个方法, 再往下走呢
                    @Override
                    public void call(String s) {
                        Log.e(TAG, s);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e(TAG, s);
                    }
                });
    }

    /**
     * 事件发起者(订阅事件)在子线程, 事件消费者(观察事件)在主线程, 中间切换几个线程
     * @param v
     */
    public void diffThread(View v) {
        diffThreadMethod();
    }

    private void diffThreadMethod() {
        Observable.from(mUserData)
                .subscribeOn(Schedulers.newThread())    //表示订阅的时候再子线程
                .observeOn(Schedulers.io())             //表示在io线程中操作
                .map(new Func1<User, String>() {
                    @Override
                    public String call(User user) {
                        return user.name;
                    }
                }).observeOn(AndroidSchedulers.mainThread())    //表示观察的结果在主线程中运行
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e(TAG, s);
                    }
                });
    }

    private void flatMapMethod() {
        Observable.from(mUserData).flatMap(new Func1<User, Observable<String>>() {
            @Override
            public Observable<String> call(User user) {
                return Observable.from(user.list);
            }
        }).subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e(TAG, s);
            }
        });
    }
}

class User{
    public String name;
    public ArrayList<String> list;

    public User(String name, ArrayList<String> list) {
        this.name = name;
        this.list = list;
    }

}
